package com.btd.maiscreen.streaming

actual object PlaybackApiFactory {
    actual fun create(config: PlaybackConfig): PlaybackApi {
        // For JVM/Desktop, we'll create a simple implementation that returns test URLs
        return object : PlaybackApi {
            override suspend fun getManifest(assetId: String, tier: String, mode: String): PlaybackResult<String> {
                // Return test HLS stream for desktop
                return PlaybackSuccess("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
            }
        }
    }
}

