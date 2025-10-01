import SwiftUI

struct SubmissionRulesView: View {
    let tierName: String
    @Binding var isPresented: Bool
    
    let navyBlue = Color(red: 0.106, green: 0.161, blue: 0.318)
    let black = Color.black
    let starWhite = Color.white
    
    // Generate random stars
    let stars: [(x: Double, y: Double, size: Double)] = {
        return (0..<40).map { _ in
            (
                x: Double.random(in: 0...1),
                y: Double.random(in: 0...0.3),
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
            
            // Content card
            VStack(spacing: 0) {
                // Header
                HStack {
                    Text("Submission Rules & Guidelines")
                        .font(.system(size: 20, weight: .bold))
                        .foregroundColor(navyBlue)
                    
                    Spacer()
                    
                    Button("Close") {
                        isPresented = false
                    }
                    .foregroundColor(navyBlue)
                }
                .padding(.horizontal, 24)
                .padding(.vertical, 16)
                .background(Color.white)
                
                // Scrollable content
                ScrollView {
                    VStack(alignment: .leading, spacing: 16) {
                        // What you can submit
                        SectionHeaderView(title: "What you can submit")
                        BulletPointView(text: "Mini-series — upload all episodes at once as a single submission.")
                        BulletPointView(text: "Feature film — one full-length movie as a single submission.")
                        BulletPointView(text: "Single part — one episode, short, or reel per submission.")
                        BulletPointView(text: "Accepted formats: video (e.g., MP4), audio (e.g., MP3/WAV), and required artwork (poster/thumbnail).")
                        
                        // Duration limits
                        SectionHeaderView(title: "Duration limits by tier (per submission)")
                        
                        // Tier limits table
                        VStack(spacing: 8) {
                            HStack {
                                Text("Tier")
                                    .font(.system(size: 14, weight: .bold))
                                    .foregroundColor(navyBlue)
                                    .frame(maxWidth: .infinity, alignment: .leading)
                                
                                Text("Max combined duration")
                                    .font(.system(size: 14, weight: .bold))
                                    .foregroundColor(navyBlue)
                                    .frame(maxWidth: .infinity, alignment: .trailing)
                            }
                            
                            TierLimitRowView(tier: "Free", limit: "30 minutes")
                            TierLimitRowView(tier: "Standard", limit: "1 hour 15 minutes")
                            TierLimitRowView(tier: "Plus", limit: "2 hours 30 minutes")
                        }
                        .padding(16)
                        .background(Color(UIColor.systemGray6))
                        .cornerRadius(8)
                        
                        InfoTextView(text: "Combined duration = the total runtime of everything included in that submission (e.g., all episodes in a mini-series submitted together).")
                        InfoTextView(text: "If you submit one episode/short at a time, each submission must be within your tier's limit.")
                        
                        // Examples
                        SectionHeaderView(title: "Examples")
                        BulletPointView(text: "6 episodes × 5 minutes = 30 minutes → fits Free (as one submission).")
                        BulletPointView(text: "1 feature = 1h 10m → fits Standard.")
                        BulletPointView(text: "12 shorts × 12 minutes = 144 minutes → needs Plus (2h 24m total).")
                        
                        // Submission form
                        SectionHeaderView(title: "Submission form (required)")
                        InfoTextView(text: "Every submission must include a completed form so we can review and (if approved) mint its provenance on the blockchain. You'll be asked for:")
                        BulletPointView(text: "Title, synopsis, genres, and poster/thumbnail")
                        BulletPointView(text: "Episode list (if applicable) and runtimes")
                        BulletPointView(text: "Declared rating and any content warnings")
                        BulletPointView(text: "Prompt origin details (where you created the prompts/models)")
                        BulletPointView(text: "A statement that you own the rights to submit the AI-generated content")
                        
                        // Review process
                        SectionHeaderView(title: "Review process")
                        BulletPointView(text: "Timeline: Reviews can take up to 7 days.")
                        BulletPointView(text: "Outcome: You'll be notified if your content is accepted or rejected.")
                        BulletPointView(text: "No penalty for rejection: Rejected submissions do not count against your tier's usage limit and can be resubmitted anytime after updates or fixes.")
                        
                        // After approval
                        SectionHeaderView(title: "After approval")
                        InfoTextView(text: "Upon acceptance, we record the submission's metadata (including prompt origin and content hashes) on-chain for provenance and ownership. There are no purchases on or from the blockchain.")
                        
                        // Quick tips
                        SectionHeaderView(title: "Quick tips")
                        BulletPointView(text: "Keep the total runtime of each submission within your tier's limit.")
                        BulletPointView(text: "If you have a long mini-series, submit in parts (each under the limit) or upgrade your tier.")
                        BulletPointView(text: "Fill out the form accurately—missing info can delay review.")
                        
                        Spacer()
                            .frame(height: 16)
                    }
                    .padding(.horizontal, 24)
                    .padding(.vertical, 16)
                }
                .background(Color.white)
            }
            .background(Color.white)
            .cornerRadius(16)
            .padding(16)
        }
    }
}

struct SectionHeaderView: View {
    let title: String
    let navyBlue = Color(red: 0.106, green: 0.161, blue: 0.318)
    
    var body: some View {
        Text(title)
            .font(.system(size: 16, weight: .bold))
            .foregroundColor(navyBlue)
            .padding(.vertical, 4)
    }
}

struct BulletPointView: View {
    let text: String
    
    var body: some View {
        HStack(alignment: .top, spacing: 8) {
            Text("•")
                .font(.system(size: 14))
                .foregroundColor(Color(UIColor.systemGray))
            
            Text(text)
                .font(.system(size: 14))
                .foregroundColor(Color(UIColor.darkGray))
                .fixedSize(horizontal: false, vertical: true)
        }
    }
}

struct InfoTextView: View {
    let text: String
    
    var body: some View {
        Text(text)
            .font(.system(size: 14))
            .foregroundColor(Color(UIColor.systemGray))
            .padding(.vertical, 4)
    }
}

struct TierLimitRowView: View {
    let tier: String
    let limit: String
    
    var body: some View {
        HStack {
            Text(tier)
                .font(.system(size: 14))
                .foregroundColor(Color(UIColor.darkGray))
                .frame(maxWidth: .infinity, alignment: .leading)
            
            Text(limit)
                .font(.system(size: 14))
                .foregroundColor(Color(UIColor.darkGray))
                .frame(maxWidth: .infinity, alignment: .trailing)
        }
    }
}

#Preview {
    SubmissionRulesView(tierName: "Free", isPresented: .constant(true))
}
