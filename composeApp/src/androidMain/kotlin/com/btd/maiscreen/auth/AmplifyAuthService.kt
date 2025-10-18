package com.btd.maiscreen.auth

import android.util.Log
import com.amplifyframework.auth.AuthUserAttribute
import com.amplifyframework.auth.AuthUserAttributeKey
import com.amplifyframework.core.Amplify

/**
 * AWS Amplify Authentication Service
 * Handles user sign-up, confirmation, and sign-in using Cognito User Pools
 */
class AmplifyAuthService {
    
    companion object {
        private const val TAG = "AmplifyAuthService"
    }
    
    // Callbacks for UI
    var onSignUpSuccess: ((username: String, isSignUpComplete: Boolean) -> Unit)? = null
    var onSignUpError: ((error: String) -> Unit)? = null
    var onConfirmationSuccess: (() -> Unit)? = null
    var onConfirmationError: ((error: String) -> Unit)? = null
    var onSignInSuccess: (() -> Unit)? = null
    var onSignInError: ((error: String) -> Unit)? = null
    var onAuthCheckSuccess: ((email: String?) -> Unit)? = null
    var onAuthCheckError: (() -> Unit)? = null
    
    /**
     * Sign up a new user with email and password
     */
    fun signUp(firstName: String, lastName: String, email: String, password: String, birthdate: String) {
        Log.d(TAG, "🚀 Starting sign up for: $email")
        
        val attributes = listOf(
            AuthUserAttribute(AuthUserAttributeKey.email(), email),
            AuthUserAttribute(AuthUserAttributeKey.givenName(), firstName),
            AuthUserAttribute(AuthUserAttributeKey.familyName(), lastName),
            AuthUserAttribute(AuthUserAttributeKey.birthdate(), birthdate)
        )
        
        val options = com.amplifyframework.auth.options.AuthSignUpOptions.builder()
            .userAttributes(attributes)
            .build()
            
        Amplify.Auth.signUp(
            email, // Use email as username
            password,
            options,
            { result ->
                Log.d(TAG, "✅ Sign up succeeded for: $email")
                Log.d(TAG, "Sign up complete: ${result.isSignUpComplete}")
                Log.d(TAG, "Next step: ${result.nextStep}")
                
                onSignUpSuccess?.invoke(email, result.isSignUpComplete)
            },
            { error ->
                Log.e(TAG, "❌ Sign up failed for: $email", error)
                onSignUpError?.invoke(error.message ?: "Sign up failed")
            }
        )
    }
    
    /**
     * Confirm sign up with verification code
     */
    fun confirmSignUp(username: String, confirmationCode: String) {
        Log.d(TAG, "🔐 Confirming sign up for: $username with code: $confirmationCode")
        
        Amplify.Auth.confirmSignUp(
            username,
            confirmationCode,
            { result ->
                Log.d(TAG, "✅ Confirm sign up succeeded for: $username")
                Log.d(TAG, "Sign up complete: ${result.isSignUpComplete}")
                
                onConfirmationSuccess?.invoke()
            },
            { error ->
                Log.e(TAG, "❌ Confirm sign up failed for: $username", error)
                onConfirmationError?.invoke(error.message ?: "Confirmation failed")
            }
        )
    }
    
    /**
     * Resend confirmation code
     */
    fun resendConfirmationCode(username: String) {
        Log.d(TAG, "📧 Resending confirmation code for: $username")
        
        Amplify.Auth.resendSignUpCode(
            username,
            { result ->
                Log.d(TAG, "✅ Confirmation code resent to: ${result.destination}")
            },
            { error ->
                Log.e(TAG, "❌ Failed to resend confirmation code", error)
                onSignUpError?.invoke("Failed to resend code: ${error.message}")
            }
        )
    }
    
    /**
     * Sign in with email and password
     */
    fun signIn(email: String, password: String) {
        Log.d(TAG, "🔑 Signing in: $email")
        
        Amplify.Auth.signIn(
            email,
            password,
            { result ->
                Log.d(TAG, "✅ Sign in succeeded for: $email")
                Log.d(TAG, "Sign in complete: ${result.isSignedIn}")
                
                if (result.isSignedIn) {
                    onSignInSuccess?.invoke()
                } else {
                    Log.d(TAG, "Additional steps required: ${result.nextStep}")
                    // Handle MFA or other additional steps if needed
                    onSignInError?.invoke("Additional authentication steps required")
                }
            },
            { error ->
                Log.e(TAG, "❌ Sign in failed for: $email", error)
                onSignInError?.invoke(error.message ?: "Sign in failed")
            }
        )
    }
    
    /**
     * Sign out current user
     */
    fun signOut() {
        Log.d(TAG, "🚪 Signing out")
        
        Amplify.Auth.signOut(
            {
                Log.d(TAG, "✅ Sign out succeeded")
            }
        )
    }

    /**
     * Sign out from all devices (global sign out)
     */
    fun globalSignOut() {
        Log.d(TAG, "🚪 Global sign out - signing out from all devices")
        
//        Amplify.Auth.signOut(
//            com.amplifyframework.auth.options.AuthSignOutOptions.builder()
//                .globalSignOut(true)
//                .build(),
//            {
//                Log.d(TAG, "✅ Global sign out succeeded")
//            },
//            { error ->
//                Log.e(TAG, "❌ Global sign out failed", error)
//            }
//        )
    }
    
    /**
     * Check if user is currently signed in
     */
    fun isSignedIn(callback: (Boolean) -> Unit) {
        Amplify.Auth.fetchAuthSession(
            { result ->
                val isSignedIn = result.isSignedIn
                Log.d(TAG, "Auth session check - Signed in: $isSignedIn")
                callback(isSignedIn)
            },
            { error ->
                Log.e(TAG, "Failed to fetch auth session", error)
                callback(false)
            }
        )
    }
    
    /**
     * Get current user details
     */
    fun getCurrentUser() {
        Amplify.Auth.getCurrentUser(
            { user ->
                Log.d(TAG, "Current user: ${user.username}")
            },
            { error ->
                Log.e(TAG, "Failed to get current user", error)
            }
        )
    }
    
    /**
     * Check if user is currently authenticated and get their email
     * This method is used during app startup to determine navigation flow
     */
    fun checkAuthenticationStatus() {
        Log.d(TAG, "🔍 Checking authentication status...")
        
        Amplify.Auth.fetchAuthSession(
            { result ->
                if (result.isSignedIn) {
                    Log.d(TAG, "✅ User is authenticated")
                    // Get current user details to retrieve email
                    Amplify.Auth.getCurrentUser(
                        { user ->
                            Log.d(TAG, "Current authenticated user: ${user.username}")
                            onAuthCheckSuccess?.invoke(user.username)
                        },
                        { error ->
                            Log.e(TAG, "Failed to get current user details", error)
                            onAuthCheckSuccess?.invoke(null)
                        }
                    )
                } else {
                    Log.d(TAG, "❌ User is not authenticated")
                    onAuthCheckError?.invoke()
                }
            },
            { error ->
                Log.e(TAG, "Failed to fetch auth session", error)
                onAuthCheckError?.invoke()
            }
        )
    }
}
