package com.btd.maiscreen.streaming

import android.util.Log
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
import com.amplifyframework.core.Amplify
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

    // One OkHttpClient shared by API + HLS
    private val okClient = OkHttpClient.Builder()
        .cookieJar(object : CookieJar {
            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                Log.d(TAG, "💾 Saving ${cookies.size} cookies from: ${url.host}")
                cookies.forEach { cookie ->
                    Log.d(TAG, "Cookie: ${cookie.name} (domain: ${cookie.domain})")
                }
                
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
                Log.d(TAG, "🍪 Loading cookies for: ${url.host}")
                
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
                
                Log.d(TAG, "🍪 Loaded ${cookies.size} cookies for ${url.host}")
                return cookies
            }
        })
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
                Log.d(TAG, "🧪 TEST MODE: Testing AWS API call for test item")
                Log.d(TAG, "🧪 This will make a real API call to test the backend")
                
                // For test items, still make a real API call to test the backend
                Log.d(TAG, "🧪 Making real API call to: $url")
            } else {
                Log.d(TAG, "Making API call to: $url")
            }
            
            // Get JWT token from Amplify Auth
            Log.d(TAG, "🔑 Attempting to get JWT token...")
            val jwtToken = getCurrentJwtToken()
            if (jwtToken == null) {
                Log.e(TAG, "❌ No JWT token available - user needs to be authenticated")
                Log.e(TAG, "🔍 DEBUG: Check if user is signed in with Amplify")
                Log.e(TAG, "🔍 DEBUG: Verify Amplify configuration")
                return@withContext PlaybackError("User not authenticated - JWT token missing. Please ensure you're logged in.")
            }
            Log.d(TAG, "✅ JWT token obtained successfully")
            
            Log.d(TAG, "🔑 Using JWT token for API call")
            Log.d(TAG, "🔑 Request URL: $url")
            Log.d(TAG, "🔑 Authorization header: Bearer ${jwtToken.take(20)}...")
            
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $jwtToken")
                .addHeader("Content-Type", "application/json")
                .get()
                .build()
            
            okClient.newCall(request).execute().use { resp ->
                Log.d(TAG, "📡 API response code: ${resp.code}")
                Log.d(TAG, "📡 Response headers:")
                resp.headers.names().forEach { name ->
                    Log.d(TAG, "📡   $name: ${resp.headers.get(name)}")
                }
                
                if (!resp.isSuccessful) {
                    val errorBody = resp.body?.string()
                    Log.e(TAG, "❌ API call failed with code ${resp.code}")
                    Log.e(TAG, "❌ Error response body: $errorBody")
                    Log.e(TAG, "❌ Response headers: ${resp.headers}")
                    return@withContext PlaybackError("HTTP ${resp.code}: $errorBody", Exception("HTTP ${resp.code}"))
                }
                
                val body = resp.body?.string() ?: return@withContext PlaybackError("Empty body")
                Log.d(TAG, "API response body: $body")
                
                // Check if we received the required CloudFront cookies
                val receivedCookies = resp.headers.values("Set-Cookie")
                Log.d(TAG, "Received ${receivedCookies.size} Set-Cookie headers")
                
                val requiredCookies = listOf("CloudFront-Policy", "CloudFront-Signature", "CloudFront-Key-Pair-Id")
                val foundCookies = mutableListOf<String>()
                
                receivedCookies.forEach { cookieHeader ->
                    Log.d(TAG, "Set-Cookie header: $cookieHeader")
                    requiredCookies.forEach { requiredCookie ->
                        if (cookieHeader.startsWith("$requiredCookie=")) {
                            foundCookies.add(requiredCookie)
                            Log.d(TAG, "✅ Found required cookie: $requiredCookie")
                        }
                    }
                }
                
                if (isTestItem) {
                    Log.d(TAG, "🧪 COOKIE TEST RESULTS:")
                    Log.d(TAG, "🧪 CloudFront-Policy: ${if (foundCookies.contains("CloudFront-Policy")) "✅ FOUND" else "❌ MISSING"}")
                    Log.d(TAG, "🧪 CloudFront-Signature: ${if (foundCookies.contains("CloudFront-Signature")) "✅ FOUND" else "❌ MISSING"}")
                    Log.d(TAG, "🧪 CloudFront-Key-Pair-Id: ${if (foundCookies.contains("CloudFront-Key-Pair-Id")) "✅ FOUND" else "❌ MISSING"}")
                    
                    if (foundCookies.size == requiredCookies.size) {
                        Log.d(TAG, "🧪 🎉 ALL COOKIES FOUND! Cookie setup is working correctly!")
                    } else {
                        val missing = requiredCookies - foundCookies.toSet()
                        Log.e(TAG, "🧪 ⚠️ MISSING COOKIES: $missing")
                        Log.e(TAG, "🧪 Check your CloudFront configuration!")
                    }
                } else {
                    if (foundCookies.size != requiredCookies.size) {
                        val missing = requiredCookies - foundCookies.toSet()
                        Log.w(TAG, "⚠️ Missing CloudFront cookies: $missing")
                        Log.w(TAG, "This will cause 403 errors on HLS segments")
                    }
                }
                
                val json = JSONObject(body)
                val manifestUrl = json.getString("manifestUrl")
                
                Log.d(TAG, "Got manifest URL: $manifestUrl")
                
                // 🧪 Quick runtime test - try to fetch the master playlist (skip for test items)
                if (isTestItem) {
                    Log.d(TAG, "🧪 Skipping master playlist test for test item")
                } else {
                    testMasterPlaylistAccess(assetId, manifestUrl)
                }
                
                PlaybackSuccess(manifestUrl)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in getManifest", e)
            PlaybackError("Failed to get manifest: ${e.message}", e)
        }
    }
    
    /**
     * Quick runtime test to verify cookies work for HLS requests
     */
    private suspend fun testMasterPlaylistAccess(assetId: String, manifestUrl: String) {
        try {
            Log.d(TAG, "🧪 Testing master playlist access...")
            val testRequest = Request.Builder().url(manifestUrl).build()
            
            okClient.newCall(testRequest).execute().use { r ->
                Log.d(TAG, "🧪 Master fetch code=${r.code}")
                Log.d(TAG, "🧪 Sent cookies? ${r.request.header("Cookie")}")
                
                if (r.code == 200) {
                    Log.d(TAG, "✅ Cookie test PASSED - HLS should work!")
                } else {
                    Log.e(TAG, "❌ Cookie test FAILED - HLS will have 403 errors")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "🧪 Cookie test exception: ${e.message}")
        }
    }
    
    /**
     * Get the current JWT token from Amplify Auth
     */
    private suspend fun getCurrentJwtToken(): String? = suspendCoroutine { continuation ->
        try {
            Log.d(TAG, "🔑 Starting JWT token retrieval...")
            Amplify.Auth.fetchAuthSession(
                { result ->
                    Log.d(TAG, "🔑 Auth session result: isSignedIn=${result.isSignedIn}")
                    Log.d(TAG, "🔑 Auth session class: ${result.javaClass.simpleName}")
                    
                    if (result.isSignedIn) {
                        try {
                            Log.d(TAG, "🔑 User is signed in, extracting JWT token...")
                            
                            // Try to get the JWT token using a more flexible approach
                            var accessToken: String? = null
                            
                            // Method 1: Try to cast to AWSCognitoAuthSession directly
                            try {
                                val cognitoSession = result as? Any
                                if (cognitoSession != null) {
                                    Log.d(TAG, "🔑 Attempting direct method access...")
                                    
                                    // Try to get user pool tokens using reflection with the actual class
                                    val getTokensMethod = cognitoSession.javaClass.getMethod("getUserPoolTokens")
                                    val tokensResult = getTokensMethod.invoke(cognitoSession)
                                    
                                    if (tokensResult != null) {
                                        Log.d(TAG, "🔑 Got tokens result: ${tokensResult.javaClass.simpleName}")
                                        
                                        // Try to get the value
                                        val getValueMethod = tokensResult.javaClass.getMethod("getValue")
                                        val tokens = getValueMethod.invoke(tokensResult)
                                        
                                        if (tokens != null) {
                                            Log.d(TAG, "🔑 Got tokens value: ${tokens.javaClass.simpleName}")
                                            
                                            // Try to get the access token
                                            val getAccessTokenMethod = tokens.javaClass.getMethod("getAccessToken")
                                            accessToken = getAccessTokenMethod.invoke(tokens) as? String
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                Log.w(TAG, "🔑 Direct method access failed: ${e.message}")
                            }
                            
                            // Method 2: Try using the session result directly
                            if (accessToken == null) {
                                try {
                                    Log.d(TAG, "🔑 Attempting alternative token extraction...")
                                    
                                    // Try to access the session properties directly
                                    val fields = result.javaClass.declaredFields
                                    for (field in fields) {
                                        field.isAccessible = true
                                        val value = field.get(result)
                                        Log.d(TAG, "🔑 Field: ${field.name} = ${value?.javaClass?.simpleName}")
                                    }
                                    
                                    // Try common method names
                                    val methodNames = listOf("getUserPoolTokens", "getTokens", "getAccessToken")
                                    for (methodName in methodNames) {
                                        try {
                                            val method = result.javaClass.getMethod(methodName)
                                            val result = method.invoke(result)
                                            Log.d(TAG, "🔑 Method $methodName returned: ${result?.javaClass?.simpleName}")
                                            
                                            if (result != null && result is String && result.startsWith("eyJ")) {
                                                accessToken = result
                                                Log.d(TAG, "🔑 Found JWT token via $methodName")
                                                break
                                            }
                                        } catch (e: Exception) {
                                            // Method doesn't exist, continue
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.w(TAG, "🔑 Alternative extraction failed: ${e.message}")
                                }
                            }
                            
                            if (accessToken != null) {
                                Log.d(TAG, "🔑 JWT token retrieved successfully: ${accessToken.take(20)}...")
                                Log.d(TAG, "🔑 Token length: ${accessToken.length}")
                            } else {
                                Log.e(TAG, "❌ JWT token is null - could not extract token from session")
                                Log.e(TAG, "🔍 Available methods on session:")
                                result.javaClass.methods.forEach { method ->
                                    Log.e(TAG, "🔍 Method: ${method.name}")
                                }
                            }
                            continuation.resume(accessToken)
                        } catch (e: Exception) {
                            Log.e(TAG, "❌ Failed to extract JWT token", e)
                            Log.e(TAG, "❌ Exception details: ${e.message}")
                            continuation.resume(null)
                        }
                    } else {
                        Log.w(TAG, "⚠️ User is not signed in - no JWT token available")
                        continuation.resume(null)
                    }
                },
                { error ->
                    Log.e(TAG, "❌ Failed to fetch auth session", error)
                    Log.e(TAG, "❌ Error details: ${error.message}")
                    continuation.resume(null)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception in getCurrentJwtToken", e)
            Log.e(TAG, "❌ Exception details: ${e.message}")
            continuation.resume(null)
        }
    }
    
    /**
     * Get the OkHttp client for use with ExoPlayer
     * This ensures the same cookies are used for HLS segment requests
     */
    fun okHttpClient(): OkHttpClient = okClient
}

