package com.btd.maiscreen.streaming

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.CookieJar
import okhttp3.Cookie
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.concurrent.TimeUnit
import com.amplifyframework.core.Amplify
import com.btd.maiscreen.util.SecureLogger
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Android implementation of PlaybackApi using OkHttp with CloudFront signed cookies
 */
class AndroidPlaybackApi(
    private val config: PlaybackConfig
) : PlaybackApi {
    
    companion object {
        private const val TAG = "AndroidPlaybackApi"
    }
    
    private val cookieManager = CookieManager().apply {
        setCookiePolicy(CookiePolicy.ACCEPT_ALL)
    }
    
    // Make this the global cookie handler (optional but fine)
    init { 
        java.net.CookieHandler.setDefault(cookieManager) 
    }

    // One OkHttpClient shared by API + HLS with security configurations
    private val okClient = OkHttpClient.Builder()
        .cookieJar(object : CookieJar {
            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                SecureLogger.d(TAG, "Saving ${cookies.size} cookies from: ${url.host}")

                // Convert OkHttp cookies to Java format for CookieManager
                val uri = url.toUri()
                val cookieHeaders = cookies.map { cookie ->
                    buildString {
                        append("${cookie.name}=${cookie.value}")
                        if (cookie.domain.isNotEmpty()) append("; Domain=${cookie.domain}")
                        if (cookie.path.isNotEmpty()) append("; Path=${cookie.path}")
                        if (cookie.secure) append("; Secure")
                        if (cookie.httpOnly) append("; HttpOnly")
                    }
                }
                cookieManager.put(uri, mapOf("Set-Cookie" to cookieHeaders))
            }

            override fun loadForRequest(url: HttpUrl): List<Cookie> {
                val uri = url.toUri()
                val cookieHeaders = cookieManager.get(uri, emptyMap())["Cookie"] ?: emptyList()

                val cookies = cookieHeaders.flatMap { cookieHeader ->
                    cookieHeader.split(";").mapNotNull { cookiePair ->
                        val parts = cookiePair.trim().split("=", limit = 2)
                        if (parts.size == 2) {
                            Cookie.Builder()
                                .name(parts[0].trim())
                                .value(parts[1].trim())
                                .domain(url.host)
                                .path("/")
                                .build()
                        } else null
                    }
                }

                SecureLogger.d(TAG, "Loaded ${cookies.size} cookies for ${url.host}")
                return cookies
            }
        })
        // Security: Add timeout configurations to prevent hanging requests
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        // TODO: Add certificate pinning for CloudFront domain
        // .certificatePinner(CertificatePinner.Builder()
        //     .add(config.cloudFrontDomain, "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
        //     .build())
        .build()
    
    override suspend fun getManifest(
        assetId: String, 
        tier: String, 
        mode: String
    ): PlaybackResult<String> = withContext(Dispatchers.IO) {
        try {
            val url = "https://${config.cloudFrontDomain}/api/play/$assetId?tier=$tier&mode=$mode"
            val isTestItem = assetId.startsWith("TEST")
            
            if (isTestItem) {
                SecureLogger.d(TAG, "TEST MODE: Testing AWS API call for test item")
            } else {
                SecureLogger.d(TAG, "Making API call for asset: $assetId")
            }

            // Get JWT token from Amplify Auth
            val jwtToken = getCurrentJwtToken()
            if (jwtToken == null) {
                SecureLogger.e(TAG, "No JWT token available - user needs to be authenticated")
                return@withContext PlaybackError("User not authenticated - JWT token missing. Please ensure you're logged in.")
            }
            SecureLogger.d(TAG, "JWT token obtained successfully")
            
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $jwtToken")
                .addHeader("Content-Type", "application/json")
                .get()
                .build()
            
            okClient.newCall(request).execute().use { resp ->
                SecureLogger.d(TAG, "API response code: ${resp.code}")

                if (!resp.isSuccessful) {
                    val errorBody = resp.body?.string()
                    SecureLogger.e(TAG, "API call failed with code ${resp.code}")
                    return@withContext PlaybackError("API request failed. Please try again later.", Exception("HTTP ${resp.code}"))
                }

                val body = resp.body?.string() ?: return@withContext PlaybackError("Empty response from server")
                SecureLogger.d(TAG, "API response received successfully")

                // Check if we received the required CloudFront cookies
                val receivedCookies = resp.headers.values("Set-Cookie")
                val requiredCookies = listOf("CloudFront-Policy", "CloudFront-Signature", "CloudFront-Key-Pair-Id")
                val foundCookies = mutableListOf<String>()

                receivedCookies.forEach { cookieHeader ->
                    requiredCookies.forEach { requiredCookie ->
                        if (cookieHeader.startsWith("$requiredCookie=")) {
                            foundCookies.add(requiredCookie)
                        }
                    }
                }

                if (isTestItem) {
                    SecureLogger.d(TAG, "COOKIE TEST RESULTS:")
                    SecureLogger.d(TAG, "CloudFront-Policy: ${if (foundCookies.contains("CloudFront-Policy")) "FOUND" else "MISSING"}")
                    SecureLogger.d(TAG, "CloudFront-Signature: ${if (foundCookies.contains("CloudFront-Signature")) "FOUND" else "MISSING"}")
                    SecureLogger.d(TAG, "CloudFront-Key-Pair-Id: ${if (foundCookies.contains("CloudFront-Key-Pair-Id")) "FOUND" else "MISSING"}")

                    if (foundCookies.size == requiredCookies.size) {
                        SecureLogger.d(TAG, "ALL COOKIES FOUND! Cookie setup is working correctly!")
                    } else {
                        val missing = requiredCookies - foundCookies.toSet()
                        SecureLogger.e(TAG, "MISSING COOKIES: $missing")
                    }
                } else {
                    if (foundCookies.size != requiredCookies.size) {
                        val missing = requiredCookies - foundCookies.toSet()
                        SecureLogger.w(TAG, "Missing CloudFront cookies: $missing")
                    }
                }

                val json = JSONObject(body)
                val manifestUrl = json.getString("manifestUrl")

                SecureLogger.d(TAG, "Got manifest URL successfully")
                
                // Quick runtime test - try to fetch the master playlist (skip for test items)
                if (!isTestItem) {
                    testMasterPlaylistAccess(assetId, manifestUrl)
                }

                PlaybackSuccess(manifestUrl)
            }
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Exception in getManifest", e)
            PlaybackError("Failed to load video content. Please try again later.", e)
        }
    }
    
    /**
     * Quick runtime test to verify cookies work for HLS requests
     */
    private suspend fun testMasterPlaylistAccess(assetId: String, manifestUrl: String) {
        try {
            SecureLogger.d(TAG, "Testing master playlist access...")
            val testRequest = Request.Builder().url(manifestUrl).build()

            okClient.newCall(testRequest).execute().use { r ->
                if (r.code == 200) {
                    SecureLogger.d(TAG, "Cookie test PASSED - HLS should work")
                } else {
                    SecureLogger.e(TAG, "Cookie test FAILED - HLS will have 403 errors")
                }
            }
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Cookie test exception", e)
        }
    }
    
    /**
     * Get the current JWT token from Amplify Auth
     * Uses reflection as a temporary workaround until proper Amplify SDK APIs are available
     * TODO: Replace reflection with proper SDK APIs when available
     */
    private suspend fun getCurrentJwtToken(): String? = suspendCoroutine { continuation ->
        try {
            Amplify.Auth.fetchAuthSession(
                { result ->
                    if (result.isSignedIn) {
                        try {
                            // Try to get the JWT token using reflection
                            // This is a workaround until proper SDK APIs are available
                            var accessToken: String? = null

                            // Try to get user pool tokens
                            try {
                                val cognitoSession = result as? Any
                                if (cognitoSession != null) {
                                    val getTokensMethod = cognitoSession.javaClass.getMethod("getUserPoolTokens")
                                    val tokensResult = getTokensMethod.invoke(cognitoSession)

                                    if (tokensResult != null) {
                                        val getValueMethod = tokensResult.javaClass.getMethod("getValue")
                                        val tokens = getValueMethod.invoke(tokensResult)

                                        if (tokens != null) {
                                            val getAccessTokenMethod = tokens.javaClass.getMethod("getAccessToken")
                                            accessToken = getAccessTokenMethod.invoke(tokens) as? String
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                SecureLogger.e(TAG, "Failed to extract token using primary method", e)
                            }

                            // Fallback: Try alternative method
                            if (accessToken == null) {
                                try {
                                    val methodNames = listOf("getUserPoolTokens", "getTokens", "getAccessToken")
                                    for (methodName in methodNames) {
                                        try {
                                            val method = result.javaClass.getMethod(methodName)
                                            val methodResult = method.invoke(result)

                                            if (methodResult != null && methodResult is String && methodResult.startsWith("eyJ")) {
                                                accessToken = methodResult
                                                break
                                            }
                                        } catch (e: Exception) {
                                            // Method doesn't exist, continue
                                        }
                                    }
                                } catch (e: Exception) {
                                    SecureLogger.e(TAG, "Alternative token extraction failed", e)
                                }
                            }

                            if (accessToken != null) {
                                SecureLogger.d(TAG, "JWT token retrieved successfully")
                            } else {
                                SecureLogger.e(TAG, "Failed to extract JWT token from session")
                            }
                            continuation.resume(accessToken)
                        } catch (e: Exception) {
                            SecureLogger.e(TAG, "Failed to extract JWT token", e)
                            continuation.resume(null)
                        }
                    } else {
                        SecureLogger.w(TAG, "User is not signed in")
                        continuation.resume(null)
                    }
                },
                { error ->
                    SecureLogger.e(TAG, "Failed to fetch auth session", error)
                    continuation.resume(null)
                }
            )
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Exception in getCurrentJwtToken", e)
            continuation.resume(null)
        }
    }
    
    /**
     * Get the OkHttp client for use with ExoPlayer
     * This ensures the same cookies are used for HLS segment requests
     */
    fun okHttpClient(): OkHttpClient = okClient
}

