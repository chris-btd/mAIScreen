package com.btd.maiscreen.auth

import com.amplifyframework.auth.AuthUserAttribute
import com.amplifyframework.auth.AuthUserAttributeKey
import com.amplifyframework.core.Amplify
import com.btd.maiscreen.util.SecureLogger

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
        SecureLogger.d(TAG, "Starting sign up")

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
                SecureLogger.d(TAG, "Sign up succeeded")

                onSignUpSuccess?.invoke(email, result.isSignUpComplete)
            },
            { error ->
                SecureLogger.e(TAG, "Sign up failed", error)
                onSignUpError?.invoke(error.message ?: "Sign up failed")
            }
        )
    }
    
    /**
     * Confirm sign up with verification code
     */
    fun confirmSignUp(username: String, confirmationCode: String) {
        SecureLogger.d(TAG, "Confirming sign up")

        Amplify.Auth.confirmSignUp(
            username,
            confirmationCode,
            { result ->
                SecureLogger.d(TAG, "Confirm sign up succeeded")

                onConfirmationSuccess?.invoke()
            },
            { error ->
                SecureLogger.e(TAG, "Confirm sign up failed", error)
                onConfirmationError?.invoke(error.message ?: "Confirmation failed")
            }
        )
    }
    
    /**
     * Resend confirmation code
     */
    fun resendConfirmationCode(username: String) {
        SecureLogger.d(TAG,"📧 Resending confirmation code for: $username")
        
        Amplify.Auth.resendSignUpCode(
            username,
            { result ->
                SecureLogger.d(TAG,"✅ Confirmation code resent to: ${result.destination}")
            },
            { error ->
                SecureLogger.e(TAG,"❌ Failed to resend confirmation code", error)
                onSignUpError?.invoke("Failed to resend code: ${error.message}")
            }
        )
    }
    
    /**
     * Sign in with email and password
     */
    fun signIn(email: String, password: String) {
        SecureLogger.d(TAG,"🔑 Signing in: $email")
        
        Amplify.Auth.signIn(
            email,
            password,
            { result ->
                SecureLogger.d(TAG,"✅ Sign in succeeded for: $email")
                SecureLogger.d(TAG,"Sign in complete: ${result.isSignedIn}")
                
                if (result.isSignedIn) {
                    onSignInSuccess?.invoke()
                } else {
                    SecureLogger.d(TAG,"Additional steps required: ${result.nextStep}")
                    // Handle MFA or other additional steps if needed
                    onSignInError?.invoke("Additional authentication steps required")
                }
            },
            { error ->
                SecureLogger.e(TAG,"❌ Sign in failed for: $email", error)
                onSignInError?.invoke(error.message ?: "Sign in failed")
            }
        )
    }
    
    /**
     * Sign out current user
     */
    fun signOut() {
        SecureLogger.d(TAG,"🚪 Signing out")
        
        Amplify.Auth.signOut(
            {
                SecureLogger.d(TAG,"✅ Sign out succeeded")
            }
        )
    }

    /**
     * Sign out from all devices (global sign out)
     */
    fun globalSignOut() {
        SecureLogger.d(TAG,"🚪 Global sign out - signing out from all devices")
        
//        Amplify.Auth.signOut(
//            com.amplifyframework.auth.options.AuthSignOutOptions.builder()
//                .globalSignOut(true)
//                .build(),
//            {
//                SecureLogger.d(TAG,"✅ Global sign out succeeded")
//            },
//            { error ->
//                SecureLogger.e(TAG,"❌ Global sign out failed", error)
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
                SecureLogger.d(TAG,"Auth session check - Signed in: $isSignedIn")
                callback(isSignedIn)
            },
            { error ->
                SecureLogger.e(TAG,"Failed to fetch auth session", error)
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
                SecureLogger.d(TAG,"Current user: ${user.username}")
            },
            { error ->
                SecureLogger.e(TAG,"Failed to get current user", error)
            }
        )
    }
    
    /**
     * Check if user is currently authenticated and get their email
     * This method is used during app startup to determine navigation flow
     */
    fun checkAuthenticationStatus() {
        SecureLogger.d(TAG,"🔍 Checking authentication status...")
        
        Amplify.Auth.fetchAuthSession(
            { result ->
                if (result.isSignedIn) {
                    SecureLogger.d(TAG,"✅ User is authenticated")
                    // Get current user details to retrieve email
                    Amplify.Auth.getCurrentUser(
                        { user ->
                            SecureLogger.d(TAG,"Current authenticated user: ${user.username}")
                            onAuthCheckSuccess?.invoke(user.username)
                        },
                        { error ->
                            SecureLogger.e(TAG,"Failed to get current user details", error)
                            onAuthCheckSuccess?.invoke(null)
                        }
                    )
                } else {
                    SecureLogger.d(TAG,"❌ User is not authenticated")
                    onAuthCheckError?.invoke()
                }
            },
            { error ->
                SecureLogger.e(TAG,"Failed to fetch auth session", error)
                onAuthCheckError?.invoke()
            }
        )
    }
}
