package com.btd.maiscreen.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaSubmissionScreen(
    contentType: String, // "Audio" or "Video"
    subType: String, // "Movie", "Series", "Short Film", "Podcast", "AudioBook"
    onBackClick: () -> Unit = {},
    onSubmitClick: (Map<String, String>) -> Unit = {}
) {
    val navyBlue = Color(0xFF1B2951)
    val black = Color(0xFF000000)
    val starWhite = Color(0xFFFFFFFF)
    
    // Form state based on sub-type
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var director by remember { mutableStateOf("") }
    var cast by remember { mutableStateOf("") }
    var episodeCount by remember { mutableStateOf("") }
    var host by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    
    // Generate stars for background — size and alpha are remembered here so they don't
    // re-randomise on every Canvas draw, preventing visual flicker.
    // Each entry: [x, y, size, alpha]
    val stars = remember {
        List(50) {
            floatArrayOf(
                Random.nextFloat(),
                Random.nextFloat(),
                Random.nextFloat() * 2f + 1f,
                Random.nextFloat() * 0.3f + 0.1f
            )
        }
    }
    
    // Get screen title based on sub-type
    val screenTitle = when (subType) {
        "Movie" -> "Movie Submission"
        "Series" -> "Series Submission"
        "Short Film" -> "Short Film Submission"
        "Podcast" -> "Podcast Submission"
        "AudioBook" -> "AudioBook Submission"
        else -> "Media Submission"
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
            
            // Draw stars using pre-computed size/alpha to avoid per-frame randomisation
            stars.forEach { star ->
                drawCircle(
                    color = starWhite.copy(alpha = star[3]),
                    radius = star[2],
                    center = Offset(x = star[0] * size.width, y = star[1] * size.height)
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
                IconButton(onClick = onBackClick) {
                    Text(
                        text = "←",
                        fontSize = 24.sp,
                        color = Color.White
                    )
                }
                
                Text(
                    text = screenTitle,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.width(48.dp))
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Form Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = when (subType) {
                                "Movie" -> "🎬"
                                "Series" -> "📺"
                                "Short Film" -> "🎥"
                                "Podcast" -> "🎙️"
                                "AudioBook" -> "📚"
                                else -> "📤"
                            },
                            fontSize = 48.sp
                        )
                        
                        Text(
                            text = screenTitle,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                // Common Fields
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title *") },
                    placeholder = { Text("Enter title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF6366F1),
                        unfocusedContainerColor = Color(0xFF6366F1)
                    )
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description *") },
                    placeholder = { Text("Enter description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    minLines = 3,
                    maxLines = 5,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF6366F1),
                        unfocusedContainerColor = Color(0xFF6366F1)
                    )
                )

                // Contextual Fields based on sub-type
                when (subType) {
                    "Movie", "Short Film" -> {
                        OutlinedTextField(
                            value = duration,
                            onValueChange = { duration = it },
                            label = { Text("Duration (minutes)") },
                            placeholder = { Text("e.g., 120") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = androidx.compose.material3.TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF6366F1),
                                unfocusedContainerColor = Color.Gray.copy(alpha = 0.5f)
                            )
                        )

                        OutlinedTextField(
                            value = year,
                            onValueChange = { year = it },
                            label = { Text("Release Year") },
                            placeholder = { Text("e.g., 2024") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = androidx.compose.material3.TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF6366F1),
                                unfocusedContainerColor = Color.Gray.copy(alpha = 0.5f)
                            )
                        )

                        OutlinedTextField(
                            value = director,
                            onValueChange = { director = it },
                            label = { Text("Director") },
                            placeholder = { Text("Director name") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = androidx.compose.material3.TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF6366F1),
                                unfocusedContainerColor = Color.Gray.copy(alpha = 0.5f)
                            )
                        )

                        OutlinedTextField(
                            value = cast,
                            onValueChange = { cast = it },
                            label = { Text("Cast") },
                            placeholder = { Text("Actor names (comma separated)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = androidx.compose.material3.TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF6366F1),
                                unfocusedContainerColor = Color.Gray.copy(alpha = 0.5f)
                            )
                        )
                    }

                    "Series" -> {
                        OutlinedTextField(
                            value = episodeCount,
                            onValueChange = { episodeCount = it },
                            label = { Text("Number of Episodes") },
                            placeholder = { Text("e.g., 10") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = androidx.compose.material3.TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF6366F1),
                                unfocusedContainerColor = Color.Gray.copy(alpha = 0.5f)
                            )
                        )

                        OutlinedTextField(
                            value = director,
                            onValueChange = { director = it },
                            label = { Text("Showrunner / Creator") },
                            placeholder = { Text("Creator name") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = androidx.compose.material3.TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF6366F1),
                                unfocusedContainerColor = Color.Gray.copy(alpha = 0.5f)
                            )
                        )

                        OutlinedTextField(
                            value = cast,
                            onValueChange = { cast = it },
                            label = { Text("Main Cast") },
                            placeholder = { Text("Actor names (comma separated)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = androidx.compose.material3.TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF6366F1),
                                unfocusedContainerColor = Color.Gray.copy(alpha = 0.5f)
                            )
                        )

                        OutlinedTextField(
                            value = year,
                            onValueChange = { year = it },
                            label = { Text("Release Year") },
                            placeholder = { Text("e.g., 2024") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = androidx.compose.material3.TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF6366F1),
                                unfocusedContainerColor = Color.Gray.copy(alpha = 0.5f)
                            )
                        )
                    }

                    "Podcast" -> {
                        OutlinedTextField(
                            value = host,
                            onValueChange = { host = it },
                            label = { Text("Host *") },
                            placeholder = { Text("Host name") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = androidx.compose.material3.TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF6366F1),
                                unfocusedContainerColor = Color.Gray.copy(alpha = 0.5f)
                            )
                        )

                        OutlinedTextField(
                            value = duration,
                            onValueChange = { duration = it },
                            label = { Text("Average Episode Duration (minutes)") },
                            placeholder = { Text("e.g., 45") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = androidx.compose.material3.TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF6366F1),
                                unfocusedContainerColor = Color.Gray.copy(alpha = 0.5f)
                            )
                        )

                        OutlinedTextField(
                            value = episodeCount,
                            onValueChange = { episodeCount = it },
                            label = { Text("Number of Episodes") },
                            placeholder = { Text("e.g., 50") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = androidx.compose.material3.TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF6366F1),
                                unfocusedContainerColor = Color.Gray.copy(alpha = 0.5f)
                            )
                        )
                    }

                    "AudioBook" -> {
                        OutlinedTextField(
                            value = author,
                            onValueChange = { author = it },
                            label = { Text("Author *") },
                            placeholder = { Text("Author name") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = androidx.compose.material3.TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF6366F1),
                                unfocusedContainerColor = Color.Gray.copy(alpha = 0.5f)
                            )
                        )

                        OutlinedTextField(
                            value = duration,
                            onValueChange = { duration = it },
                            label = { Text("Duration (hours)") },
                            placeholder = { Text("e.g., 8.5") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = androidx.compose.material3.TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF6366F1),
                                unfocusedContainerColor = Color.Gray.copy(alpha = 0.5f)
                            )
                        )

                        OutlinedTextField(
                            value = year,
                            onValueChange = { year = it },
                            label = { Text("Release Year") },
                            placeholder = { Text("e.g., 2024") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = androidx.compose.material3.TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF6366F1),
                                unfocusedContainerColor = Color.Gray.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Submit Button
                Button(
                    onClick = {
                        val formData = mapOf(
                            "type" to subType,
                            "title" to title,
                            "description" to description,
                            "genre" to genre,
                            "duration" to duration,
                            "year" to year,
                            "director" to director,
                            "cast" to cast,
                            "episodeCount" to episodeCount,
                            "host" to host,
                            "author" to author
                        )
                        onSubmitClick(formData)
                    },
                    enabled = title.isNotEmpty() && description.isNotEmpty(),
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
                        text = "Submit",
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
