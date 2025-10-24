import SwiftUI
import AVKit
import AVFoundation

struct VideoPlayerView: View {
    let content: ContentItem
    let onDismiss: () -> Void
    let onPlayerError: (String) -> Void
    
    @State private var player: AVPlayer?
    @State private var isLoading = true
    @State private var errorMessage: String?
    @State private var showReportSheet = false
    
    var body: some View {
        ZStack {
            Color.black.ignoresSafeArea()
            
            if let player = player, errorMessage == nil {
                VideoPlayer(player: player)
                    .onAppear {
                        setupPlayer()
                    }
                    .onDisappear {
                        player.pause()
                    }
            }
            
            // Loading indicator
            if isLoading && errorMessage == nil {
                VStack(spacing: 16) {
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: .white))
                        .scaleEffect(1.5)
                    
                    Text("Loading \(content.title)...")
                        .foregroundColor(.white)
                        .font(.system(size: 16))
                }
            }
            
            // Error message
            if let errorMessage = errorMessage {
                VStack(spacing: 16) {
                    Text("⚠️")
                        .font(.system(size: 48))
                    
                    Text("Playback Error")
                        .foregroundColor(.white)
                        .font(.system(size: 20, weight: .bold))
                    
                    Text(errorMessage)
                        .foregroundColor(.white.opacity(0.8))
                        .font(.system(size: 14))
                        .multilineTextAlignment(.center)
                    
                    Button("Go Back") {
                        onDismiss()
                    }
                    .foregroundColor(.black)
                    .padding(.horizontal, 24)
                    .padding(.vertical, 12)
                    .background(Color.white)
                    .cornerRadius(8)
                }
                .padding()
            }
            
            // Top controls overlay
            VStack {
                HStack {
                    // Close button (top-left)
                    Button(action: onDismiss) {
                        Image(systemName: "xmark")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(.white)
                            .frame(width: 48, height: 48)
                            .background(Color.black.opacity(0.7))
                            .clipShape(Circle())
                    }
                    .padding()
                    
                    Spacer()
                    
                    // Report/Flag button (top-right)
                    Button(action: { showReportSheet = true }) {
                        Text("🚩")
                            .font(.system(size: 24))
                            .frame(width: 48, height: 48)
                            .background(Color.black.opacity(0.7))
                            .clipShape(Circle())
                    }
                    .padding()
                }
                
                Spacer()
            }
        }
        .navigationBarHidden(true)
        .onAppear {
            setupPlayer()
        }
        .sheet(isPresented: $showReportSheet) {
            ReportVideoSheet(
                contentTitle: content.title,
                onDismiss: { showReportSheet = false },
                onReportSubmit: { reason, details in
                    print("🚩 Report submitted for '\(content.title)': \(reason) - \(details)")
                    // TODO: Send report to backend
                    showReportSheet = false
                }
            )
        }
    }
    
    private func setupPlayer() {
        // For iOS, we'll use the streaming API to get the HLS manifest
        // This is a simplified version - in production, integrate with the KMP streaming API
        
        // Simulate streaming API call - replace with actual KMP integration
        Task {
            do {
                // This would call the KMP streaming API
                let manifestUrl = await getStreamingUrl(assetId: content.assetId ?? content.id)
                
                guard let url = URL(string: manifestUrl) else {
                    await MainActor.run {
                        errorMessage = "Invalid streaming URL"
                        isLoading = false
                        onPlayerError("Invalid streaming URL")
                    }
                    return
                }
                
                await MainActor.run {
                    let playerItem = AVPlayerItem(url: url)
                    let newPlayer = AVPlayer(playerItem: playerItem)
                    setupPlayerObservers(player: newPlayer, playerItem: playerItem)
                    self.player = newPlayer
                }
            } catch {
                await MainActor.run {
                    errorMessage = "Failed to load stream: \(error.localizedDescription)"
                    isLoading = false
                    onPlayerError(errorMessage!)
                }
            }
        }
    }
    
    private func getStreamingUrl(assetId: String) async -> String {
        // This is a placeholder - in production, integrate with KMP PlaybackApi
        // For now, return test HLS stream
        return "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
    }
    
    private func setupPlayerObservers(player: AVPlayer, playerItem: AVPlayerItem) {
        
        // Add observer for player status
        NotificationCenter.default.addObserver(
            forName: .AVPlayerItemDidPlayToEndTime,
            object: playerItem,
            queue: .main
        ) { _ in
            // Video finished playing
            newPlayer.seek(to: .zero)
        }
        
        // Auto-play when ready
        player.addObserver(forKeyPath: "status") { player, _ in
            DispatchQueue.main.async {
                if player.status == .readyToPlay {
                    isLoading = false
                    player.play()
                } else if player.status == .failed {
                    isLoading = false
                    errorMessage = player.error?.localizedDescription ?? "Unknown playback error"
                    onPlayerError(errorMessage ?? "Unknown error")
                }
            }
        }
    }
}

