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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

data class SubscriptionTier(
    val name: String,
    val price: String,
    val priceSubtext: String,
    val deviceLimit: String,
    val features: List<String>,
    val isPopular: Boolean = false,
    val isSelected: Boolean = false
)

@Composable
fun SubscriptionScreen(
    onTierSelected: (tier: SubscriptionTier) -> Unit,
    onContinueClick: () -> Unit,
    onSubmissionRulesClick: (tierName: String) -> Unit = { }
) {
    var selectedTier by remember { mutableStateOf<SubscriptionTier?>(null) }
    var showRulesDialog by remember { mutableStateOf(false) }
    var selectedRulesTier by remember { mutableStateOf("") }
    
    // Define subscription tiers
    val tiers = listOf(
        SubscriptionTier(
            name = "Free",
            price = "$0",
            priceSubtext = "Forever",
            deviceLimit = "30 Minutes",
            features = listOf(
                "Stream to only one device at a time",
                "Community Support",
                "720p and 1080p resolution streaming",
                "The ability to submit up to 30 minutes of your own AI generated content to be featured on our platform. Click rules guide below",
                "⚠️ Free tier minutes do not roll over each month"
            )
        ),
        SubscriptionTier(
            name = "Standard",
            price = "$3.99",
            priceSubtext = "per month",
            deviceLimit = "1 Hour 15 Min",
            features = listOf(
                "Stream on up to 3 devices simultaneously",
                "Community Support",
                "1080p resolution streaming",
                "The ability to submit up to 1 hour and 15 minutes of your own AI generated content to be featured on our platform. Click rules guide below",
                "✨ Unused minutes roll over to the next month!"
            ),
            isPopular = true
        ),
        SubscriptionTier(
            name = "Plus",
            price = "$8.99",
            priceSubtext = "per month",
            deviceLimit = "2 Hours 30 Min",
            features = listOf(
                "Stream on up to 3 devices simultaneously",
                "Community Support",
                "4K resolution streaming",
                "The ability to submit up to 2 hours and 30 minutes of your own AI generated content to be featured on our platform. Click rules guide below",
                "✨ Unused minutes roll over to the next month!"
            )
        )
    )
    
    // Define colors
    val navyBlue = Color(0xFF1B2951)
    val black = Color(0xFF000000)
    val starWhite = Color(0xFFFFFFFF)
    val goldAccent = Color(0xFFFFD700)
    
    // Generate random stars
    val stars = remember {
        List(60) {
            Offset(
                x = Random.nextFloat(),
                y = Random.nextFloat() * 0.3f // Stars only in the top 30% (black area)
            )
        }
    }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
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
                val starSize = Random.nextFloat() * 3f + 1.5f
                drawCircle(
                    color = starWhite.copy(alpha = Random.nextFloat() * 0.8f + 0.2f),
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
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            // Header
            Text(
                text = "Choose Your Plan",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Select the streaming plan that fits your needs",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Subscription Tiers
            tiers.forEach { tier ->
                SubscriptionTierCard(
                    tier = tier,
                    isSelected = selectedTier?.name == tier.name,
                    onTierClick = { 
                        selectedTier = tier
                        onTierSelected(tier)
                    },
                    onSubmissionRulesClick = { 
                        selectedRulesTier = tier.name
                        showRulesDialog = true
                        onSubmissionRulesClick(tier.name) 
                    },
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Content Review Disclaimer
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.05f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Content Review Policy",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Text(
                        text = "All AI-generated content will be reviewed for quality, originality, compliance with our Terms of Service, and ratings accuracy. This ensures the highest quality experience for all users.",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        lineHeight = 16.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Continue Button
            Button(
                onClick = onContinueClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = selectedTier != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = navyBlue,
                    disabledContainerColor = Color.White.copy(alpha = 0.5f),
                    disabledContentColor = navyBlue.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = if (selectedTier != null) "Continue with ${selectedTier!!.name}" else "Select a Plan",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Terms text
            Text(
                text = "You can change or cancel your plan anytime",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
    
    // Show rules dialog
    if (showRulesDialog) {
        SubmissionRulesDialog(
            tierName = selectedRulesTier,
            onDismiss = { showRulesDialog = false }
        )
    }
}

@Composable
private fun SubscriptionTierCard(
    tier: SubscriptionTier,
    isSelected: Boolean,
    onTierClick: () -> Unit,
    onSubmissionRulesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navyBlue = Color(0xFF1B2951)
    val goldAccent = Color(0xFFFFD700)
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onTierClick() }
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = Color.White,
                        shape = RoundedCornerShape(16.dp)
                    )
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 4.dp
        )
    ) {
        Box {
            // Popular badge
            if (tier.isPopular) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(
                            goldAccent,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "MOST POPULAR",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = navyBlue
                    )
                }
            }
            
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Plan name
                Text(
                    text = tier.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Price
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = tier.price,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (tier.priceSubtext.isNotEmpty()) {
                        Text(
                            text = " ${tier.priceSubtext}",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Device limit
                Text(
                    text = tier.deviceLimit,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (tier.isPopular) goldAccent else Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Features
                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tier.features.forEach { feature ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            // Checkmark
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(
                                        Color.White,
                                        RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "✓",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = navyBlue
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Text(
                                text = feature,
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Submission Rules Link
                Text(
                    text = "View Submission Rules & Guidelines",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (tier.isPopular) goldAccent else Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .clickable { onSubmissionRulesClick() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .fillMaxWidth()
                )
                
                if (isSelected) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "✓ Selected",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .background(
                                Color.White.copy(alpha = 0.2f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}
