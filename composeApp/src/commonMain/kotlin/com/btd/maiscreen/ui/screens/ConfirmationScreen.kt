package com.btd.maiscreen.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmationScreen(
    email: String,
    onConfirmClick: (String) -> Unit,
    onResendClick: () -> Unit,
    onBackClick: () -> Unit,
    onBypassClick: () -> Unit = {}  // Debug-only bypass callback
) {
    var confirmationCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Title
            Text(
                text = "Verify Your Email",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Description
            Text(
                text = "We've sent a verification code to:",
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = email,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Confirmation Code Input
            OutlinedTextField(
                value = confirmationCode,
                onValueChange = { confirmationCode = it },
                label = { Text("Verification Code", color = Color.Gray) },
                placeholder = { Text("Enter 6-digit code", color = Color.Gray) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.Red,
                    unfocusedBorderColor = Color.Gray,
                    cursorColor = Color.Red
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Confirm Button
            Button(
                onClick = {
                    if (confirmationCode.isNotBlank()) {
                        isLoading = true
                        onConfirmClick(confirmationCode)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = confirmationCode.isNotBlank() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = "Verify Email",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Resend Code Button
            TextButton(
                onClick = onResendClick,
                enabled = !isLoading
            ) {
                Text(
                    text = "Didn't receive the code? Resend",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Back Button
            TextButton(
                onClick = onBackClick,
                enabled = !isLoading
            ) {
                Text(
                    text = "← Back to Sign Up",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
            
            // Debug Bypass (only in debug builds)
            if (isDebugBuild()) {
                Spacer(modifier = Modifier.height(24.dp))
                
                // Divider
                HorizontalDivider(
                    color = Color.Gray.copy(alpha = 0.3f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                Text(
                    text = "🔧 DEV MODE",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                TextButton(
                    onClick = onBypassClick,
                    enabled = !isLoading
                ) {
                    Text(
                        text = "Skip Email Verification (Dev Only)",
                        color = Color(0xFFFF6B35), // Orange color for dev
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Text(
                    text = "This bypass is only available in debug builds",
                    fontSize = 10.sp,
                    color = Color.Gray.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// Helper function to check if this is a debug build
@Composable
expect fun isDebugBuild(): Boolean
