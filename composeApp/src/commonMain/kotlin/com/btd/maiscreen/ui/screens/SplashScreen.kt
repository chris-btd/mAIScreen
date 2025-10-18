package com.btd.maiscreen.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit,
    onAuthenticatedUserFound: (userEmail: String?) -> Unit = {}
) {
    var progress by remember { mutableFloatStateOf(0f) }
    
    // Define colors
    val navyBlue = Color(0xFF1B2951)
    val black = Color(0xFF000000)
    val starWhite = Color(0xFFFFFFFF)
    
    // Generate random stars
    val stars = remember {
        List(100) {
            Offset(
                x = Random.nextFloat(),
                y = Random.nextFloat() * 0.6f // Stars only in the top 60% (black area)
            )
        }
    }
    
    // Animate progress
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 2000, easing = EaseInOut),
        label = "progress"
    )
    
    // Animate star twinkle
    val starAlpha by animateFloatAsState(
        targetValue = if ((System.currentTimeMillis() / 1000) % 2 == 0L) 0.8f else 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starTwinkle"
    )
    
    // Start loading animation and check authentication
    LaunchedEffect(Unit) {
        var authCheckCompleted = false
        
        // Start authentication check in parallel with loading animation
        com.btd.maiscreen.auth.AuthManager.checkAuthenticationStatus { isAuthenticated, userEmail ->
            println("🔍 Authentication check result: authenticated=$isAuthenticated, email=$userEmail")
            if (isAuthenticated) {
                println("✅ Authenticated user found during splash: $userEmail")
                onAuthenticatedUserFound(userEmail)
            }
            authCheckCompleted = true
        }
        
        // Simulate loading time with progress animation
        for (i in 0..100) {
            progress = i / 100f
            delay(20)
        }
        
        // Wait for authentication check to complete if it hasn't already
        var waitCount = 0
        while (!authCheckCompleted && waitCount < 50) { // Max wait 1 second (50 * 20ms)
            delay(20)
            waitCount++
        }
        
        delay(500) // Extra delay to show completion
        onSplashComplete()
    }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Background gradient
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gradient = Brush.verticalGradient(
                colors = listOf(black, navyBlue),
                startY = 0f,
                endY = size.height
            )
            drawRect(gradient)
            
            // Draw stars
            stars.forEach { star ->
                val starSize = Random.nextFloat() * 4f + 2f
                drawCircle(
                    color = starWhite.copy(alpha = starAlpha * Random.nextFloat()),
                    radius = starSize,
                    center = Offset(
                        x = star.x * size.width,
                        y = star.y * size.height
                    )
                )
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // App Logo/Title
            Text(
                text = "mAIScreen",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "AI-Powered Screen Intelligence",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Loading Progress
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Progress Bar
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .width(200.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f),
                )
                
                // Progress Text
                Text(
                    text = "Loading... ${(animatedProgress * 100).toInt()}%",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                
                // Authentication status text (for debugging)
                if (animatedProgress > 0.5f) {
                    Text(
                        text = "Checking authentication...",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
