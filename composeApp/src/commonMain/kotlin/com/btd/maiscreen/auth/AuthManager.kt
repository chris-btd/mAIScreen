package com.btd.maiscreen.auth

/**
 * Outcome of a native sign-in attempt, mirroring the Cognito next-step cases
 * the app knows how to route (new-password-required, unconfirmed account).
 */
enum class SignInOutcome {
    SUCCESS,
    NEEDS_NEW_PASSWORD,
    NEEDS_CONFIRMATION,
    FAILED
}

/**
 * Simple singleton to hold reference to CognitoAuthService and AmplifyAuthService for access from common code
 * This is a temporary solution - in production, use proper DI framework
 */
object AuthManager {
    var cognitoAuthService: Any? = null
    var amplifyAuthService: Any? = null
    
    fun startCognitoAuth() {
        try {
            val service = cognitoAuthService
            if (service != null) {
                val method = service::class.java.getMethod("startAuthorization")
                method.invoke(service)
            }
        } catch (e: Exception) {
            println("Failed to start Cognito auth: ${e.message}")
        }
    }
    
    fun logout() {
        try {
            // Sign out from Amplify first (more important for persistent sessions)
            val amplifyService = amplifyAuthService
            if (amplifyService != null) {
                val method = amplifyService::class.java.getMethod("signOut")
                method.invoke(amplifyService)
                println("✅ Amplify sign out initiated")
            }
            
            // Also sign out from Cognito
            val cognitoService = cognitoAuthService
            if (cognitoService != null) {
                val method = cognitoService::class.java.getMethod("logout")
                method.invoke(cognitoService)
                println("✅ Cognito logout initiated")
            }
        } catch (e: Exception) {
            println("Failed to logout: ${e.message}")
        }
    }
    
    fun isAuthenticated(): Boolean {
        return try {
            val service = cognitoAuthService
            if (service != null) {
                val method = service::class.java.getMethod("isAuthenticated")
                method.invoke(service) as? Boolean ?: false
            } else {
                false
            }
        } catch (e: Exception) {
            println("Failed to check auth status: ${e.message}")
            false
        }
    }
    
    /**
     * Check authentication status asynchronously using Amplify
     * This is more reliable for checking persistent sessions
     */
    fun checkAuthenticationStatus(callback: (isAuthenticated: Boolean, userEmail: String?) -> Unit) {
        try {
            val service = amplifyAuthService
            if (service != null) {
                // Set callback on the service
                val onAuthCheckSuccessField = service::class.java.getDeclaredField("onAuthCheckSuccess")
                onAuthCheckSuccessField.isAccessible = true
                onAuthCheckSuccessField.set(service) { email: String? ->
                    callback(true, email)
                }
                
                val onAuthCheckErrorField = service::class.java.getDeclaredField("onAuthCheckError")
                onAuthCheckErrorField.isAccessible = true
                onAuthCheckErrorField.set(service) {
                    callback(false, null)
                }
                
                // Call checkAuthenticationStatus method
                val method = service::class.java.getMethod("checkAuthenticationStatus")
                method.invoke(service)
            } else {
                // Fallback to synchronous check if Amplify service not available
                val isAuth = isAuthenticated()
                callback(isAuth, if (isAuth) null else null)
            }
        } catch (e: Exception) {
            println("Failed to check authentication status: ${e.message}")
            // Fallback to synchronous check
            val isAuth = isAuthenticated()
            callback(isAuth, if (isAuth) null else null)
        }
    }
    
    // Amplify Auth Methods
    fun signUpWithAmplify(
        firstName: String, 
        lastName: String, 
        email: String, 
        password: String, 
        birthdate: String,
        callback: (success: Boolean, needsConfirmation: Boolean, error: String?) -> Unit
    ) {
        try {
            val service = amplifyAuthService
            if (service != null) {
                // Set callback on the service
                val onSignUpSuccessField = service::class.java.getDeclaredField("onSignUpSuccess")
                onSignUpSuccessField.isAccessible = true
                onSignUpSuccessField.set(service) { username: String, isSignUpComplete: Boolean ->
                    callback(true, !isSignUpComplete, null)
                }
                
                val onSignUpErrorField = service::class.java.getDeclaredField("onSignUpError")
                onSignUpErrorField.isAccessible = true
                onSignUpErrorField.set(service) { error: String ->
                    println("Sign up error: $error")
                    callback(false, false, error)
                }
                
                // Call signUp method
                val method = service::class.java.getMethod("signUp", String::class.java, String::class.java, String::class.java, String::class.java, String::class.java)
                method.invoke(service, firstName, lastName, email, password, birthdate)
            } else {
                callback(false, false, "Authentication service not available")
            }
        } catch (e: Exception) {
            println("Failed to sign up with Amplify: ${e.message}")
            callback(false, false, e.message ?: "Unknown error occurred")
        }
    }
    
