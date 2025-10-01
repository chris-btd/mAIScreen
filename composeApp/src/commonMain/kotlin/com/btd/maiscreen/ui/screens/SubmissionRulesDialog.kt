package com.btd.maiscreen.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlin.random.Random

@Composable
fun SubmissionRulesDialog(
    tierName: String,
    onDismiss: () -> Unit
) {
    val navyBlue = Color(0xFF1B2951)
    val black = Color(0xFF000000)
    val starWhite = Color(0xFFFFFFFF)
    
    // Generate random stars
    val stars = remember {
        List(40) {
            Offset(
                x = Random.nextFloat(),
                y = Random.nextFloat() * 0.3f
            )
        }
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Background with gradient and stars
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
            
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.95f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Submission Rules & Guidelines",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = navyBlue
                        )
                        
                        TextButton(onClick = onDismiss) {
                            Text("Close", color = navyBlue)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Scrollable content
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // What you can submit
                        SectionHeader("What you can submit")
                        BulletPoint("Mini-series — upload all episodes at once as a single submission.")
                        BulletPoint("Feature film — one full-length movie as a single submission.")
                        BulletPoint("Single part — one episode, short, or reel per submission.")
                        BulletPoint("Accepted formats: video (e.g., MP4), audio (e.g., MP3/WAV), and required artwork (poster/thumbnail).")
                        
                        // Duration limits
                        SectionHeader("Duration limits by tier (per submission)")
                        
                        // Tier limits table
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF5F5F5)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                TierLimitRow("Tier", "Max combined duration", isHeader = true)
                                Spacer(modifier = Modifier.height(8.dp))
                                TierLimitRow("Free", "30 minutes")
                                TierLimitRow("Standard", "1 hour 15 minutes")
                                TierLimitRow("Plus", "2 hours 30 minutes")
                            }
                        }
                        
                        InfoText("Combined duration = the total runtime of everything included in that submission (e.g., all episodes in a mini-series submitted together).")
                        InfoText("If you submit one episode/short at a time, each submission must be within your tier's limit.")
                        
                        // Examples
                        SectionHeader("Examples")
                        BulletPoint("6 episodes × 5 minutes = 30 minutes → fits Free (as one submission).")
                        BulletPoint("1 feature = 1h 10m → fits Standard.")
                        BulletPoint("12 shorts × 12 minutes = 144 minutes → needs Plus (2h 24m total).")
                        
                        // Submission form
                        SectionHeader("Submission form (required)")
                        InfoText("Every submission must include a completed form so we can review and (if approved) mint its provenance on the blockchain. You'll be asked for:")
                        BulletPoint("Title, synopsis, genres, and poster/thumbnail")
                        BulletPoint("Episode list (if applicable) and runtimes")
                        BulletPoint("Declared rating and any content warnings")
                        BulletPoint("Prompt origin details (where you created the prompts/models)")
                        BulletPoint("A statement that you own the rights to submit the AI-generated content")
                        
                        // Review process
                        SectionHeader("Review process")
                        BulletPoint("Timeline: Reviews can take up to 7 days.")
                        BulletPoint("Outcome: You'll be notified if your content is accepted or rejected.")
                        BulletPoint("No penalty for rejection: Rejected submissions do not count against your tier's usage limit and can be resubmitted anytime after updates or fixes.")
                        
                        // After approval
                        SectionHeader("After approval")
                        InfoText("Upon acceptance, we record the submission's metadata (including prompt origin and content hashes) on-chain for provenance and ownership. There are no purchases on or from the blockchain.")
                        
                        // Quick tips
                        SectionHeader("Quick tips")
                        BulletPoint("Keep the total runtime of each submission within your tier's limit.")
                        BulletPoint("If you have a long mini-series, submit in parts (each under the limit) or upgrade your tier.")
                        BulletPoint("Fill out the form accurately—missing info can delay review.")
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1B2951),
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun BulletPoint(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "• ",
            fontSize = 14.sp,
            color = Color(0xFF666666),
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color(0xFF333333),
            modifier = Modifier.weight(1f),
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun InfoText(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        color = Color(0xFF555555),
        lineHeight = 20.sp,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun TierLimitRow(tier: String, limit: String, isHeader: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = tier,
            fontSize = 14.sp,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            color = if (isHeader) Color(0xFF1B2951) else Color(0xFF333333),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = limit,
            fontSize = 14.sp,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            color = if (isHeader) Color(0xFF1B2951) else Color(0xFF333333),
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}
