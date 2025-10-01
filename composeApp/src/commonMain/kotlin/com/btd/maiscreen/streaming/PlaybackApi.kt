package com.btd.maiscreen.streaming

/**
 * Playback API interface for HLS streaming
 * Handles CloudFront signed cookies for secure video streaming
 */
interface PlaybackApi {
    /**
     * Get HLS manifest URL for an asset
     * @param assetId The asset identifier
     * @param tier The subscription tier (free, premium, etc.)
     * @param mode The playback mode (video or audio)
     * @return The HLS manifest URL with CloudFront signed cookies set
     */
    suspend fun getManifest(
        assetId: String, 
        tier: String = "free", 
        mode: String = "video"
    ): PlaybackResult<String>
}

/**
 * Result wrapper for playback operations
 */
sealed class PlaybackResult<out T>
data class PlaybackSuccess<out T>(val data: T) : PlaybackResult<T>()
data class PlaybackError(val message: String, val exception: Throwable? = null) : PlaybackResult<Nothing>()

/**
 * Playback configuration
 */
data class PlaybackConfig(
    val cloudFrontDomain: String,
    val defaultTier: String = "free"
)

