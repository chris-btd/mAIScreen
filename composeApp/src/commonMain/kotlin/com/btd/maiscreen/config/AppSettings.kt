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

    /**
     * SECURE: Test mode is now controlled by build configuration, not runtime
     * This prevents users from accessing test content in production builds
     */
    val useTestData: Boolean
        get() = AppConfig.isTestMode

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

    /**
     * DEPRECATED: Test mode is now controlled by build configuration
     * This function is kept for compatibility but has no effect
     */
    @Deprecated(
        "Test mode is now controlled by build configuration and cannot be changed at runtime",
        ReplaceWith("AppConfig.isTestMode"),
        DeprecationLevel.WARNING
    )
    fun toggleTestData() {
        println("⚠️ toggleTestData() is deprecated - test mode is now controlled by build configuration")
        println("📊 Current test mode: ${if (useTestData) "🧪 Test Data" else "☁️ AWS"} (build-time)")
    }
}
