package com.btd.maiscreen.streaming

import com.btd.maiscreen.config.AppConfig

actual object PlaybackApiFactory {
    actual fun create(config: PlaybackConfig): PlaybackApi {
        // For JVM/Desktop, we'll create a simple implementation that returns test URLs
        return object : PlaybackApi {
            override suspend fun getManifest(assetId: String, tier: String, mode: String): PlaybackResult<String> {
                // Return demo videos based on asset ID for desktop testing
                val demoUrl = when (assetId) {
                    "1" -> AppConfig.demoVideo1
                    "2" -> AppConfig.demoVideo2
                    "3" -> AppConfig.demoVideo3
                    else -> AppConfig.demoVideo1 // Default to first demo video
                }
                return PlaybackSuccess(demoUrl)
            }
        }
    }
}

