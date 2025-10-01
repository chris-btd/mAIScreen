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
}

struct ContentView: View {
    @State private var currentScreen: AppScreen = .splash
    @State private var selectedTier: SubscriptionTier?
    @State private var selectedContent: ContentItem?
    
    var body: some View {
        switch currentScreen {
        case .splash:
            SplashView {
                currentScreen = .login
            }
        case .login:
            LoginView(
                onLoginClick: { email, password in
                    // TODO: Implement login logic
                    print("Login attempt: \(email)")
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
                    onContentClick: { content in
                        print("Content clicked: \(content.title)")
                        // TODO: Navigate to content player
                    },
                    onPlayContent: { content in
                        print("Play content: \(content.title)")
                        selectedContent = content
                        currentScreen = .videoPlayer
                    },
                    onToggleLike: { content in
                        print("Toggle like: \(content.title)")
                        // TODO: Update like status
                    },
                    onToggleWatchlist: { content in
                        print("Toggle watchlist: \(content.title)")
                        // TODO: Update watchlist status
                    }
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
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
