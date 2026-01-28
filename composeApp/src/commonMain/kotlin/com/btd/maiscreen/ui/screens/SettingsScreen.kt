package com.btd.maiscreen.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.btd.maiscreen.config.AppSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onTogglePlaybackMode: (Boolean) -> Unit = {},
    isLocalPlaybackEnabled: Boolean = false,
    onPurchaseMinutesClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    // Observe test data setting - initialize from AppSettings
    var useTestData by remember { mutableStateOf(AppSettings.useTestData) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // Top Navigation Bar
        TopAppBar(
            title = {
                Text(
                    text = "Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            navigationIcon = {
                TextButton(onClick = onBackClick) {
                    Text(
                        text = "← Back",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF1E3A8A)
            )
        )
        
        // Settings Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Playback Mode Section
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Video Playback Mode",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3A8A)
                    )
                    
                    Text(
                        text = "Toggle between local test videos and AWS streaming",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Playback Mode Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Use Local Videos",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                            Text(
                                text = if (isLocalPlaybackEnabled) {
                                    "✅ Playing test videos from device"
                                } else {
                                    "☁️ Streaming from AWS CloudFront"
                                },
                                fontSize = 12.sp,
                                color = if (isLocalPlaybackEnabled) Color(0xFF059669) else Color(0xFF1E3A8A)
                            )
                        }
                        
                        Switch(
                            checked = isLocalPlaybackEnabled,
                            onCheckedChange = onTogglePlaybackMode,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF059669), // Green when ON (local)
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFF1E3A8A) // Blue when OFF (AWS)
                            )
                        )
                    }
                    
                    // Info box
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isLocalPlaybackEnabled) 
                                Color(0xFFECFDF5) else Color(0xFFEFF6FF) // Green tint for local, blue tint for AWS
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isLocalPlaybackEnabled) "📱" else "☁️",
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = if (isLocalPlaybackEnabled) "Local Mode Active" else "AWS Streaming Mode",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isLocalPlaybackEnabled) Color(0xFF065F46) else Color(0xFF1E3A8A)
                                )
                            }
                            Text(
                                text = if (isLocalPlaybackEnabled) {
                                    "• Plays demo videos instantly\n• No internet required\n• Perfect for testing UI\n• Uses Big Buck Bunny, Elephants Dream, etc."
                                } else {
                                    "• Streams from AWS CloudFront\n• Requires authentication\n• Tests your backend integration\n• Uses signed cookies for security"
                                },
                                fontSize = 12.sp,
                                color = Color(0xFF374151),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
            
            // Content Data Source Section (Dev builds)
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Content Data Source",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3A8A)
                    )
                    
                    Text(
                        text = "Controlled by build configuration for security (Debug = Test, Release = AWS)",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Data Source Status (Read-only - controlled by build configuration)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Test Data Mode",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                            Text(
                                text = if (useTestData) {
                                    "🧪 Using hardcoded test content (Debug Build)"
                                } else {
                                    "☁️ Fetching from AWS API (Release Build)"
                                },
                                fontSize = 12.sp,
                                color = if (useTestData) Color(0xFF059669) else Color(0xFF1E3A8A)
                            )
                        }

                        // Switch is now disabled - test mode controlled by build configuration
                        Switch(
                            checked = useTestData,
                            onCheckedChange = { },  // No-op: cannot be changed at runtime
                            enabled = false,  // SECURITY: Disabled to prevent runtime changes
                            colors = SwitchDefaults.colors(
                                disabledCheckedThumbColor = Color.White,
                                disabledCheckedTrackColor = Color(0xFF059669), // Green when ON (test data)
                                disabledUncheckedThumbColor = Color.White,
                                disabledUncheckedTrackColor = Color(0xFF1E3A8A) // Blue when OFF (AWS)
                            )
                        )
                    }
                    
                    // Info box
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (useTestData) 
                                Color(0xFFECFDF5) else Color(0xFFEFF6FF) // Green tint for test, blue tint for AWS
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (useTestData) "🧪" else "☁️",
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = if (useTestData) "Test Data Mode" else "AWS API Mode",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (useTestData) Color(0xFF065F46) else Color(0xFF1E3A8A)
                                )
                            }
                            Text(
                                text = if (useTestData) {
                                    "• Uses hardcoded test content\n• No network calls required\n• Perfect for UI testing\n• Fast and predictable data\n• 🔒 Secured: Only in debug builds"
                                } else {
                                    "• Fetches content from AWS API\n• Requires backend integration\n• Tests real API endpoints\n• Updates with real data changes\n• 🔒 Production mode: Test content disabled"
                                },
                                fontSize = 12.sp,
                                color = Color(0xFF374151),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
            
            // Purchase Minutes Section
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Submission Minutes",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3A8A)
                    )
                    
                    Text(
                        text = "Get more time to submit your AI content",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Purchase button
                    Button(
                        onClick = onPurchaseMinutesClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFD700),
                            contentColor = Color(0xFF1E3A8A)
                        )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "💰",
                                fontSize = 20.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "Purchase Additional Minutes",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    // Info text
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF3F4F6)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ℹ️",
                                fontSize = 16.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "Purchase minutes never expire and can be used anytime to submit your content.",
                                fontSize = 12.sp,
                                color = Color(0xFF374151),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
            
            // Other Settings Sections (placeholder)
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Account",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3A8A)
                    )
                    
                    Text(
                        text = "Manage your account settings",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    
                    // Placeholder for future account settings
                    Text(
                        text = "Coming soon...",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
            
            // Account Actions Section
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Account Actions",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3A8A)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Logout option
                    Text(
                        text = "Logout",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFDC2626),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onLogoutClick)
                            .padding(vertical = 12.dp)
                    )
                    
                    Text(
                        text = "Sign out of your account",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            
            // About Section
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "About",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3A8A)
                    )
                    
                    Text(
                        text = "mAIScreen v1.0.0",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    
                    Text(
                        text = "Streaming platform for content creators",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}
