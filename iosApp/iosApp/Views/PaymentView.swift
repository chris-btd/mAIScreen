import SwiftUI

struct PaymentView: View {
    let selectedTier: SubscriptionTier
    let onBackClick: () -> Void
    let onPaymentSuccess: () -> Void
    let onPaymentError: (String) -> Void
    
    @State private var isProcessing = false
    
    let navyBlue = Color(red: 0.106, green: 0.161, blue: 0.318)
    let black = Color.black
    let starWhite = Color.white
    
    // Generate random stars
    let stars: [(x: Double, y: Double, size: Double)] = {
        return (0..<60).map { _ in
            (
                x: Double.random(in: 0...1),
                y: Double.random(in: 0...0.4),
                size: Double.random(in: 1.5...4)
            )
        }
    }()
    
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
                    // Top Navigation
                    HStack {
                        Button(action: onBackClick) {
                            Image(systemName: "chevron.left")
                                .font(.system(size: 20, weight: .medium))
                                .foregroundColor(.white)
                        }
                        
                        Text("Complete Your Purchase")
                            .font(.system(size: 20, weight: .bold))
                            .foregroundColor(.white)
                            .padding(.leading, 16)
                        
                        Spacer()
                    }
                    .padding(.horizontal, 24)
                    .padding(.top, 16)
                    
                    // Selected Plan Summary
                    VStack(spacing: 16) {
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Selected Plan")
                                .font(.system(size: 14, weight: .medium))
                                .foregroundColor(.white.opacity(0.7))
                            
                            HStack {
                                Text("\(selectedTier.name) Plan")
                                    .font(.system(size: 18, weight: .bold))
                                    .foregroundColor(.white)
                                
                                Spacer()
                                
                                HStack(alignment: .bottom, spacing: 4) {
                                    Text(selectedTier.price)
                                        .font(.system(size: 24, weight: .bold))
                                        .foregroundColor(.white)
                                    
                                    if !selectedTier.priceSubtext.isEmpty {
                                        Text(selectedTier.priceSubtext)
                                            .font(.system(size: 14))
                                            .foregroundColor(.white.opacity(0.7))
                                            .padding(.bottom, 2)
                                    }
                                }
                            }
                        }
                        .padding(24)
                        .background(Color.white.opacity(0.1))
                        .cornerRadius(12)
                    }
                    .padding(.horizontal, 24)
                    
                    // Payment Method Section
                    VStack(spacing: 16) {
                        VStack(alignment: .leading, spacing: 16) {
                            Text("Payment Method")
                                .font(.system(size: 18, weight: .bold))
                                .foregroundColor(navyBlue)
                                .frame(maxWidth: .infinity, alignment: .leading)
                            
                            // Stripe Payment Element Placeholder
                            VStack(spacing: 8) {
                                Text("🔒 Stripe Payment Form")
                                    .font(.system(size: 16, weight: .medium))
                                    .foregroundColor(Color(UIColor.systemGray))
                                
                                Text("Secure payment processing")
                                    .font(.system(size: 12))
                                    .foregroundColor(Color(UIColor.systemGray2))
                                
                                Text("Card details, billing address, and payment method selection would appear here")
                                    .font(.system(size: 10))
                                    .foregroundColor(Color(UIColor.systemGray2))
                                    .multilineTextAlignment(.center)
                                    .padding(.horizontal, 16)
                            }
                            .frame(maxWidth: .infinity)
                            .frame(height: 200)
                            .background(Color(UIColor.systemGray6))
                            .cornerRadius(8)
                            .overlay(
                                RoundedRectangle(cornerRadius: 8)
                                    .stroke(Color(UIColor.systemGray4), lineWidth: 1)
                            )
                            
                            // Security Notice
                            HStack {
                                Spacer()
                                Text("🔒 Your payment information is secure and encrypted")
                                    .font(.system(size: 12))
                                    .foregroundColor(Color(UIColor.systemGray))
                                    .multilineTextAlignment(.center)
                                Spacer()
                            }
                        }
                        .padding(24)
                        .background(Color.white)
                        .cornerRadius(12)
                    }
                    .padding(.horizontal, 24)
                    
                    // Terms and Conditions
                    Text("By completing this purchase, you agree to our Terms of Service and Privacy Policy. Your subscription will automatically renew unless cancelled.")
                        .font(.system(size: 12))
                        .foregroundColor(.white.opacity(0.7))
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 24)
                    
                    // Payment Button
                    Button(action: {
                        if !isProcessing {
                            isProcessing = true
                            // Simulate payment processing
                            DispatchQueue.main.asyncAfter(deadline: .now() + 2.0) {
                                isProcessing = false
                                onPaymentSuccess()
                            }
                        }
                    }) {
                        HStack {
                            if isProcessing {
                                ProgressView()
                                    .progressViewStyle(CircularProgressViewStyle(tint: navyBlue))
                                    .scaleEffect(0.8)
                                Text("Processing Payment...")
                                    .font(.system(size: 16, weight: .medium))
                            } else {
                                let buttonText = selectedTier.price == "$0" ? 
                                    "Start Free Plan" : 
                                    "Complete Purchase \(selectedTier.price)\(!selectedTier.priceSubtext.isEmpty ? "/\(selectedTier.priceSubtext.replacingOccurrences(of: "per ", with: ""))" : "")"
                                Text(buttonText)
                                    .font(.system(size: 16, weight: .medium))
                            }
                        }
                        .frame(maxWidth: .infinity)
                        .frame(height: 56)
                        .background(isProcessing ? Color.white.opacity(0.5) : Color.white)
                        .foregroundColor(navyBlue)
                        .cornerRadius(12)
                    }
                    .disabled(isProcessing)
                    .padding(.horizontal, 24)
                    
                    // Cancel Button
                    Button(action: onBackClick) {
                        Text("Cancel")
                            .font(.system(size: 16, weight: .medium))
                            .frame(maxWidth: .infinity)
                            .frame(height: 56)
                            .background(Color.clear)
                            .foregroundColor(isProcessing ? .white.opacity(0.5) : .white)
                            .overlay(
                                RoundedRectangle(cornerRadius: 12)
                                    .stroke(isProcessing ? Color.white.opacity(0.5) : Color.white, lineWidth: 1)
                            )
                    }
                    .disabled(isProcessing)
                    .padding(.horizontal, 24)
                    
                    Spacer()
                        .frame(height: 32)
                }
            }
        }
        .navigationBarHidden(true)
    }
}

#Preview {
    PaymentView(
        selectedTier: SubscriptionTier(
            name: "Standard",
            price: "$3.99",
            priceSubtext: "per month",
            deviceLimit: "1 Hour 15 Min",
            features: [],
            isPopular: true
        ),
        onBackClick: {},
        onPaymentSuccess: {},
        onPaymentError: { _ in }
    )
}
