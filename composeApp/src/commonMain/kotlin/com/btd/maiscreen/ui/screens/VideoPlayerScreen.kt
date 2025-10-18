package com.btd.maiscreen.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(
    content: ContentItem,
    onBackClick: () -> Unit = {},
    onPlayerError: (String) -> Unit = {}
) {
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Handle system back button to prevent app exit on Android
    OnBackPressed(onBackClick)
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Video Player
        VideoPlayerContent(
            content = content,
            onPlayerReady = { isLoading = false },
            onPlayerError = { error ->
                errorMessage = error
                isLoading = false
                onPlayerError(error)
            }
        )
        
        // Close button overlay (top-left) - Larger and more accessible
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            IconButton(
                onClick = {
                    println("🔙 Close button clicked!")
                    onBackClick()
                },
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        Color.Black.copy(alpha = 0.9f),
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        println("🔙 Close button clicked (clickable)!")
                        onBackClick()
                    }
            ) {
                Text(
                    text = "✕",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // Loading indicator
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
            ) {
                // Close button on loading screen too
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                ) {
                    IconButton(
                        onClick = {
                            println("🔙 Close button clicked (from loading)!")
                            onBackClick()
                        },
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                Color.Black.copy(alpha = 0.9f),
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                println("🔙 Close button clicked (loading clickable)!")
                                onBackClick()
                            }
                    ) {
                        Text(
                            text = "✕",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Loading content in center
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Loading ${content.title}...",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }
        
        // Error message
        errorMessage?.let { error ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "⚠️",
                        fontSize = 48.sp
                    )
                    Text(
                        text = "Playback Error",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = error,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                    Button(
                        onClick = onBackClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Go Back")
                    }
                }
            }
        }
    }
}

@Composable
expect fun VideoPlayerContent(
    content: ContentItem,
    onPlayerReady: () -> Unit,
    onPlayerError: (String) -> Unit
)

@Composable
expect fun OnBackPressed(onBackClick: () -> Unit)
