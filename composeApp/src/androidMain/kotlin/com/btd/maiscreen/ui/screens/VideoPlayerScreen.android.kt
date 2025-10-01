package com.btd.maiscreen.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.btd.maiscreen.ui.components.VideoPlayerView

@Composable
actual fun VideoPlayerContent(
    content: ContentItem,
    onPlayerReady: () -> Unit,
    onPlayerError: (String) -> Unit
) {
    VideoPlayerView(
        contentItem = content,
        modifier = Modifier.fillMaxSize(),
        autoPlay = true,
        onPlayerReady = onPlayerReady,
        onPlayerError = onPlayerError
    )
}

@Composable
actual fun OnBackPressed(onBackClick: () -> Unit) {
    // Handle system back button to prevent app exit and use our custom navigation
    BackHandler(onBack = onBackClick)
}