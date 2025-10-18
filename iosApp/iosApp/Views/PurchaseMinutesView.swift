import SwiftUI

struct MinutePackage: Identifiable {
    let id: String
    let minutes: Int
    let price: String
    let priceValue: Double
    let pricePerMinute: String
    let isPopular: Bool
}

struct PurchaseMinutesView: View {
    @State private var selectedPackage: MinutePackage?
    let currentMinutes: Int
    let onPackageSelected: (MinutePackage) -> Void
    let onPurchaseClick: (MinutePackage) -> Void
    let onBackClick: () -> Void
    
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
    
    // Define minute packages
    let packages: [MinutePackage] = [
        MinutePackage(
            id: "minutes_30",
            minutes: 30,
            price: "$2.49",
            priceValue: 2.49,
            pricePerMinute: "$0.08/min",
            isPopular: false
        ),
        MinutePackage(
            id: "minutes_60",
            minutes: 60,
            price: "$4.49",
            priceValue: 4.49,
            pricePerMinute: "$0.07/min",
            isPopular: false
        ),
        MinutePackage(
            id: "minutes_180",
            minutes: 180,
            price: "$11.99",
            priceValue: 11.99,
            pricePerMinute: "$0.07/min",
            isPopular: true
        ),
        MinutePackage(
            id: "minutes_360",
            minutes: 360,
            price: "$19.99",
            priceValue: 19.99,
            pricePerMinute: "$0.06/min",
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
            GeometryReader { geometry in
                ForEach(0..<stars.count, id: \.self) { index in
                    let star = stars[index]
                    Circle()
                        .fill(starWhite.opacity(Double.random(in: 0.2...0.5)))
                        .frame(width: star.size, height: star.size)
                        .position(
                            x: star.x * geometry.size.width,
                            y: star.y * geometry.size.height
                        )
                }
            }
            
            ScrollView {
                VStack(spacing: 24) {
                    Spacer()
                        .frame(height: 20)
                    
                    // Header with back button
                    HStack {
                        Button(action: onBackClick) {
                            HStack(spacing: 4) {
                                Image(systemName: "chevron.left")
                                Text("Back")
                            }
                            .foregroundColor(.white)
                            .font(.system(size: 16))
                        }
                        Spacer()
                    }
                    .padding(.horizontal, 24)
                    
                    // Title and current balance
                    VStack(spacing: 16) {
                        Text("Purchase Minutes")
                            .font(.system(size: 32, weight: .bold))
                            .foregroundColor(.white)
                            .multilineTextAlignment(.center)
                        
                        Text("Get more time to submit your AI content")
                            .font(.system(size: 16))
                            .foregroundColor(.white.opacity(0.8))
                            .multilineTextAlignment(.center)
                        
                        // Current balance card
                        VStack(spacing: 8) {
                            Text("Current Balance")
                                .font(.system(size: 14))
                                .foregroundColor(.white.opacity(0.7))
                            Text("\(currentMinutes) Minutes")
                                .font(.system(size: 28, weight: .bold))
                                .foregroundColor(goldAccent)
                        }
                        .frame(maxWidth: .infinity)
                        .padding(20)
                        .background(Color.white.opacity(0.1))
                        .cornerRadius(12)
                    }
                    .padding(.horizontal, 24)
                    
                    // Package cards
                    VStack(spacing: 16) {
                        ForEach(packages) { pkg in
                            MinutePackageCard(
                                pkg: pkg,
                                isSelected: selectedPackage?.id == pkg.id,
                                onClick: {
                                    selectedPackage = pkg
                                    onPackageSelected(pkg)
                                },
                                navyBlue: navyBlue,
                                goldAccent: goldAccent
                            )
                        }
                    }
                    .padding(.horizontal, 24)
                    
                    // Info box
                    VStack(alignment: .leading, spacing: 12) {
                        HStack(alignment: .top) {
                            Text("ℹ️")
                                .font(.system(size: 20))
                            VStack(alignment: .leading, spacing: 4) {
                                Text("About Purchased Minutes")
                                    .font(.system(size: 16, weight: .bold))
                                    .foregroundColor(.white)
                            }
                        }
                        
                        Text("""
                        • Purchased minutes never expire
                        • Use them anytime to submit content
                        • Combine with your subscription minutes
                        • One-time purchase, no recurring charges
                        """)
                        .font(.system(size: 14))
                        .foregroundColor(.white.opacity(0.8))
                        .lineSpacing(4)
                    }
                    .padding(20)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(Color.white.opacity(0.1))
                    .cornerRadius(12)
                    .padding(.horizontal, 24)
                    
                    // Purchase button
                    Button(action: {
                        if let pkg = selectedPackage {
                            onPurchaseClick(pkg)
                        }
                    }) {
                        Text(selectedPackage != nil ? 
                             "Purchase \(selectedPackage!.minutes) Minutes for \(selectedPackage!.price)" : 
                             "Select a Package")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(navyBlue)
                            .frame(maxWidth: .infinity)
                            .frame(height: 56)
                            .background(selectedPackage != nil ? goldAccent : Color.white.opacity(0.3))
                            .cornerRadius(12)
                    }
                    .disabled(selectedPackage == nil)
                    .padding(.horizontal, 24)
                    
                    Spacer()
                        .frame(height: 40)
                }
            }
        }
    }
}

