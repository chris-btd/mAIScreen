package com.btd.maiscreen

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.btd.maiscreen.auth.CognitoAuthService
import com.btd.maiscreen.auth.AmplifyAuthService
import com.btd.maiscreen.auth.AppAuthState

class MainActivity : ComponentActivity() {
    
    private lateinit var cognitoAuthService: CognitoAuthService
    private lateinit var amplifyAuthService: AmplifyAuthService
    
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Initialize Cognito authentication service
        cognitoAuthService = CognitoAuthService(this)
        
        // Initialize Amplify authentication service
        amplifyAuthService = AmplifyAuthService()

        // Set the auth services in AuthManager for common access
        com.btd.maiscreen.auth.AuthManager.cognitoAuthService = cognitoAuthService
        com.btd.maiscreen.auth.AuthManager.amplifyAuthService = amplifyAuthService
        
        // Check if user is already authenticated on startup
        checkExistingAuthentication()
        
        // Set up Cognito callbacks
        cognitoAuthService.onAuthSuccess = { tokenResponse ->
            runOnUiThread {
                Toast.makeText(this, "✅ Login successful!", Toast.LENGTH_SHORT).show()
                // Set global authentication state
                AppAuthState.setAuthenticated(true, "user@example.com") // TODO: Extract email from token
            }
        }
        
        cognitoAuthService.onAuthError = { exception ->
            runOnUiThread {
                Toast.makeText(this, "❌ Login failed: ${exception.message}", Toast.LENGTH_LONG).show()
            }
        }
        
        cognitoAuthService.onLogoutComplete = {
            runOnUiThread {
                Toast.makeText(this, "👋 Logged out", Toast.LENGTH_SHORT).show()
                // Clear global authentication state
                AppAuthState.logout()
            }
        }

        setContent {
            App()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        cognitoAuthService.dispose()
    }
    
    private fun checkExistingAuthentication() {
        // Check Amplify auth state first (more reliable for persistent sessions)
        com.amplifyframework.core.Amplify.Auth.fetchAuthSession(
            { result ->
                runOnUiThread {
                    if (result.isSignedIn) {
                        println("🔐 User already authenticated with Amplify")
                        AppAuthState.setAuthenticated(true, "existing_user@example.com")
                    } else {
                        println("👤 No existing authentication found")
                    }
                }
            },
            { error ->
                runOnUiThread {
                    println("⚠️ Failed to check auth session: ${error.message}")
                    // Fallback to Cognito check
                    if (cognitoAuthService.isAuthenticated()) {
                        println("🔐 User authenticated via Cognito fallback")
                        AppAuthState.setAuthenticated(true, "existing_user@example.com")
                    }
                }
            }
        )
    }
    
    fun getCognitoAuthService(): CognitoAuthService = cognitoAuthService
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}