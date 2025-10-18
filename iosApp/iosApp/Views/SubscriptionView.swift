import SwiftUI

struct SubscriptionTier {
    let name: String
    let price: String
    let priceSubtext: String
    let deviceLimit: String
    let features: [String]
    let isPopular: Bool
}

struct SubscriptionView: View {
    @State private var selectedTier: SubscriptionTier?
    @State private var showRulesDialog = false
    @State private var selectedRulesTier = ""
    
    let onTierSelected: (SubscriptionTier) -> Void
    let onContinueClick: () -> Void
    let onSubmissionRulesClick: (String) -> Void
    
    // Define colors
    let navyBlue = Color(red: 0.106, green: 0.161, blue: 0.318) // #1B2951
    let black = Color.black
    let starWhite = Color.white
    let goldAccent = Color(red: 1.0, green: 0.843, blue: 0.0) // #FFD700
    
    // Generate random stars
    let stars: [(x: Double, y: Double, size: Double)] = {
        return (0..<60).map { _ in
            (
                x: Double.random(in: 0...1),
                y: Double.random(in: 0...0.3), // Stars only in top 30%
                size: Double.random(in: 1.5...4)
            )
        }
    }()
    
    // Define subscription tiers
    let tiers: [SubscriptionTier] = [
        SubscriptionTier(
            name: "Free",
            price: "$0",
            priceSubtext: "Forever",
            deviceLimit: "30 Minutes",
            features: [
                "Stream to only one device at a time",
                "Community Support",
                "720p and 1080p resolution streaming",
                "The ability to submit up to 30 minutes of your own AI generated content to be featured on our platform. Click rules guide below",
                "⚠️ Free tier minutes do not roll over each month"
            ],
            isPopular: false
        ),
        SubscriptionTier(
            name: "Standard",
            price: "$3.99",
            priceSubtext: "per month",
            deviceLimit: "1 Hour 15 Min",
            features: [
                "Stream on up to 3 devices simultaneously",
                "Community Support",
                "1080p resolution streaming",
                "The ability to submit up to 1 hour and 15 minutes of your own AI generated content to be featured on our platform. Click rules guide below",
                "✨ Unused minutes roll over to the next month!"
            ],
            isPopular: true
        ),
        SubscriptionTier(
            name: "Plus",
            price: "$8.99",
            priceSubtext: "per month",
            deviceLimit: "2 Hours 30 Min",
            features: [
                "Stream on up to 3 devices simultaneously",
                "Community Support",
                "4K resolution streaming",
                "The ability to submit up to 2 hours and 30 minutes of your own AI generated content to be featured on our platform. Click rules guide below",
                "✨ Unused minutes roll over to the next month!"
            ],
            isPopular: false
        )
    ]
    
    var body: some View {
        ZStack {
            // Background gradient
            LinearGradient(
                gradient: Gradient(colors: [black, navyBlue]),
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()
            
            // Stars
            ForEach(0..<stars.count, id: \.self) { index in
                let star = stars[index]
                Circle()
                    .fill(starWhite.opacity(Double.random(in: 0.3...0.8)))
                    .frame(width: star.size, height: star.size)
                    .position(
                        x: star.x * UIScreen.main.bounds.width,
                        y: star.y * UIScreen.main.bounds.height
                    )
            }
            
            ScrollView {
                VStack(spacing: 24) {
                    Spacer()
                        .frame(height: 40)
                    
                    // Header
                    VStack(spacing: 8) {
                        Text("Choose Your Plan")
                            .font(.system(size: 32, weight: .bold))
                            .foregroundColor(.white)
                            .multilineTextAlignment(.center)
                        
                        Text("Select the streaming plan that fits your needs")
                            .font(.system(size: 16))
                            .foregroundColor(.white.opacity(0.8))
                            .multilineTextAlignment(.center)
                    }
                    
                    Spacer()
                        .frame(height: 8)
                    
                    // Subscription Tiers
                    ForEach(Array(tiers.enumerated()), id: \.offset) { index, tier in
                        SubscriptionTierCard(
                            tier: tier,
                            isSelected: selectedTier?.name == tier.name,
                            onTierClick: {
                                selectedTier = tier
                                onTierSelected(tier)
                            },
                            onSubmissionRulesClick: {
                                selectedRulesTier = tier.name
                                showRulesDialog = true
                                onSubmissionRulesClick(tier.name)
                            }
                        )
                    }
                    
                    Spacer()
                        .frame(height: 16)
                    
                    // Content Review Disclaimer
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Content Review Policy")
                            .font(.system(size: 14, weight: .bold))
                            .foregroundColor(.white)
                        
                        Text("All AI-generated content will be reviewed for quality, originality, compliance with our Terms of Service, and ratings accuracy. This ensures the highest quality experience for all users.")
                            .font(.system(size: 12))
                            .foregroundColor(.white.opacity(0.8))
                            .multilineTextAlignment(.leading)
                    }
                    .padding(16)
                    .background(Color.white.opacity(0.05))
                    .cornerRadius(12)
                    
                    Spacer()
                        .frame(height: 8)
                    
                    // Continue Button
                    Button(action: onContinueClick) {
                        Text(selectedTier != nil ? "Continue with \(selectedTier!.name)" : "Select a Plan")
                            .font(.system(size: 16, weight: .medium))
                            .frame(maxWidth: .infinity)
                            .frame(height: 56)
                            .background(
                                selectedTier != nil ? Color.white : Color.white.opacity(0.5)
                            )
                            .foregroundColor(navyBlue)
                            .cornerRadius(12)
                    }
                    .disabled(selectedTier == nil)
                    
                    Spacer()
                        .frame(height: 16)
                    
                    // Terms text
                    Text("You can change or cancel your plan anytime")
                        .font(.system(size: 12))
                        .foregroundColor(.white.opacity(0.6))
                        .multilineTextAlignment(.center)
                    
                    Spacer()
                        .frame(height: 20)
                }
                .padding(.horizontal, 24)
            }
        }
        .fullScreenCover(isPresented: $showRulesDialog) {
            SubmissionRulesView(tierName: selectedRulesTier, isPresented: $showRulesDialog)
        }
    }
}

