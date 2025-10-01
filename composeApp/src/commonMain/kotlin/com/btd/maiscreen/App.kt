package com.btd.maiscreen

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.btd.maiscreen.config.AppConfig
import com.btd.maiscreen.ui.screens.SplashScreen
import com.btd.maiscreen.ui.screens.LoginScreen
import com.btd.maiscreen.ui.screens.SignUpScreen
import com.btd.maiscreen.ui.screens.SubscriptionScreen
import com.btd.maiscreen.ui.screens.PaymentScreen
import com.btd.maiscreen.ui.screens.MainScreen
import com.btd.maiscreen.ui.screens.VideoPlayerScreen
import com.btd.maiscreen.ui.screens.ContentItem

@Composable
@Preview
fun App() {
    MaterialTheme {
        var currentScreen by remember { mutableStateOf(AppScreen.Splash) }
        var selectedTier by remember { mutableStateOf<com.btd.maiscreen.ui.screens.SubscriptionTier?>(null) }
        var selectedContent by remember { mutableStateOf<ContentItem?>(null) }
        
        when (currentScreen) {
            AppScreen.Splash -> {
                SplashScreen(
                    onSplashComplete = {
                        currentScreen = AppScreen.Login
                    }
                )
            }
            AppScreen.Login -> {
                LoginScreen(
                    onLoginClick = { email, password ->
                        // Test login account for easy testing
                        if (email == AppConfig.testEmail && password == AppConfig.testPassword) {
                            println("Test login successful!")
                            currentScreen = AppScreen.Main // Skip subscription flow for test account
                        } else {
                            println("Login attempt: $email")
                            // TODO: Implement real login logic
                        }
                    },
                    onSignUpClick = {
                        currentScreen = AppScreen.SignUp
                    }
                )
            }
            AppScreen.SignUp -> {
                SignUpScreen(
                    onSignUpClick = { firstName, lastName, email, password ->
                        // TODO: Implement sign up logic - after backend validation, go to subscription
                        println("Sign up attempt: $firstName $lastName, $email")
                        // Simulate successful sign up and proceed to subscription
                        currentScreen = AppScreen.Subscription
                    },
                    onBackToLoginClick = {
                        currentScreen = AppScreen.Login
                    }
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
                            // TODO: Navigate to profile screen
                        },
                        onSearchClick = {
                            println("Search clicked")
                            // TODO: Navigate to search screen
                        },
                        onContentClick = { content ->
                            println("Content clicked: ${content.title}")
                            // TODO: Navigate to content player
                        },
                        onPlayContent = { content ->
                            println("Play content: ${content.title}")
                            selectedContent = content
                            currentScreen = AppScreen.VideoPlayer
                        },
                        onToggleLike = { content ->
                            println("Toggle like: ${content.title}")
                            // TODO: Update like status
                        },
                        onToggleWatchlist = { content ->
                            println("Toggle watchlist: ${content.title}")
                            // TODO: Update watchlist status
                        }
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
        }
    }
}

enum class AppScreen {
    Splash,
    Login,
    SignUp,
    Subscription,
    Payment,
    Main,
    VideoPlayer
}