struct MinutePackageCard: View {
    let pkg: MinutePackage
    let isSelected: Bool
    let onClick: () -> Void
    let navyBlue: Color
    let goldAccent: Color
    
    var body: some View {
        Button(action: onClick) {
            ZStack {
                VStack(alignment: .leading, spacing: 0) {
                    // Popular badge
                    if pkg.isPopular {
                        HStack {
                            Spacer()
                            Text("BEST VALUE")
                                .font(.system(size: 12, weight: .bold))
                                .foregroundColor(navyBlue)
                                .padding(.horizontal, 16)
                                .padding(.vertical, 8)
                                .background(goldAccent)
                                .cornerRadius(16, corners: [.topRight, .bottomLeft])
                        }
                    }
                    
                    VStack(alignment: .leading, spacing: 16) {
                        // Title and price
                        HStack {
                            VStack(alignment: .leading, spacing: 4) {
                                Text("\(pkg.minutes) Minutes")
                                    .font(.system(size: 24, weight: .bold))
                                    .foregroundColor(.white)
                                Text(pkg.pricePerMinute)
                                    .font(.system(size: 14))
                                    .foregroundColor(.white.opacity(0.7))
                            }
                            
                            Spacer()
                            
                            Text(pkg.price)
                                .font(.system(size: 32, weight: .bold))
                                .foregroundColor(isSelected ? goldAccent : .white)
                        }
                        
                        // Description
                        Text(getDescription(for: pkg.minutes))
                            .font(.system(size: 14))
                            .foregroundColor(.white.opacity(0.8))
                            .lineSpacing(4)
                        
                        // Selection indicator
                        if isSelected {
                            HStack(spacing: 8) {
                                Text("✓")
                                    .font(.system(size: 20, weight: .bold))
                                    .foregroundColor(goldAccent)
                                Text("Selected")
                                    .font(.system(size: 14, weight: .medium))
                                    .foregroundColor(goldAccent)
                            }
                        }
                    }
                    .padding(24)
                }
                .frame(maxWidth: .infinity)
                .background(isSelected ? Color.white.opacity(0.15) : Color.white.opacity(0.05))
                .cornerRadius(16)
                .overlay(
                    RoundedRectangle(cornerRadius: 16)
                        .stroke(isSelected ? goldAccent : Color.white.opacity(0.3), 
                               lineWidth: isSelected ? 3 : 1)
                )
            }
        }
        .buttonStyle(PlainButtonStyle())
    }
    
    private func getDescription(for minutes: Int) -> String {
        switch minutes {
        case 30:
            return "Perfect for trying out additional minutes"
        case 60:
            return "Great for occasional content creators"
        case 180:
            return "Best value for regular submissions"
        case 360:
            return "Maximum savings for frequent creators"
        default:
            return "Add more submission time"
        }
    }
}

// Helper extension for custom corner radius
extension View {
    func cornerRadius(_ radius: CGFloat, corners: UIRectCorner) -> some View {
        clipShape(RoundedCorner(radius: radius, corners: corners))
    }
}

struct RoundedCorner: Shape {
    var radius: CGFloat = .infinity
    var corners: UIRectCorner = .allCorners

    func path(in rect: CGRect) -> Path {
        let path = UIBezierPath(
            roundedRect: rect,
            byRoundingCorners: corners,
            cornerRadii: CGSize(width: radius, height: radius)
        )
        return Path(path.cgPath)
    }
}

