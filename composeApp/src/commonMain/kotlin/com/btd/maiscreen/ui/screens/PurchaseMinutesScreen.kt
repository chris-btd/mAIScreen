package com.btd.maiscreen.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.btd.maiscreen.model.ProfileUiState
import kotlin.random.Random

data class MinutePackage(
    val id: String,
    val minutes: Int,
    val price: String,
    val priceValue: Double,
    val pricePerMinute: String,
    val isPopular: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseMinutesScreen(
    uiState: ProfileUiState,
    onPackageSelected: (MinutePackage) -> Unit = {},
    onPurchaseClick: (MinutePackage) -> Unit,
    onBackClick: () -> Unit
) {
    var selectedPackage by remember { mutableStateOf<MinutePackage?>(null) }

    
    // Define minute packages based on the provided pricing
    val packages = listOf(
        MinutePackage(
            id = "minutes_30",
            minutes = 30,
            price = "$2.49",
            priceValue = 2.49,
            pricePerMinute = "$0.08/min"
        ),
        MinutePackage(
            id = "minutes_60",
            minutes = 60,
            price = "$4.49",
            priceValue = 4.49,
            pricePerMinute = "$0.07/min"
        ),
        MinutePackage(
            id = "minutes_180",
            minutes = 180,
            price = "$11.99",
            priceValue = 11.99,
            pricePerMinute = "$0.07/min",
            isPopular = true
        ),
        MinutePackage(
            id = "minutes_360",
            minutes = 360,
            price = "$19.99",
            priceValue = 19.99,
            pricePerMinute = "$0.06/min"
        )
    )
    
    // Define colors
    val navyBlue = Color(0xFF1B2951)
    val black = Color(0xFF000000)
    val starWhite = Color(0xFFFFFFFF)
    val goldAccent = Color(0xFFFFD700)
    
    // Generate random stars for background
    val stars = remember {
        List(80) {
            Offset(
                x = Random.nextFloat(),
                y = Random.nextFloat() * 0.4f  // Stars only in top 40%
            )
        }
    }
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background gradient with stars
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gradient = Brush.verticalGradient(
                colors = listOf(black, navyBlue),
                startY = 0f,
                endY = size.height
            )
            drawRect(gradient)
            
            // Draw stars
            stars.forEach { star ->
                val starSize = Random.nextFloat() * 2f + 1f
                drawCircle(
                    color = starWhite.copy(alpha = Random.nextFloat() * 0.3f + 0.1f),
                    radius = starSize,
                    center = Offset(star.x * size.width, star.y * size.height)
                )
            }
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBackClick) {
                    Text(
                        text = "← Back",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Title and current balance
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Purchase Minutes",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Get more time to submit your AI content",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Current balance card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Current Balance",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${uiState.currentMinutes} Minutes",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = goldAccent
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Package cards
            packages.forEach { pkg ->
                MinutePackageCard(
                    pkg = pkg,
                    isSelected = selectedPackage?.id == pkg.id,
                    onClick = {
                        selectedPackage = pkg
                        onPackageSelected(pkg)
                    },
                    navyBlue = navyBlue,
                    goldAccent = goldAccent
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Info box
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ℹ️",
                            fontSize = 20.sp,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Column {
                            Text(
                                text = "About Purchased Minutes",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "• Purchased minutes never expire\n" +
                              "• Use them anytime to submit content\n" +
                              "• Combine with your subscription minutes\n" +
                              "• One-time purchase, no recurring charges",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        lineHeight = 20.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Purchase button
            Button(
                onClick = {
                    selectedPackage?.let { onPurchaseClick(it) }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = selectedPackage != null,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = goldAccent,
                    contentColor = navyBlue,
                    disabledContainerColor = Color.White.copy(alpha = 0.3f),
                    disabledContentColor = Color.White.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = if (selectedPackage != null) {
                        "Purchase ${selectedPackage!!.minutes} Minutes for ${selectedPackage!!.price}"
                    } else {
                        "Select a Package"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun MinutePackageCard(
    pkg: MinutePackage,
    isSelected: Boolean,
    onClick: () -> Unit,
    navyBlue: Color,
    goldAccent: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) goldAccent else Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                Color.White.copy(alpha = 0.15f) 
            else 
                Color.White.copy(alpha = 0.05f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Popular badge
            if (pkg.isPopular) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(
                            goldAccent,
                            shape = RoundedCornerShape(topEnd = 16.dp, bottomStart = 16.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "BEST VALUE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = navyBlue
                    )
                }
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Title and price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${pkg.minutes} Minutes",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = pkg.pricePerMinute,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    
                    Text(
                        text = pkg.price,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) goldAccent else Color.White
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Description
                Text(
                    text = when (pkg.minutes) {
                        30 -> "Perfect for trying out additional minutes"
                        60 -> "Great for occasional content creators"
                        180 -> "Best value for regular submissions"
                        360 -> "Maximum savings for frequent creators"
                        else -> "Add more submission time"
                    },
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    lineHeight = 20.sp
                )
                
                // Selection indicator
                if (isSelected) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✓",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = goldAccent,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Selected",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = goldAccent
                        )
                    }
                }
            }
        }
    }
}

