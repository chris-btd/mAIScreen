package com.btd.maiscreen

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.btd.maiscreen.auth.AuthManager
import com.btd.maiscreen.auth.AppAuthState
import com.btd.maiscreen.auth.SignInOutcome
import com.btd.maiscreen.ui.screens.ForceChangePasswordScreen
import com.btd.maiscreen.config.AppConfig
import com.btd.maiscreen.ui.screens.SplashScreen
import com.btd.maiscreen.ui.screens.LoginScreen
import com.btd.maiscreen.ui.screens.SignUpScreen
import com.btd.maiscreen.ui.screens.ConfirmationScreen
import com.btd.maiscreen.ui.screens.SubscriptionScreen
import com.btd.maiscreen.ui.screens.PaymentScreen
import com.btd.maiscreen.ui.screens.MainScreen
import com.btd.maiscreen.ui.screens.VideoPlayerScreen
import com.btd.maiscreen.ui.screens.SettingsScreen
import com.btd.maiscreen.ui.screens.PurchaseMinutesScreen
import com.btd.maiscreen.ui.screens.ProfileScreen
import com.btd.maiscreen.ui.screens.UploadTemplateScreen
import com.btd.maiscreen.ui.screens.MediaSubmissionScreen
import com.btd.maiscreen.ui.screens.CreatorSuiteScreen
import com.btd.maiscreen.ui.screens.ContentItem
import com.btd.maiscreen.config.AppSettings
import com.btd.maiscreen.viewmodel.AppViewModel
import com.btd.maiscreen.viewmodel.ProfileViewModel

