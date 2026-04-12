package com.btd.maiscreen.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random
import com.btd.maiscreen.ui.components.ErrorDialog

@Composable
fun SignUpScreen(
    onSignUpClick: (firstName: String, lastName: String, email: String, password: String, birthdate: String) -> Unit,
    onBackToLoginClick: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onErrorDismiss: () -> Unit = {}
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var confirmEmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var birthdate by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    // Function to format error messages
    fun formatErrorMessage(error: String): String {
        return when {
            error.contains("Password did not conform with policy") -> {
                when {
                    error.contains("Password not long enough") -> "Password must be at least 8 characters long"
                    error.contains("Password must have uppercase characters") -> "Password must contain uppercase letters"
                    error.contains("Password must have lowercase characters") -> "Password must contain lowercase letters"
                    error.contains("Password must have numeric characters") -> "Password must contain numbers"
                    error.contains("Password must have symbol characters") -> "Password must contain special characters"
                    else -> "Password does not meet security requirements"
                }
            }
            error.contains("UsernameExistsException") -> "An account with this email already exists"
            error.contains("InvalidParameterException") && error.contains("birthdate") -> "Please enter a valid birthdate in MM/DD/YYYY format"
            error.contains("InvalidParameterException") -> "Please check your email format"
            error.contains("SignUp is not permitted") -> "Sign-up is currently disabled. Please contact support."
            else -> "Sign-up failed: ${error.take(100)}..."
        }
    }
    
    // Birthdate validation function
    fun isValidBirthdate(date: String): Boolean {
        if (date.isBlank()) return false
        
        // Check MM/DD/YYYY format
        val regex = Regex("""^(0[1-9]|1[0-2])/(0[1-9]|[12]\d|3[01])/\d{4}$""")
        if (!regex.matches(date)) return false
        
        // Parse and validate date
        val parts = date.split("/")
        if (parts.size != 3) return false
        
        val month = parts[0].toIntOrNull() ?: return false
        val day = parts[1].toIntOrNull() ?: return false
        val year = parts[2].toIntOrNull() ?: return false
        
        // Basic validation
        if (month !in 1..12) return false
        if (day !in 1..31) return false
        if (year < 1900 || year > 2024) return false
        
        return true
    }
    
    // Form validation
    val isFormValid = firstName.isNotBlank() && 
                     lastName.isNotBlank() && 
                     email.isNotBlank() && 
                     confirmEmail.isNotBlank() && 
                     password.isNotBlank() && 
                     confirmPassword.isNotBlank() &&
                     birthdate.isNotBlank() &&
                     email == confirmEmail &&
                     password == confirmPassword &&
                     password.length >= 8 &&
                     isValidBirthdate(birthdate)
    
    val emailsMatch = email == confirmEmail || confirmEmail.isEmpty()
    val passwordsMatch = password == confirmPassword || confirmPassword.isEmpty()
    
    // Define colors
    val navyBlue = Color(0xFF1B2951)
    val black = Color(0xFF000000)
    val starWhite = Color(0xFFFFFFFF)
    
    // Generate random stars — size and alpha are remembered here so they don't
    // re-randomise on every Canvas draw, preventing visual flicker.
    // Each entry: [x, y, size, alpha]
    val stars = remember {
        List(80) {
            floatArrayOf(
                Random.nextFloat(),
                Random.nextFloat() * 0.4f,
                Random.nextFloat() * 3f + 1.5f,
                Random.nextFloat() * 0.8f + 0.2f
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
            
            // Draw stars using pre-computed size/alpha to avoid per-frame randomisation
            stars.forEach { star ->
                drawCircle(
                    color = starWhite.copy(alpha = star[3]),
                    radius = star[2],
                    center = Offset(x = star[0] * size.width, y = star[1] * size.height)
                )
            }
        }
        
        // Back Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp)
        ) {
            TextButton(
                onClick = onBackToLoginClick,
                enabled = !isLoading
            ) {
                Text(
                    text = "← Back",
                    fontSize = 17.sp,
                    color = Color.White
                )
            }
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            // App Title
            Text(
                text = "Create Account",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Join mAIScreen today",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // First Name Input
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("First Name", color = Color.White.copy(alpha = 0.7f)) },
                placeholder = { Text("Enter your first name", color = Color.White.copy(alpha = 0.5f)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    cursorColor = Color.White
                )
            )
            
            // Last Name Input
            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Last Name", color = Color.White.copy(alpha = 0.7f)) },
                placeholder = { Text("Enter your last name", color = Color.White.copy(alpha = 0.5f)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    cursorColor = Color.White
                )
            )
            
            // Email Input
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", color = Color.White.copy(alpha = 0.7f)) },
                placeholder = { Text("Enter your email", color = Color.White.copy(alpha = 0.5f)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    cursorColor = Color.White
                )
            )
            
            // Confirm Email Input
            OutlinedTextField(
                value = confirmEmail,
                onValueChange = { confirmEmail = it },
                label = { Text("Confirm Email", color = Color.White.copy(alpha = 0.7f)) },
                placeholder = { Text("Confirm your email", color = Color.White.copy(alpha = 0.5f)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                isError = !emailsMatch,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = if (emailsMatch) Color.White else Color.Red,
                    unfocusedBorderColor = if (emailsMatch) Color.White.copy(alpha = 0.5f) else Color.Red.copy(alpha = 0.7f),
                    cursorColor = Color.White,
                    errorBorderColor = Color.Red,
                    errorTextColor = Color.White
                )
            )
            
            if (!emailsMatch) {
                Text(
                    text = "Emails do not match",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Birthdate Input
            OutlinedTextField(
                value = birthdate,
                onValueChange = { 
                    // Format input as MM/DD/YYYY
                    val filtered = it.filter { char -> char.isDigit() || char == '/' }
                    if (filtered.length <= 10) {
                        birthdate = when (filtered.length) {
                            3 -> if (filtered[2] != '/') "${filtered.substring(0, 2)}/${filtered[2]}" else filtered
                            6 -> if (filtered[5] != '/') "${filtered.substring(0, 5)}/${filtered[5]}" else filtered
                            else -> filtered
                        }
                    }
                },
                label = { Text("Birthdate", color = Color.White.copy(alpha = 0.7f)) },
                placeholder = { Text("MM/DD/YYYY", color = Color.White.copy(alpha = 0.5f)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                isError = birthdate.isNotBlank() && !isValidBirthdate(birthdate),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = if (birthdate.isBlank() || isValidBirthdate(birthdate)) Color.White else Color.Red,
                    unfocusedBorderColor = if (birthdate.isBlank() || isValidBirthdate(birthdate)) Color.White.copy(alpha = 0.5f) else Color.Red.copy(alpha = 0.7f),
                    cursorColor = Color.White,
                    errorBorderColor = Color.Red,
                    errorTextColor = Color.White
                )
            )
            
            // Birthdate validation message
            if (birthdate.isNotBlank() && !isValidBirthdate(birthdate)) {
                Text(
                    text = "Please enter a valid date in MM/DD/YYYY format",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Password Input
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = Color.White.copy(alpha = 0.7f)) },
                placeholder = { Text("Enter your password", color = Color.White.copy(alpha = 0.5f)) },
                trailingIcon = {
                    TextButton(onClick = { passwordVisible = !passwordVisible }) {
                        Text(
                            text = if (passwordVisible) "Hide" else "Show",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    cursorColor = Color.White
                )
            )
            
            if (password.isNotEmpty() && password.length < 8) {
                Text(
                    text = "Password must be at least 8 characters",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Confirm Password Input
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password", color = Color.White.copy(alpha = 0.7f)) },
                placeholder = { Text("Confirm your password", color = Color.White.copy(alpha = 0.5f)) },
                trailingIcon = {
                    TextButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Text(
                            text = if (confirmPasswordVisible) "Hide" else "Show",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                isError = !passwordsMatch,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = if (passwordsMatch) Color.White else Color.Red,
                    unfocusedBorderColor = if (passwordsMatch) Color.White.copy(alpha = 0.5f) else Color.Red.copy(alpha = 0.7f),
                    cursorColor = Color.White,
                    errorBorderColor = Color.Red,
                    errorTextColor = Color.White
                )
            )
            
            if (!passwordsMatch) {
                Text(
                    text = "Passwords do not match",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Sign Up Button
            Button(
                onClick = {
                    if (isFormValid) {
                        onSignUpClick(firstName, lastName, email, password, birthdate)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading && isFormValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = navyBlue,
                    disabledContainerColor = Color.White.copy(alpha = 0.5f),
                    disabledContentColor = navyBlue.copy(alpha = 0.5f)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = navyBlue
                    )
                } else {
                    Text(
                        text = "Create Account",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Back to Login
            TextButton(
                onClick = onBackToLoginClick,
                enabled = !isLoading
            ) {
                Text(
                    text = "Already have an account? Sign In",
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
        }
        
        // Error Dialog
        errorMessage?.let { message ->
            ErrorDialog(
                title = "Sign Up Failed",
                message = formatErrorMessage(message),
                onDismiss = onErrorDismiss,
                onRetry = {
                    // Clear form for retry
                    onErrorDismiss()
                }
            )
        }
    }
}
