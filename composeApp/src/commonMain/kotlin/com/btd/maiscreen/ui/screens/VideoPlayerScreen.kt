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
    var showReportDialog by remember { mutableStateOf(false) }
    
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
        
        // Top controls overlay
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Close button (top-left)
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
            
            // Report/Flag button (top-right)
            IconButton(
                onClick = {
                    println("🚩 Report button clicked!")
                    showReportDialog = true
                },
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        Color.Black.copy(alpha = 0.9f),
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            ) {
                Text(
                    text = "🚩",
                    fontSize = 28.sp
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
        
        // Report Dialog
        if (showReportDialog) {
            ReportVideoDialog(
                contentTitle = content.title,
                onDismiss = { showReportDialog = false },
                onReportSubmit = { reason, details ->
                    println("🚩 Report submitted for '${content.title}': $reason - $details")
                    // TODO: Send report to backend
                    showReportDialog = false
                }
            )
        }
    }
}

@Composable
private fun ReportVideoDialog(
    contentTitle: String,
    onDismiss: () -> Unit,
    onReportSubmit: (reason: String, details: String) -> Unit
) {
    var selectedReason by remember { mutableStateOf("") }
    var additionalDetails by remember { mutableStateOf("") }
    
    val reportReasons = listOf(
        "Inappropriate Content",
        "Copyright Violation",
        "Misleading or Spam",
        "Violence or Harmful Content",
        "Hate Speech",
        "Other"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🚩",
                    fontSize = 24.sp
                )
                Text(
                    text = "Report Video",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Report: \"$contentTitle\"",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = "Why are you reporting this video?",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                
                // Report reasons
                reportReasons.forEach { reason ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReason = reason }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = selectedReason == reason,
                            onClick = { selectedReason = reason }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = reason,
                            fontSize = 14.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Additional details
                OutlinedTextField(
                    value = additionalDetails,
                    onValueChange = { additionalDetails = it },
                    label = { Text("Additional details (optional)") },
                    placeholder = { Text("Provide more information...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedReason.isNotEmpty()) {
                        onReportSubmit(selectedReason, additionalDetails)
                    }
                },
                enabled = selectedReason.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFDC2626)
                )
            ) {
                Text("Submit Report")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
expect fun VideoPlayerContent(
    content: ContentItem,
    onPlayerReady: () -> Unit,
    onPlayerError: (String) -> Unit
)

@Composable
expect fun OnBackPressed(onBackClick: () -> Unit)