@Composable
@Preview
fun App() {
    MaterialTheme {
        // Initialize ViewModels
        val appViewModel = remember { AppViewModel() }
        val profileViewModel = remember { ProfileViewModel() }

        var currentScreen by remember { mutableStateOf(AppScreen.Splash) }
        var selectedTier by remember { mutableStateOf<com.btd.maiscreen.ui.screens.SubscriptionTier?>(null) }
        var selectedContent by remember { mutableStateOf<ContentItem?>(null) }
        var selectedContentType by remember { mutableStateOf("") }
        var selectedSubType by remember { mutableStateOf("") }
        var pendingSignUpEmail by remember { mutableStateOf("") }
        // Held only in memory between SignUp and Confirmation so we can sign the user in
        // immediately after confirming — Cognito's confirmSignUp does NOT establish a
        // session by itself. Cleared right after that sign-in attempt completes.
        var pendingSignUpPassword by remember { mutableStateOf("") }
        var pendingForceChangeEmail by remember { mutableStateOf("") }
        var isSignUpLoading by remember { mutableStateOf(false) }
        
        // Observe authentication state
        val isAuthenticated by AppAuthState.isAuthenticated
        val userEmail by AppAuthState.userEmail
        
        // Auto-navigate based on authentication state
        LaunchedEffect(isAuthenticated) {
            if (isAuthenticated && currentScreen in listOf(AppScreen.Login, AppScreen.SignUp, AppScreen.Confirmation, AppScreen.ForceChangePassword)) {
                println("🔐 User authenticated, navigating to main screen")
                currentScreen = AppScreen.Main
            } else if (!isAuthenticated && currentScreen == AppScreen.Main) {
                println("🚪 User logged out, navigating to login screen")
                currentScreen = AppScreen.Login
            }
        }
        
        when (currentScreen) {
            AppScreen.Splash -> {
                SplashScreen(
                    onSplashComplete = {
                        // Check if user was found to be authenticated during splash
                        if (isAuthenticated) {
                            println("🚀 User authenticated during splash, navigating to main screen")
                            currentScreen = AppScreen.Main
                        } else {
                            println("🚀 User not authenticated, navigating to login screen")
                            currentScreen = AppScreen.Login
                        }
                    },
                    onAuthenticatedUserFound = { userEmail ->
                        println("🔐 Setting authenticated state for user: $userEmail")
                        AppAuthState.setAuthenticated(true, userEmail)
                    }
                )
            }
            AppScreen.Login -> {
                var isLoading by remember { mutableStateOf(false) }
                
                LoginScreen(
                    onLoginClick = { email, password ->
                        println("🔑 Starting native Amplify sign-in for: $email")
                        appViewModel.clearError()
                        isLoading = true
                        
                        // Check if already authenticated first
                        if (isAuthenticated) {
                            println("ℹ️ User already authenticated, skipping login")
                            isLoading = false
                            return@LoginScreen
                        }
                        
                        AuthManager.signInWithAmplify(email, password) { outcome, error ->
                            isLoading = false // Stop loading spinner

                            when (outcome) {
                                SignInOutcome.SUCCESS -> {
                                    println("✅ Native sign-in successful!")
                                    AppAuthState.setAuthenticated(true, email)
                                }
                                SignInOutcome.NEEDS_NEW_PASSWORD -> {
                                    println("🔐 Account requires a new password")
                                    pendingForceChangeEmail = email
                                    currentScreen = AppScreen.ForceChangePassword
                                }
                                SignInOutcome.NEEDS_CONFIRMATION -> {
                                    println("📧 Account not yet confirmed, routing to verification")
                                    pendingSignUpEmail = email
                                    AuthManager.resendConfirmationCode(email)
                                    appViewModel.showSuccess("Please verify your email. We've sent a new code to $email.")
                                    currentScreen = AppScreen.Confirmation
                                }
                                SignInOutcome.FAILED -> {
                                    println("❌ Native sign-in failed: $error")
                                    // Handle "already signed in" error gracefully
                                    if (error?.contains("already a user signed in") == true) {
                                        println("ℹ️ User was already signed in, updating state")
                                        AppAuthState.setAuthenticated(true, email)
                                    } else {
                                        // Show error to user
                                        appViewModel.showError(error ?: "Login failed. Please check your credentials and try again.")
                                    }
                                }
                            }
                        }
                    },
                    onSignUpClick = {
                        currentScreen = AppScreen.SignUp
                    },
                    errorMessage = appViewModel.errorMessage,
                    onErrorDismiss = { appViewModel.clearError() },
                    isLoading = isLoading
                )
            }
            AppScreen.SignUp -> {
                SignUpScreen(
                    onSignUpClick = { firstName, lastName, email, password, birthdate ->
                        println("🚀 Starting Amplify sign up: $firstName $lastName, $email, birthdate: $birthdate")
                        pendingSignUpEmail = email
                        pendingSignUpPassword = password
                        isSignUpLoading = true
                        appViewModel.clearError()
                        
                        // Use AuthManager to call Amplify sign-up (will be handled by platform-specific code)
                        AuthManager.signUpWithAmplify(firstName, lastName, email, password, birthdate) { success, needsConfirmation, error ->
                            isSignUpLoading = false
                            if (success) {
                                if (needsConfirmation) {
                                    currentScreen = AppScreen.Confirmation
                                } else {
                                    currentScreen = AppScreen.Subscription
                                }
                            } else {
                                println("❌ Sign up failed: $error")
                                appViewModel.showError(error ?: "Sign up failed. Please try again.")
                            }
                        }
                    },
                    onBackToLoginClick = {
                        currentScreen = AppScreen.Login
                    },
                    isLoading = isSignUpLoading,
                    errorMessage = appViewModel.errorMessage,
                    onErrorDismiss = { appViewModel.clearError() }
                )
            }
            AppScreen.Confirmation -> {
                ConfirmationScreen(
                    email = pendingSignUpEmail,
                    onConfirmClick = { confirmationCode ->
                        println("🔐 Confirming sign up with code: $confirmationCode")
                        appViewModel.clearError()
                        AuthManager.confirmSignUp(pendingSignUpEmail, confirmationCode) { success ->
                            if (success) {
                                // Confirming the code only marks the account verified in Cognito —
                                // it does not establish a session, so sign in now with the
                                // password we held onto since SignUp.
                                val email = pendingSignUpEmail
                                val password = pendingSignUpPassword
                                AuthManager.signInWithAmplify(email, password) { outcome, error ->
                                    pendingSignUpPassword = ""
                                    when (outcome) {
                                        SignInOutcome.SUCCESS -> {
                                            AppAuthState.setAuthenticated(true, email)
                                            appViewModel.showSuccess("Email verified successfully!")
                                            currentScreen = AppScreen.Subscription
                                        }
                                        else -> {
                                            println("❌ Sign-in after confirmation failed: $error")
                                            appViewModel.showSuccess("Email verified! Please sign in to continue.")
                                            currentScreen = AppScreen.Login
                                        }
                                    }
                                }
                            } else {
                                println("❌ Confirmation failed")
                                appViewModel.showError("Invalid verification code. Please try again.")
                            }
                        }
                    },
                    onResendClick = {
                        println("📧 Resending confirmation code")
                        appViewModel.clearError()
                        AuthManager.resendConfirmationCode(pendingSignUpEmail)
                        appViewModel.showSuccess("Verification code resent to your email")
                    },
                    onBackClick = {
                        currentScreen = AppScreen.SignUp
                    },
                    onBypassClick = {
                        // UI-navigation shortcut only — this never calls confirmSignUp, so the
                        // account stays UNCONFIRMED in Cognito and can never sign in for real
                        // (Cognito rejects sign-in for unconfirmed users). Anything past this
                        // point that requires a real JWT (e.g. video playback) will fail until
                        // the account is confirmed and signed in through the normal path.
                        println("🔧 DEV MODE: Bypassing email verification (UI only — no real session will exist)")
                        pendingSignUpPassword = ""
                        currentScreen = AppScreen.Subscription
                    }
                )
            }
            AppScreen.ForceChangePassword -> {
                var isLoading by remember { mutableStateOf(false) }

                ForceChangePasswordScreen(
                    email = pendingForceChangeEmail,
                    onSubmit = { newPassword, firstName, lastName ->
                        println("🔐 Submitting new password for forced change")
                        appViewModel.clearError()
                        isLoading = true
                        AuthManager.confirmSignInNewPassword(newPassword, firstName, lastName) { success, error ->
                            isLoading = false
                            if (success) {
                                println("✅ New password set, sign-in complete")
                                AppAuthState.setAuthenticated(true, pendingForceChangeEmail)
                            } else {
                                println("❌ Failed to set new password: $error")
                                appViewModel.showError(error ?: "Failed to set new password. Please try again.")
                            }
                        }
                    },
                    isLoading = isLoading,
                    errorMessage = appViewModel.errorMessage,
                    onErrorDismiss = { appViewModel.clearError() }
                )
            }
                AppScreen.Subscription -> {
                    SubscriptionScreen(
                        onTierSelected = { tier ->
                            selectedTier = tier
                            println("Tier selected: ${tier.name}")
                        },
                        onContinueClick = {
                            currentScreen = AppScreen.Payment
                        },
                        onSubmissionRulesClick = { tierName ->
                            println("Submission rules clicked for: $tierName")
                            // TODO: Show submission rules dialog or navigate to rules screen
                        }
                    )
                }
                AppScreen.Payment -> {
                    selectedTier?.let { tier ->
                        PaymentScreen(
                            selectedTier = com.btd.maiscreen.ui.screens.PaymentTier(
                                name = tier.name,
                                price = tier.price,
                                priceSubtext = tier.priceSubtext
                            ),
                            onBackClick = {
                                currentScreen = AppScreen.Subscription
                            },
                            onPaymentSuccess = {
                                currentScreen = AppScreen.Main
                            },
                            onPaymentError = { error ->
                                println("Payment error: $error")
                                // TODO: Show error dialog
                            }
                        )
                    }
                }
                AppScreen.Main -> {
                    MainScreen(
                        onProfileClick = {
                            println("Profile clicked")
                            currentScreen = AppScreen.Profile
                        },
                        onSearchClick = {
                            println("Search clicked")
                            // TODO: Navigate to search screen
                        },
                        onSettingsClick = {
                            println("Settings clicked")
                            currentScreen = AppScreen.Settings
                        },
                        onUploadClick = {
                            println("Upload clicked")
                            currentScreen = AppScreen.UploadTemplate
                        },
                        onCreatorSuiteClick = {
                            println("Creator Suite clicked")
                            currentScreen = AppScreen.CreatorSuite
                        },
                        onContentClick = { content ->
                            println("Content clicked: ${content.title}")
                            // TODO: Navigate to content player
                        },
                        onPlayContent = { content ->
                            println("Play content: ${content.title}")
                            selectedContent = content
                            // Add to recently watched
                            appViewModel.addToRecentlyWatched(content)
                            currentScreen = AppScreen.VideoPlayer
                        },
                        onToggleLike = { content ->
                            println("Toggle like: ${content.title}")
                            // TODO: Update like status
                        },
                        onToggleWatchlist = { content ->
                            println("Toggle watchlist: ${content.title}")
                            // TODO: Update watchlist status
                        },
                        recentlyWatched = appViewModel.recentlyWatchedContent
                    )
                }
                AppScreen.VideoPlayer -> {
                    selectedContent?.let { content ->
                        VideoPlayerScreen(
                            content = content,
                            onBackClick = {
                                currentScreen = AppScreen.Main
                            },
                            onPlayerError = { error ->
                                println("Video player error: $error")
                                // Could show error dialog or navigate back
                            }
                        )
                    }
                }
                AppScreen.CreatorSuite -> {
                    CreatorSuiteScreen(
                        onBackClick = {
                            currentScreen = AppScreen.Main
                        }
                    )
                }
                AppScreen.Settings -> {
                    SettingsScreen(
                        onBackClick = {
                            currentScreen = AppScreen.Main
                        },
                        onTogglePlaybackMode = { isLocal ->
                            AppSettings.setPlaybackMode(isLocal)
                        },
                        isLocalPlaybackEnabled = AppSettings.isLocalPlaybackEnabled,
                        onPurchaseMinutesClick = {
                            currentScreen = AppScreen.PurchaseMinutes
                        },
                        onLogoutClick = {
                            println("🚪 Logout clicked from settings")
                            AuthManager.logout()
                            AppAuthState.logout()
                            // Navigate to login screen after logout
                            currentScreen = AppScreen.Login
                        }
                    )
                }
                AppScreen.PurchaseMinutes -> {
                    PurchaseMinutesScreen(
                        uiState = profileViewModel.state.value,
                        onPurchaseClick = { pkg ->
                            println("💰 Purchasing ${pkg.minutes} minutes for ${pkg.price}")
                            // TODO: Integrate with in-app purchase system (Google Play/App Store)
                            // TODO: Update user's minute balance after successful purchase
                            profileViewModel.purchaseMinutes(pkg.minutes)
                            appViewModel.showSuccess("Successfully purchased ${pkg.minutes} minutes!")
                        },
                        onBackClick = {
                            currentScreen = AppScreen.Profile
                        }
                    )
                }
                AppScreen.UploadTemplate -> {
                    UploadTemplateScreen(
                        onBackClick = {
                            currentScreen = AppScreen.Main
                        },
                        onNextClick = { type, subType ->
                            println("📤 Upload template selected: $type - $subType")
                            selectedContentType = type
                            selectedSubType = subType
                            currentScreen = AppScreen.MediaSubmission
                        }
                    )
                }
                AppScreen.MediaSubmission -> {
                    MediaSubmissionScreen(
                        contentType = selectedContentType,
                        subType = selectedSubType,
                        onBackClick = {
                            currentScreen = AppScreen.UploadTemplate
                        },
                        onSubmitClick = { formData ->
                            println("📤 Media submission: $formData")
                            // TODO: Submit data to backend
                            currentScreen = AppScreen.Main
                        }
                    )
                }
                AppScreen.Profile -> {
                    ProfileScreen(
                        viewModel = profileViewModel,
                        onBackClick = {
                            currentScreen = AppScreen.Main
                        },
                        onPurchaseMinutesClick = {
                            currentScreen = AppScreen.PurchaseMinutes
                        }
                    )
                }
        }
        
        // Global Error Dialog
        appViewModel.errorMessage?.let { error ->
            AlertDialog(
                onDismissRequest = { appViewModel.clearError() },
                title = {
                    Text(
                        text = "Error",
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                },
                text = {
                    Text(
                        text = error,
                        color = Color.Black
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = { appViewModel.clearError() }
                    ) {
                        Text("OK", color = Color.Red)
                    }
                }
            )
        }
        
        // Global Success Dialog
        appViewModel.successMessage?.let { success ->
            AlertDialog(
                onDismissRequest = { appViewModel.clearSuccess() },
                title = {
                    Text(
                        text = "Success",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF059669)
                    )
                },
                text = {
                    Text(
                        text = success,
                        color = Color.Black
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = { appViewModel.clearSuccess() }
                    ) {
                        Text("OK", color = Color(0xFF059669))
                    }
                }
            )
        }
    }
}

enum class AppScreen {
    Splash,
    Login,
    SignUp,
    Confirmation,
    ForceChangePassword,
    Subscription,
    Payment,
    Main,
    VideoPlayer,
    CreatorSuite,
    Settings,
    PurchaseMinutes,
    UploadTemplate,
    MediaSubmission,
    Profile
}