# mAIScreen - Onboarding Setup

This document describes the onboarding flow implementation for mAIScreen.

## Features Implemented

### 1. Splash Screen
- **Android/Desktop**: Jetpack Compose implementation with animated progress bar
- **iOS**: SwiftUI implementation with animated progress bar
- Shows app branding and loading progress (0-100%)
- Automatically transitions to login screen after 2.5 seconds

### 2. Login Screen
- **Android/Desktop**: Jetpack Compose implementation
- **iOS**: SwiftUI implementation
- Email and password input fields
- Password visibility toggle
- Login button with loading state
- Sign up button (navigates to sign up screen)
- Forgot password option (ready for implementation)

### 3. Sign Up Screen
- **Android/Desktop**: Jetpack Compose implementation
- **iOS**: SwiftUI implementation
- First name and last name input fields
- Email and confirm email input fields
- Password and confirm password input fields with visibility toggles
- Real-time form validation with error messages
- Sign up button with loading state (disabled until form is valid)
- Back to login navigation

### 4. Subscription Plan Selection Screen
- **Android/Desktop**: Jetpack Compose implementation
- **iOS**: SwiftUI implementation
- Three subscription tiers with detailed feature comparison
- **Free Tier**: $0 - 1 device, 720p/1080p streaming, 30 min AI content submission
- **Standard Tier**: $3.99/month - 3 devices, 1080p streaming, 1hr 15min AI content submission (Most Popular)
- **Plus Tier**: $8.99/month - 3 devices, 4K streaming, 2hr 30min AI content submission
- Visual tier selection with checkmarks and feature lists
- Clickable "View Submission Rules & Guidelines" opens comprehensive rules dialog
- Content review policy disclaimer
- Continue button (disabled until plan is selected)
- Plan change information

### 5. Submission Rules & Guidelines Dialog
- **Android/Desktop**: Full-screen dialog with scrollable content
- **iOS**: Full-screen cover with native styling
- Comprehensive submission guidelines including:
  - Content types (mini-series, feature films, single parts)
  - Accepted formats and duration limits by tier
  - Submission form requirements and review process
  - Blockchain provenance and approval workflow
  - Examples and quick tips for content creators

### 6. Payment Screen
- **Android/Desktop**: Jetpack Compose implementation with Stripe SDK integration
- **iOS**: SwiftUI implementation with Stripe SDK integration
- Selected plan summary with pricing details
- Stripe payment form placeholder (secure payment processing)
- Payment processing states with loading indicators
- Terms and conditions acceptance
- Cancel and back navigation options
- PCI compliance through Stripe SDK

### 7. Netflix-Style Main Screen
- **Android/Desktop**: Jetpack Compose implementation
- **iOS**: SwiftUI implementation
- Netflix-inspired layout with horizontal scrolling content rows
- Featured content hero section with play button
- Multiple content categories (Trending Now, AI Originals, Recently Added, Top Rated)
- Sample AI-generated content with ratings and durations
- Top navigation bar with search and profile icons
- Starry night background consistent with app theme
- Content cards with gradient overlays and metadata display

## File Structure

### Android/Desktop (Jetpack Compose)
```
composeApp/src/commonMain/kotlin/com/btd/maiscreen/
├── App.kt                           # Main app navigation
├── ui/screens/
│   ├── SplashScreen.kt             # Splash screen implementation
│   ├── LoginScreen.kt              # Login screen implementation
│   ├── SignUpScreen.kt             # Sign up screen implementation
│   ├── SubscriptionScreen.kt       # Subscription plan selection screen
│   ├── SubmissionRulesDialog.kt    # Submission rules and guidelines dialog
│   ├── PaymentScreen.kt            # Payment processing with Stripe SDK
│   └── MainScreen.kt               # Netflix-style main content screen
```

### iOS (SwiftUI)
```
iosApp/iosApp/
├── ContentView.swift               # Main app navigation
├── Views/
│   ├── SplashView.swift           # Splash screen implementation
│   ├── LoginView.swift            # Login screen implementation
│   ├── SignUpView.swift           # Sign up screen implementation
│   ├── SubscriptionView.swift     # Subscription plan selection screen
│   ├── SubmissionRulesView.swift  # Submission rules and guidelines view
│   ├── PaymentView.swift          # Payment processing with Stripe SDK
│   └── MainView.swift             # Netflix-style main content screen
```

## Running the Application

### Android
```bash
./gradlew :composeApp:assembleDebug
```

### Desktop
```bash
./gradlew :composeApp:run
```

### iOS
Open `iosApp/iosApp.xcodeproj` in Xcode and run

### Web
```bash
./gradlew :shared:jsBrowserDevelopmentLibraryDistribution
cd webApp
npm install
npm run start
```

## Navigation Flow

1. **App Launch** → **Splash Screen**
   - Shows mAIScreen branding
   - Animated progress bar (2 seconds)
   - Automatic transition to login

2. **Splash Screen** → **Login Screen**
   - Email/password input
   - Login button (TODO: implement authentication)
   - Sign up button (navigates to sign up screen)
   - Forgot password (TODO: implement password reset)

3. **Login Screen** → **Sign Up Screen**
   - First name, last name input
   - Email and confirm email input
   - Password and confirm password input
   - Real-time form validation
   - Sign up button (TODO: implement registration)
   - Back to login navigation

4. **Sign Up Screen** → **Subscription Plan Selection**
   - After successful registration and backend validation
   - Automatic transition to plan selection

5. **Subscription Plan Selection** → **Payment Screen**
   - User selects Free, Standard, or Plus plan
   - Continue button navigates to payment processing

6. **Payment Screen** → **Netflix-Style Main Screen**
   - Secure Stripe payment processing
   - After successful payment, navigate to main content screen
   - Free plan bypasses payment and goes directly to main screen

7. **Main Screen** → **Content Browsing Experience**
   - Netflix-inspired layout with content categories
   - Featured content hero section
   - Horizontal scrolling content rows
   - Search and profile navigation

8. **Submission Rules Dialog**
   - Accessible from any subscription tier card
   - Comprehensive guidelines for content submission
   - Duration limits and requirements by tier

## Next Steps

1. **Stripe SDK Integration**
   - Replace payment form placeholder with actual Stripe Elements
   - Implement secure payment processing and validation
   - Add payment method selection (cards, digital wallets)
   - Configure webhook endpoints for payment confirmations
   - Handle payment errors and retry logic

2. **Content Management System**
   - Implement actual content loading from backend API
   - Add content search and filtering functionality
   - Create content player/viewer screens for streaming
   - Add user profile and account settings screens
   - Implement content recommendations and personalization

3. **Authentication Backend Integration**
   - Implement actual login/signup API calls
   - Add proper form validation and error handling
   - Integrate with authentication service and user sessions
   - Add password reset and email verification flows

4. **Streaming Infrastructure**
   - Implement video/audio streaming capabilities
   - Add content delivery network (CDN) integration
   - Create content upload and processing workflows
   - Add content moderation and approval systems

## Design Notes

- **Color Scheme**: Navy blue (#1B2951) to black gradient background with starry night sky
- **Background**: Vertical gradient from black (top) to navy blue (bottom)
- **Stars**: Randomly positioned white dots with twinkling animation in the upper portion
- **Typography**: White text on dark background for optimal contrast
- **Input Fields**: Transparent background with white borders and text
- **Buttons**: White primary button with navy text, outlined secondary button
- **Spacing**: Consistent 24dp padding and spacing
- **Components**: Modern rounded corners (12dp radius)
- **Responsiveness**: Layouts adapt to different screen sizes
- **Animations**: Smooth progress loading and subtle star twinkling effects
