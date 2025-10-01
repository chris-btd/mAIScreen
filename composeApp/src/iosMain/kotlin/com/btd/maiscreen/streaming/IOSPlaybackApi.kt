package com.btd.maiscreen.streaming

import com.btd.maiscreen.config.AppConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import platform.Foundation.*
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * iOS implementation of PlaybackApi using NSURLSession with automatic cookie handling
 */
class IOSPlaybackApi(
    private val config: PlaybackConfig
) : PlaybackApi {
    
    private val urlSession = NSURLSession.sharedSession
    
    override suspend fun getManifest(
        assetId: String, 
        tier: String, 
        mode: String
    ): PlaybackResult<String> = withContext(Dispatchers.Main) {
        try {
            val urlString = "https://${config.cloudFrontDomain}/api/play/$assetId?tier=$tier&mode=$mode"
            val url = NSURL.URLWithString(urlString) 
                ?: return@withContext PlaybackError("Invalid URL: $urlString")
            
            val request = NSMutableURLRequest.requestWithURL(url).apply {
                setHTTPMethod("GET")
            }
            
            val result = suspendCoroutine<PlaybackResult<String>> { continuation ->
                val task = urlSession.dataTaskWithRequest(request) { data, response, error ->
                    when {
                        error != null -> {
                            continuation.resume(PlaybackError("Network error: ${error.localizedDescription}"))
                        }
                        data == null -> {
                            continuation.resume(PlaybackError("No data received"))
                        }
                        else -> {
                            try {
                                val httpResponse = response as? NSHTTPURLResponse
                                if (httpResponse?.statusCode != 200L) {
                                    continuation.resume(PlaybackError("HTTP ${httpResponse?.statusCode ?: "unknown"}"))
                                    return@dataTaskWithRequest
                                }
                                
                                val jsonString = NSString.create(data, NSUTF8StringEncoding) as String
                                val json = Json.parseToJsonElement(jsonString).jsonObject
                                val manifestUrl = json["manifestUrl"]?.jsonPrimitive?.content
                                    ?: throw Exception("manifestUrl not found in response")
                                
                                continuation.resume(PlaybackSuccess(manifestUrl))
                            } catch (e: Exception) {
                                continuation.resume(PlaybackError("Failed to parse response: ${e.message}", e))
                            }
                        }
                    }
                }
                task.resume()
            }
            
            result
        } catch (e: Exception) {
            PlaybackError("Failed to get manifest: ${e.message}", e)
        }
    }
}

