package com.btd.maiscreen.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
actual fun VideoPlayerContent(
    content: ContentItem,
    onPlayerReady: () -> Unit,
    onPlayerError: (String) -> Unit
) {
    // Desktop placeholder - would integrate with a desktop video player like VLC-J or JavaFX MediaPlayer
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "🎬",
                fontSize = 64.sp
            )
            Text(
                text = "Now Playing",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = content.title,
                color = Color.White,
                fontSize = 18.sp
            )
            Text(
                text = "Desktop video player would be implemented here",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        }
    }
    
    // Simulate player ready
    onPlayerReady()
}

@Composable
actual fun OnBackPressed(onBackClick: () -> Unit) {
    // No-op for desktop - no system back button
}
