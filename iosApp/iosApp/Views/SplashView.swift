import SwiftUI

struct SplashView: View {
    @State private var progress: Double = 0.0
    @State private var isComplete = false
    @State private var starOpacity: Double = 0.8
    let onSplashComplete: () -> Void
    
    // Define colors
    let navyBlue = Color(red: 0.106, green: 0.161, blue: 0.318) // #1B2951
    let black = Color.black
    let starWhite = Color.white
    
    // Generate random stars
    let stars: [(x: Double, y: Double, size: Double)] = {
        return (0..<100).map { _ in
            (
                x: Double.random(in: 0...1),
                y: Double.random(in: 0...0.6), // Stars only in top 60%
                size: Double.random(in: 2...6)
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
                    .fill(starWhite.opacity(starOpacity * Double.random(in: 0.3...1.0)))
                    .frame(width: star.size, height: star.size)
                    .position(
                        x: star.x * UIScreen.main.bounds.width,
                        y: star.y * UIScreen.main.bounds.height
                    )
            }
            
            VStack(spacing: 32) {
                // App Title
                VStack(spacing: 8) {
                    Text("mAIScreen")
                        .font(.system(size: 48, weight: .bold, design: .default))
                        .foregroundColor(.white)
                    
                    Text("AI-Powered Screen Intelligence")
                        .font(.system(size: 16))
                        .foregroundColor(.white.opacity(0.8))
                        .multilineTextAlignment(.center)
                }
                
                Spacer()
                    .frame(height: 48)
                
                // Loading Progress
                VStack(spacing: 16) {
                    // Progress Bar
                    ProgressView(value: progress, total: 1.0)
                        .progressViewStyle(LinearProgressViewStyle(tint: .white))
                        .frame(width: 200, height: 6)
                        .scaleEffect(x: 1, y: 2, anchor: .center)
                    
                    // Progress Text
                    Text("Loading... \(Int(progress * 100))%")
                        .font(.system(size: 14))
                        .foregroundColor(.white.opacity(0.7))
                }
            }
            .padding(.horizontal, 24)
        }
        .onAppear {
            startLoading()
            startStarTwinkle()
        }
    }
    
    private func startLoading() {
        // Animate progress from 0 to 1 over 2 seconds
        withAnimation(.easeInOut(duration: 2.0)) {
            progress = 1.0
        }
        
        // Complete splash after animation finishes
        DispatchQueue.main.asyncAfter(deadline: .now() + 2.5) {
            onSplashComplete()
        }
    }
    
    private func startStarTwinkle() {
        withAnimation(.easeInOut(duration: 1.5).repeatForever(autoreverses: true)) {
            starOpacity = 0.4
        }
    }
}

#Preview {
    SplashView {
        print("Splash completed")
    }
}
