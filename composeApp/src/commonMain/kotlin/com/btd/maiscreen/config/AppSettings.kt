package com.btd.maiscreen.config

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Global app settings state manager
 */
object AppSettings {
    var isLocalPlaybackEnabled by mutableStateOf(false)
        private set
    
    fun togglePlaybackMode() {
        isLocalPlaybackEnabled = !isLocalPlaybackEnabled
        val mode = if (isLocalPlaybackEnabled) "📱 Local Videos" else "☁️ AWS Streaming"
        println("🎬 Playback mode changed to: $mode")
    }
    
    fun setPlaybackMode(isLocal: Boolean) {
        isLocalPlaybackEnabled = isLocal
        val mode = if (isLocalPlaybackEnabled) "📱 Local Videos" else "☁️ AWS Streaming"
        println("🎬 Playback mode set to: $mode")
    }
}