struct SubscriptionTierCard: View {
    let tier: SubscriptionTier
    let isSelected: Bool
    let onTierClick: () -> Void
    let onSubmissionRulesClick: () -> Void
    
    let navyBlue = Color(red: 0.106, green: 0.161, blue: 0.318)
    let goldAccent = Color(red: 1.0, green: 0.843, blue: 0.0)
    
    var body: some View {
        Button(action: onTierClick) {
            ZStack {
                // Card background
                RoundedRectangle(cornerRadius: 16)
                    .fill(Color.white.opacity(0.1))
                    .overlay(
                        RoundedRectangle(cornerRadius: 16)
                            .stroke(isSelected ? Color.white : Color.clear, lineWidth: 2)
                    )
                
                // Popular badge
                if tier.isPopular {
                    VStack {
                        HStack {
                            Spacer()
                            Text("MOST POPULAR")
                                .font(.system(size: 10, weight: .bold))
                                .foregroundColor(navyBlue)
                                .padding(.horizontal, 12)
                                .padding(.vertical, 4)
                                .background(goldAccent)
                                .cornerRadius(12)
                                .padding(.trailing, 16)
                                .padding(.top, 16)
                        }
                        Spacer()
                    }
                }
                
                VStack(spacing: 16) {
                    // Plan name
                    Text(tier.name)
                        .font(.system(size: 24, weight: .bold))
                        .foregroundColor(.white)
                        .multilineTextAlignment(.center)
                    
                    // Price
                    HStack(alignment: .bottom, spacing: 0) {
                        Text(tier.price)
                            .font(.system(size: 36, weight: .bold))
                            .foregroundColor(.white)
                        
                        if !tier.priceSubtext.isEmpty {
                            Text(" \(tier.priceSubtext)")
                                .font(.system(size: 14))
                                .foregroundColor(.white.opacity(0.7))
                                .padding(.bottom, 4)
                        }
                    }
                    
                    // Device limit
                    Text(tier.deviceLimit)
                        .font(.system(size: 16, weight: .medium))
                        .foregroundColor(tier.isPopular ? goldAccent : .white.opacity(0.9))
                        .multilineTextAlignment(.center)
                    
                    // Features
                    VStack(alignment: .leading, spacing: 8) {
                        ForEach(Array(tier.features.enumerated()), id: \.offset) { index, feature in
                            HStack(alignment: .top, spacing: 12) {
                                // Checkmark
                                ZStack {
                                    RoundedRectangle(cornerRadius: 8)
                                        .fill(Color.white)
                                        .frame(width: 16, height: 16)
                                    
                                    Text("✓")
                                        .font(.system(size: 10, weight: .bold))
                                        .foregroundColor(navyBlue)
                                }
                                
                                Text(feature)
                                    .font(.system(size: 14))
                                    .foregroundColor(.white.opacity(0.9))
                                    .multilineTextAlignment(.leading)
                                    .frame(maxWidth: .infinity, alignment: .leading)
                            }
                        }
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    
                    // Submission Rules Link
                    Button(action: onSubmissionRulesClick) {
                        Text("View Submission Rules & Guidelines")
                            .font(.system(size: 12, weight: .medium))
                            .foregroundColor(tier.isPopular ? goldAccent : .white.opacity(0.8))
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 4)
                    }
                    
                    if isSelected {
                        Text("✓ Selected")
                            .font(.system(size: 14, weight: .medium))
                            .foregroundColor(.white)
                            .padding(.horizontal, 16)
                            .padding(.vertical, 8)
                            .background(Color.white.opacity(0.2))
                            .cornerRadius(8)
                    }
                }
                .padding(24)
            }
        }
        .buttonStyle(PlainButtonStyle())
    }
}

#Preview {
    SubscriptionView(
        onTierSelected: { tier in
            print("Tier selected: \(tier.name)")
        },
        onContinueClick: {
            print("Continue tapped")
        },
        onSubmissionRulesClick: { tierName in
            print("Submission rules clicked for: \(tierName)")
        }
    )
}