extension AVPlayer {
    func addObserver(forKeyPath keyPath: String, handler: @escaping (AVPlayer, Any?) -> Void) {
        // Simplified observer - in production, use proper KVO
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
            handler(self, nil)
        }
    }
}

struct ReportVideoSheet: View {
    let contentTitle: String
    let onDismiss: () -> Void
    let onReportSubmit: (String, String) -> Void
    
    @State private var selectedReason: String = ""
    @State private var additionalDetails: String = ""
    
    let reportReasons = [
        "Inappropriate Content",
        "Copyright Violation",
        "Misleading or Spam",
        "Violence or Harmful Content",
        "Hate Speech",
        "Other"
    ]
    
    var body: some View {
        NavigationView {
            VStack(alignment: .leading, spacing: 20) {
                // Header
                HStack(spacing: 8) {
                    Text("🚩")
                        .font(.system(size: 28))
                    Text("Report Video")
                        .font(.system(size: 24, weight: .bold))
                }
                .padding(.top, 8)
                
                Text("Report: \"\(contentTitle)\"")
                    .font(.system(size: 14))
                    .foregroundColor(.gray)
                
                Divider()
                
                // Question
                Text("Why are you reporting this video?")
                    .font(.system(size: 16, weight: .medium))
                
                // Report reasons
                ScrollView {
                    VStack(alignment: .leading, spacing: 12) {
                        ForEach(reportReasons, id: \.self) { reason in
                            Button(action: {
                                selectedReason = reason
                            }) {
                                HStack {
                                    Image(systemName: selectedReason == reason ? "circle.fill" : "circle")
                                        .foregroundColor(selectedReason == reason ? .blue : .gray)
                                    Text(reason)
                                        .foregroundColor(.primary)
                                        .font(.system(size: 15))
                                    Spacer()
                                }
                                .padding(.vertical, 4)
                            }
                        }
                    }
                }
                
                // Additional details
                VStack(alignment: .leading, spacing: 8) {
                    Text("Additional details (optional)")
                        .font(.system(size: 14))
                        .foregroundColor(.gray)
                    
                    TextEditor(text: $additionalDetails)
                        .frame(height: 100)
                        .padding(8)
                        .overlay(
                            RoundedRectangle(cornerRadius: 8)
                                .stroke(Color.gray.opacity(0.3), lineWidth: 1)
                        )
                        .overlay(
                            Group {
                                if additionalDetails.isEmpty {
                                    Text("Provide more information...")
                                        .foregroundColor(.gray.opacity(0.5))
                                        .padding(.leading, 12)
                                        .padding(.top, 16)
                                        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
                                        .allowsHitTesting(false)
                                }
                            }
                        )
                }
                
                Spacer()
                
                // Action buttons
                HStack(spacing: 12) {
                    Button(action: onDismiss) {
                        Text("Cancel")
                            .font(.system(size: 16, weight: .medium))
                            .frame(maxWidth: .infinity)
                            .frame(height: 50)
                            .foregroundColor(.blue)
                            .background(Color.gray.opacity(0.1))
                            .cornerRadius(12)
                    }
                    
                    Button(action: {
                        if !selectedReason.isEmpty {
                            onReportSubmit(selectedReason, additionalDetails)
                        }
                    }) {
                        Text("Submit Report")
                            .font(.system(size: 16, weight: .medium))
                            .frame(maxWidth: .infinity)
                            .frame(height: 50)
                            .foregroundColor(.white)
                            .background(selectedReason.isEmpty ? Color.gray : Color.red)
                            .cornerRadius(12)
                    }
                    .disabled(selectedReason.isEmpty)
                }
            }
            .padding()
            .navigationBarHidden(true)
        }
    }
}

#Preview {
    VideoPlayerView(
        content: ContentItem(
            title: "Sample Video",
            description: "Test video",
            synopsis: "Test synopsis",
            thumbnailColor: .blue,
            category: "Test",
            duration: "2h",
            rating: "★ 5.0",
            year: "2024",
            ageRating: "PG-13",
            genres: ["Test"],
            cast: ["Test Actor"],
            director: "Test Director",
            assetId: "ASSET001"
        ),
        onDismiss: {},
        onPlayerError: { _ in }
    )
}


