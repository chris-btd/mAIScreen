package com.btd.maiscreen.streaming

/**
 * Factory for creating platform-specific PlaybackApi instances
 */
expect object PlaybackApiFactory {
    fun create(config: PlaybackConfig): PlaybackApi
}

