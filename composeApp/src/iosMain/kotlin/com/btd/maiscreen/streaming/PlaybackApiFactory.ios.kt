package com.btd.maiscreen.streaming

actual object PlaybackApiFactory {
    actual fun create(config: PlaybackConfig): PlaybackApi {
        return IOSPlaybackApi(config)
    }
}

