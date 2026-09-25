import SwiftUI
import Shared

enum AppScreen {
    case splash
    case login
    case signUp
    case confirmation
    case forceChangePassword
    case subscription
    case payment
    case main
    case videoPlayer
    case settings
    case uploadTemplate
    case mediaSubmission
}

struct ContentView: View {
    @State private var currentScreen: AppScreen = .splash
    @State private var selectedTier: SubscriptionTier?
    @State private var selectedContent: ContentItem?
    @State private var recentlyWatched: [ContentItem] = []
    @State private var selectedContentType: String = ""
    @State private var selectedSubType: String = ""
    @State private var pendingSignUpEmail: String = ""
    // Held only in memory between SignUp and Confirmation so we can sign the user in
    // immediately after confirming — Cognito's confirmSignUp does NOT establish a
    // session by itself. Cleared right after that sign-in attempt completes.
    @State private var pendingSignUpPassword: String = ""
    @State private var pendingForceChangeEmail: String = ""
    @State private var authErrorMessage: String? = nil
    @StateObject private var authService = AuthService.shared
    
    var body: some View {
        Group {
            switch currentScreen {
        case .splash:
            SplashView(
                onSplashComplete: {
                    // Check if user was found to be authenticated during splash
                    if authService.isAuthenticated {
                        print("🚀 iOS: User authenticated during splash, navigating to main screen")
                        currentScreen = .main
                    } else {
                        print("🚀 iOS: User not authenticated, navigating to login screen")
                        currentScreen = .login
                    }
                },
                onAuthenticatedUserFound: { userEmail in
                    print("🔐 iOS: Setting authenticated state for user: \(userEmail ?? "unknown")")
                    authService.setAuthenticated(true, email: userEmail)
                }
            )
        case .login:
            LoginView(
                onLoginClick: { email, password in
                    print("🔑 iOS: Starting native Amplify sign-in for: \(email)")

                    // Check if already authenticated first
                    if authService.isAuthenticated {
                        print("ℹ️ iOS: User already authenticated, skipping login")
                        return
                    }

                    authErrorMessage = nil
                    authService.signIn(email: email, password: password) { outcome, error in
                        switch outcome {
                        case .success:
                            print("✅ iOS: Native sign-in successful")
                        case .needsNewPassword:
                            print("🔐 iOS: Account requires a new password")
                            pendingForceChangeEmail = email
                            currentScreen = .forceChangePassword
                        case .needsConfirmation:
                            print("📧 iOS: Account not confirmed, routing to verification")
                            pendingSignUpEmail = email
                            authService.resendConfirmationCode(username: email)
                            currentScreen = .confirmation
                        case .failed:
                            print("❌ iOS: Native sign-in failed: \(error ?? "Unknown error")")
                            authErrorMessage = error ?? "Login failed. Please check your credentials and try again."
                        }
                    }
                },
                onSignUpClick: {
                    currentScreen = .signUp
                }
            )
        case .signUp:
            SignUpView(
                onSignUpClick: { firstName, lastName, email, password in
                    print("🚀 iOS: Starting Amplify sign up: \(firstName) \(lastName), \(email)")

                    authErrorMessage = nil
                    pendingSignUpEmail = email
                    pendingSignUpPassword = password
                    authService.signUp(firstName: firstName, lastName: lastName, email: email, password: password, birthdate: "") { success, needsConfirmation, error in
                        if success {
                            if needsConfirmation {
                                currentScreen = .confirmation
                            } else {
                                currentScreen = .subscription
                            }
                        } else {
                            print("❌ iOS: Sign up failed: \(error ?? "Unknown error")")
                            authErrorMessage = error ?? "Sign up failed. Please try again."
                        }
                    }
                },
                onBackToLoginClick: {
                    currentScreen = .login
                }
            )
        case .confirmation:
            ConfirmationView(
                email: pendingSignUpEmail,
                onConfirmClick: { code in
                    print("🔐 iOS: Confirming sign up with code: \(code)")
                    authErrorMessage = nil
                    authService.confirmSignUp(username: pendingSignUpEmail, confirmationCode: code) { success, error in
                        if success {
                            // Confirming the code only marks the account verified in Cognito —
                            // it does not establish a session, so sign in now with the
                            // password we held onto since SignUp.
                            let email = pendingSignUpEmail
                            let password = pendingSignUpPassword
                            authService.signIn(email: email, password: password) { outcome, signInError in
                                pendingSignUpPassword = ""
                                switch outcome {
                                case .success:
                                    currentScreen = .subscription
                                default:
                                    print("❌ iOS: Sign-in after confirmation failed: \(signInError ?? "Unknown error")")
                                    currentScreen = .login
                                }
                            }
                        } else {
                            authErrorMessage = error ?? "Invalid verification code. Please try again."
                        }
                    }
                },
                onResendClick: {
                    print("📧 iOS: Resending confirmation code")
                    authService.resendConfirmationCode(username: pendingSignUpEmail)
                },
                onBackClick: {
                    currentScreen = .signUp
                },
                errorMessage: authErrorMessage,
                onErrorDismiss: { authErrorMessage = nil }
            )
        case .forceChangePassword:
            ForceChangePasswordView(
                email: pendingForceChangeEmail,
                onSubmit: { newPassword, firstName, lastName in
                    print("🔐 iOS: Submitting new password for forced change")
                    authErrorMessage = nil
                    authService.confirmSignInNewPassword(
                        newPassword: newPassword,
                        email: pendingForceChangeEmail,
                        firstName: firstName,
                        lastName: lastName
                    ) { success, error in
                        if !success {
                            authErrorMessage = error ?? "Failed to set new password. Please try again."
                        }
                    }
                },
                errorMessage: authErrorMessage,
                onErrorDismiss: { authErrorMessage = nil }
            )
            case .subscription:
                SubscriptionView(
                    onTierSelected: { tier in
                        selectedTier = tier
                        print("Tier selected: \(tier.name)")
                    },
                    onContinueClick: {
                        currentScreen = .payment
                    },
                    onSubmissionRulesClick: { tierName in
                        print("Submission rules clicked for: \(tierName)")
                        // TODO: Show submission rules dialog or navigate to rules screen
                    }
                )
            case .payment:
                if let tier = selectedTier {
                    PaymentView(
                        selectedTier: tier,
                        onBackClick: {
                            currentScreen = .subscription
                        },
                        onPaymentSuccess: {
                            currentScreen = .main
                        },
                        onPaymentError: { error in
                            print("Payment error: \(error)")
                            // TODO: Show error dialog
                        }
                    )
                }
            case .main:
                MainView(
                    onProfileClick: {
                        print("Profile clicked")
                        // TODO: Navigate to profile screen
                    },
                    onSearchClick: {
                        print("Search clicked")
                        // TODO: Navigate to search screen
                    },
                    onSettingsClick: {
                        print("⚙️ Settings clicked")
                        currentScreen = .settings
                    },
                    onUploadClick: {
                        print("📤 Upload clicked")
                        currentScreen = .uploadTemplate
                    },
                    onContentClick: { content in
                        print("Content clicked: \(content.title)")
                        // TODO: Navigate to content player
                    },
                    onPlayContent: { content in
                        print("Play content: \(content.title)")
                        selectedContent = content
                        // Add to recently watched
                        addToRecentlyWatched(content)
                        currentScreen = .videoPlayer
                    },
                    onToggleLike: { content in
                        print("Toggle like: \(content.title)")
                        // TODO: Update like status
                    },
                    onToggleWatchlist: { content in
                        print("Toggle watchlist: \(content.title)")
                        // TODO: Update watchlist status
                    },
                    recentlyWatched: recentlyWatched
                )
            case .videoPlayer:
                if let content = selectedContent {
                    VideoPlayerView(
                        content: content,
                        onDismiss: {
                            currentScreen = .main
                        },
                        onPlayerError: { error in
                            print("Video player error: \\(error)")
                            // Could show error alert or navigate back
                        }
                    )
                }
            case .settings:
                SettingsView(
                    onBackClick: {
                        currentScreen = .main
                    },
                    onLogoutClick: {
                        print("🚪 iOS: Logout clicked from settings")
                        authService.logout()
                        // Navigate to login screen after logout
                        currentScreen = .login
                    }
                )
            case .uploadTemplate:
                UploadTemplateView(
                    onBackClick: {
                        currentScreen = .main
                    },
                    onNextClick: { type, subType in
                        print("📤 iOS: Upload template selected: \(type) - \(subType)")
                        selectedContentType = type
                        selectedSubType = subType
                        currentScreen = .mediaSubmission
                    }
                )
            case .mediaSubmission:
                MediaSubmissionView(
                    contentType: selectedContentType,
                    subType: selectedSubType,
                    onBackClick: {
                        currentScreen = .uploadTemplate
                    },
                    onSubmitClick: { formData in
                        print("📤 iOS: Media submission: \(formData)")
                        // TODO: Submit data to backend
                        currentScreen = .main
                    }
                )
            }
        }
        .onChange(of: authService.isAuthenticated) { isAuthenticated in
            if isAuthenticated && currentScreen == .login {
                print("🔐 iOS: User authenticated, navigating to main screen")
                currentScreen = .main
            } else if !isAuthenticated && currentScreen == .main {
                print("🚪 iOS: User logged out, navigating to login screen")
                currentScreen = .login
            }
        }
        .alert(
            currentScreen == .signUp ? "Sign Up Failed" : "Login Error",
            isPresented: Binding(
                get: { authErrorMessage != nil && (currentScreen == .login || currentScreen == .signUp) },
                set: { _ in authErrorMessage = nil }
            ),
            actions: { Button("OK") { authErrorMessage = nil } },
            message: { Text(authErrorMessage ?? "") }
        )
    }
    
    // Helper function to add content to recently watched
    private func addToRecentlyWatched(_ content: ContentItem) {
        // Remove if already exists to avoid duplicates
        recentlyWatched.removeAll { $0.id == content.id }
        
        // Add to the beginning
        recentlyWatched.insert(content, at: 0)
        
        // Keep only the last 10 items
        if recentlyWatched.count > 10 {
            recentlyWatched = Array(recentlyWatched.prefix(10))
        }
        
        print("📺 iOS: Added to recently watched: \(content.title)")
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
