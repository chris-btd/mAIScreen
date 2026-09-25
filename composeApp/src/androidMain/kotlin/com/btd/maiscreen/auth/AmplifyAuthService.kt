package com.btd.maiscreen.auth

import android.util.Log
import com.amplifyframework.auth.AuthUserAttribute
import com.amplifyframework.auth.AuthUserAttributeKey
import com.amplifyframework.auth.result.step.AuthSignInStep
import com.amplifyframework.core.Amplify

/**
 * AWS Amplify Authentication Service
 * Handles user sign-up, confirmation, and sign-in using Cognito User Pools
 */
class AmplifyAuthService {
    
    companion object {
        private const val TAG = "AmplifyAuthService"

        /**
         * Converts the app's MM/DD/YYYY input into the ISO 8601 (YYYY-MM-DD) format
         * Cognito's standard `birthdate` attribute expects. Returns null when blank
         * or malformed, so callers can simply omit the attribute.
         */
        private fun toIsoBirthdate(birthdate: String): String? {
            if (birthdate.isBlank()) return null
            val parts = birthdate.split("/")
            if (parts.size != 3) return null
            val (month, day, year) = parts
            if (month.length != 2 || day.length != 2 || year.length != 4) return null
            return "$year-$month-$day"
        }
    }
    
    // Callbacks for UI
    var onSignUpSuccess: ((username: String, isSignUpComplete: Boolean) -> Unit)? = null
    var onSignUpError: ((error: String) -> Unit)? = null
    var onConfirmationSuccess: (() -> Unit)? = null
    var onConfirmationError: ((error: String) -> Unit)? = null
    var onSignInSuccess: (() -> Unit)? = null
    var onSignInError: ((error: String) -> Unit)? = null
    var onNewPasswordRequired: (() -> Unit)? = null
    var onSignInNeedsConfirmation: (() -> Unit)? = null
    var onAuthCheckSuccess: ((email: String?) -> Unit)? = null
    var onAuthCheckError: (() -> Unit)? = null
    
    /**
     * Sign up a new user with email and password.
     * firstName, lastName, and birthdate are optional — omitted from the attribute
     * list entirely when blank, rather than sent as empty strings.
     */
    fun signUp(firstName: String, lastName: String, email: String, password: String, birthdate: String) {
        Log.d(TAG, "🚀 Starting sign up for: $email")

        val attributes = buildList {
            add(AuthUserAttribute(AuthUserAttributeKey.email(), email))
            if (firstName.isNotBlank()) add(AuthUserAttribute(AuthUserAttributeKey.givenName(), firstName))
            if (lastName.isNotBlank()) add(AuthUserAttribute(AuthUserAttributeKey.familyName(), lastName))
            toIsoBirthdate(birthdate)?.let { add(AuthUserAttribute(AuthUserAttributeKey.birthdate(), it)) }
        }

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
                onSignUpError?.invoke(AuthErrorMapper.toUserMessage(error))
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
                onConfirmationError?.invoke(AuthErrorMapper.toUserMessage(error))
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
                    when (result.nextStep.signInStep) {
                        AuthSignInStep.CONFIRM_SIGN_IN_WITH_NEW_PASSWORD -> {
                            Log.d(TAG, "🔐 Account requires a new password: $email")
                            onNewPasswordRequired?.invoke()
                        }
                        AuthSignInStep.CONFIRM_SIGN_UP -> {
                            Log.d(TAG, "📧 Account not yet confirmed: $email")
                            onSignInNeedsConfirmation?.invoke()
                        }
                        else -> {
                            Log.d(TAG, "Additional steps required: ${result.nextStep.signInStep}")
                            onSignInError?.invoke("Additional verification is required to sign in.")
                        }
                    }
                }
            },
            { error ->
                Log.e(TAG, "❌ Sign in failed for: $email", error)
                onSignInError?.invoke(AuthErrorMapper.toUserMessage(error))
            }
        )
    }

    /**
     * Complete sign-in for an account that Cognito flagged as requiring a new password
     * (e.g. an admin-created user with FORCE_CHANGE_PASSWORD status).
     *
     * When the user pool has required standard attributes (given_name, family_name, etc.)
     * that were never set on the account, Cognito rejects the challenge response unless
     * those attributes are supplied alongside the new password.
     */
    fun confirmSignInNewPassword(newPassword: String, firstName: String? = null, lastName: String? = null) {
        Log.d(TAG, "🔐 Submitting new password to complete sign-in")

        val attributes = buildList {
            if (!firstName.isNullOrBlank()) add(AuthUserAttribute(AuthUserAttributeKey.givenName(), firstName))
            if (!lastName.isNullOrBlank()) add(AuthUserAttribute(AuthUserAttributeKey.familyName(), lastName))
        }

        val onSuccess = { result: com.amplifyframework.auth.result.AuthSignInResult ->
            if (result.isSignedIn) {
                Log.d(TAG, "✅ Sign-in completed after password change")
                onSignInSuccess?.invoke()
            } else {
                Log.d(TAG, "Additional steps still required: ${result.nextStep.signInStep}")
                onSignInError?.invoke("Additional verification is required to finish signing in.")
            }
            Unit
        }

        if (attributes.isEmpty()) {
            Amplify.Auth.confirmSignIn(
                newPassword,
                onSuccess,
                { error ->
                    Log.e(TAG, "❌ Failed to set new password", error)
                    onSignInError?.invoke(AuthErrorMapper.toUserMessage(error))
                }
            )
        } else {
            val options = com.amplifyframework.auth.cognito.options.AWSCognitoAuthConfirmSignInOptions.builder()
                .userAttributes(attributes)
                .build()

            Amplify.Auth.confirmSignIn(
                newPassword,
                options,
                onSuccess,
                { error ->
                    Log.e(TAG, "❌ Failed to set new password", error)
                    onSignInError?.invoke(AuthErrorMapper.toUserMessage(error))
                }
            )
        }
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

    /**
     * Get the current user's ID token (JWT) for calling authenticated backend
     * endpoints (AppSync, API Gateway), or null if not signed in.
     */
    fun getIdToken(callback: (String?) -> Unit) {
        Amplify.Auth.fetchAuthSession(
            { result ->
                val cognitoSession = result as? com.amplifyframework.auth.cognito.AWSCognitoAuthSession
                val idToken = cognitoSession?.userPoolTokensResult?.value?.idToken
                callback(idToken)
            },
            { error ->
                Log.e(TAG, "Failed to fetch auth session for ID token", error)
                callback(null)
            }
        )
    }
}
