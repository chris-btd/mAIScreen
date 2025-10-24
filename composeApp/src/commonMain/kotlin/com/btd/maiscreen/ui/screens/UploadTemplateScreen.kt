package com.btd.maiscreen.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadTemplateScreen(
    onBackClick: () -> Unit = {},
    onNextClick: (String, String) -> Unit = { _, _ -> }
) {
    val navyBlue = Color(0xFF1B2951)
    val black = Color(0xFF000000)
    val starWhite = Color(0xFFFFFFFF)
    
    // State for dropdown and radio buttons
    var showTypeDropdown by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf("") }
    var selectedSubType by remember { mutableStateOf("") }
    
    // Generate stars for background
    val stars = remember {
        (1..50).map {
            Pair(Random.nextFloat(), Random.nextFloat())
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
                    center = Offset(
                        x = star.first * size.width,
                        y = star.second * size.height
                    )
                )
            }
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(WindowInsets.statusBars.asPaddingValues())
        ) {
            // Top Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back button
                IconButton(onClick = onBackClick) {
                    Text(
                        text = "←",
                        fontSize = 24.sp,
                        color = Color.White
                    )
                }
                
                Text(
                    text = "Create Upload Template",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                
                // Placeholder for balance
                Spacer(modifier = Modifier.width(48.dp))
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Main Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Header Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.1f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "📤",
                            fontSize = 48.sp
                        )
                        
                        Text(
                            text = "Create Upload Template",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        
                        Text(
                            text = "Choose the type of content you want to upload and select the appropriate category.",
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                // Type Selection Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Content Type",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B2951)
                        )
                        
                        // Type Dropdown
                        Box {
                            OutlinedButton(
                                onClick = { showTypeDropdown = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF1B2951)
                                )
                            ) {
                                Text(
                                    text = selectedType.ifEmpty { "Select Type" },
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Start
                                )
                                Text("▼")
                            }
                            
                            DropdownMenu(
                                expanded = showTypeDropdown,
                                onDismissRequest = { showTypeDropdown = false }
                            ) {
                                listOf("Audio", "Video").forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type) },
                                        onClick = {
                                            selectedType = type
                                            selectedSubType = "" // Reset sub-type when type changes
                                            showTypeDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Sub-Type Selection Card (conditional)
                AnimatedVisibility(
                    visible = selectedType.isNotEmpty(),
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = if (selectedType == "Audio") "Audio Category" else "Video Category",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B2951)
                            )
                            
                            // Sub-type options based on selected type
                            val subTypes = if (selectedType == "Audio") {
                                listOf("Podcast", "AudioBook")
                            } else {
                                listOf("Series", "Movie", "Short Film", "Podcast")
                            }
                            
                            subTypes.forEach { subType ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedSubType = subType }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedSubType == subType,
                                        onClick = { selectedSubType = subType }
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = subType,
                                        fontSize = 16.sp,
                                        color = Color(0xFF1B2951)
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Next Button
                Button(
                    onClick = {
                        if (selectedType.isNotEmpty() && selectedSubType.isNotEmpty()) {
                            onNextClick(selectedType, selectedSubType)
                        }
                    },
                    enabled = selectedType.isNotEmpty() && selectedSubType.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6366F1),
                        disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Next",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                // Back Button
                TextButton(
                    onClick = onBackClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Cancel",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Bottom padding
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
