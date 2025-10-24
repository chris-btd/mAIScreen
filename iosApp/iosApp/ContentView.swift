import SwiftUI
import Shared

enum AppScreen {
    case splash
    case login
    case signUp
    case subscription
    case payment
    case main
    case videoPlayer
    case settings
    case uploadTemplate
}

struct ContentView: View {
    @State private var currentScreen: AppScreen = .splash
    @State private var selectedTier: SubscriptionTier?
    @State private var selectedContent: ContentItem?
    @State private var recentlyWatched: [ContentItem] = []
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
                    print("🔑 iOS: Starting login for: \(email)")
                    
                    // Check if already authenticated first
                    if authService.isAuthenticated {
                        print("ℹ️ iOS: User already authenticated, skipping login")
                        return
                    }
                    
                    // Use AuthService to simulate login
                    authService.simulateLogin(email: email, password: password)
                },
                onSignUpClick: {
                    currentScreen = .signUp
                }
            )
        case .signUp:
            SignUpView(
                onSignUpClick: { firstName, lastName, email, password in
                    // TODO: Implement sign up logic - after backend validation, go to subscription
                    print("Sign up attempt: \(firstName) \(lastName), \(email)")
                    // Simulate successful sign up and proceed to subscription
                    currentScreen = .subscription
                },
                onBackToLoginClick: {
                    currentScreen = .login
                }
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
                    }
                )
            case .uploadTemplate:
                UploadTemplateView(
                    onBackClick: {
                        currentScreen = .main
                    },
                    onNextClick: { type, subType in
                        print("📤 iOS: Upload template selected: \(type) - \(subType)")
                        // TODO: Navigate to next step in upload flow
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