    fun confirmSignUp(username: String, confirmationCode: String, callback: (success: Boolean) -> Unit) {
        try {
            val service = amplifyAuthService
            if (service != null) {
                // Set callback on the service
                val onConfirmationSuccessField = service::class.java.getDeclaredField("onConfirmationSuccess")
                onConfirmationSuccessField.isAccessible = true
                onConfirmationSuccessField.set(service) {
                    callback(true)
                }
                
                val onConfirmationErrorField = service::class.java.getDeclaredField("onConfirmationError")
                onConfirmationErrorField.isAccessible = true
                onConfirmationErrorField.set(service) { error: String ->
                    println("Confirmation error: $error")
                    callback(false)
                }
                
                // Call confirmSignUp method
                val method = service::class.java.getMethod("confirmSignUp", String::class.java, String::class.java)
                method.invoke(service, username, confirmationCode)
            } else {
                callback(false)
            }
        } catch (e: Exception) {
            println("Failed to confirm sign up: ${e.message}")
            callback(false)
        }
    }
    
    fun resendConfirmationCode(username: String) {
        try {
            val service = amplifyAuthService
            if (service != null) {
                val method = service::class.java.getMethod("resendConfirmationCode", String::class.java)
                method.invoke(service, username)
            }
        } catch (e: Exception) {
            println("Failed to resend confirmation code: ${e.message}")
        }
    }

    // Native Sign-In Methods
    fun signInWithAmplify(
        email: String,
        password: String,
        callback: (outcome: SignInOutcome, error: String?) -> Unit
    ) {
        try {
            val service = amplifyAuthService
            if (service != null) {
                // Set callback on the service
                val onSignInSuccessField = service::class.java.getDeclaredField("onSignInSuccess")
                onSignInSuccessField.isAccessible = true
                onSignInSuccessField.set(service) {
                    callback(SignInOutcome.SUCCESS, null)
                }

                val onSignInErrorField = service::class.java.getDeclaredField("onSignInError")
                onSignInErrorField.isAccessible = true
                onSignInErrorField.set(service) { error: String ->
                    println("Sign in error: $error")
                    callback(SignInOutcome.FAILED, error)
                }

                val onNewPasswordRequiredField = service::class.java.getDeclaredField("onNewPasswordRequired")
                onNewPasswordRequiredField.isAccessible = true
                onNewPasswordRequiredField.set(service) {
                    callback(SignInOutcome.NEEDS_NEW_PASSWORD, null)
                }

                val onSignInNeedsConfirmationField = service::class.java.getDeclaredField("onSignInNeedsConfirmation")
                onSignInNeedsConfirmationField.isAccessible = true
                onSignInNeedsConfirmationField.set(service) {
                    callback(SignInOutcome.NEEDS_CONFIRMATION, null)
                }

                // Call signIn method
                val method = service::class.java.getMethod("signIn", String::class.java, String::class.java)
                method.invoke(service, email, password)
            } else {
                callback(SignInOutcome.FAILED, "Authentication service not available")
            }
        } catch (e: Exception) {
            println("Failed to sign in with Amplify: ${e.message}")
            callback(SignInOutcome.FAILED, e.message ?: "Unknown error occurred")
        }
    }

    /**
     * Complete a sign-in that Cognito flagged as requiring a new password
     * (e.g. an admin-created user with FORCE_CHANGE_PASSWORD status).
     */
    fun confirmSignInNewPassword(
        newPassword: String,
        firstName: String? = null,
        lastName: String? = null,
        callback: (success: Boolean, error: String?) -> Unit
    ) {
        try {
            val service = amplifyAuthService
            if (service != null) {
                val onSignInSuccessField = service::class.java.getDeclaredField("onSignInSuccess")
                onSignInSuccessField.isAccessible = true
                onSignInSuccessField.set(service) {
                    callback(true, null)
                }

                val onSignInErrorField = service::class.java.getDeclaredField("onSignInError")
                onSignInErrorField.isAccessible = true
                onSignInErrorField.set(service) { error: String ->
                    println("Confirm new password error: $error")
                    callback(false, error)
                }

                val method = service::class.java.getMethod(
                    "confirmSignInNewPassword",
                    String::class.java,
                    String::class.java,
                    String::class.java
                )
                method.invoke(service, newPassword, firstName, lastName)
            } else {
                callback(false, "Authentication service not available")
            }
        } catch (e: Exception) {
            println("Failed to confirm new password: ${e.message}")
            callback(false, e.message ?: "Unknown error occurred")
        }
    }
}
