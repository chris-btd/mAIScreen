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
            
            // Back button overlay
            VStack {
                HStack {
                    Button(action: onDismiss) {
                        Image(systemName: "xmark")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(.white)
                            .frame(width: 32, height: 32)
                            .background(Color.black.opacity(0.5))
                            .clipShape(Circle())
                    }
                    .padding()
                    
                    Spacer()
                }
                
                Spacer()
            }
        }
        .navigationBarHidden(true)
        .onAppear {
            setupPlayer()
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


