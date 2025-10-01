import SwiftUI

struct ContentItem: Identifiable {
    let id = UUID()
    let title: String
    let description: String
    let synopsis: String
    let thumbnailColor: Color
    let category: String
    let duration: String
    let rating: String
    let year: String
    let ageRating: String
    let genres: [String]
    let cast: [String]
    let director: String
    let videoUrl: String?
    var isLiked: Bool = false
    var isInWatchlist: Bool = false
}

struct ContentCategory: Identifiable {
    let id = UUID()
    let name: String
    let items: [ContentItem]
}

struct MainView: View {
    let onProfileClick: () -> Void
    let onSearchClick: () -> Void
    let onContentClick: (ContentItem) -> Void
    let onPlayContent: (ContentItem) -> Void
    let onToggleLike: (ContentItem) -> Void
    let onToggleWatchlist: (ContentItem) -> Void
    
    @State private var showDetailsSheet = false
    @State private var selectedContent: ContentItem?
    
    let navyBlue = Color(red: 0.106, green: 0.161, blue: 0.318)
    let black = Color.black
    let starWhite = Color.white
    
    // Sample content data
    let featuredContent = ContentItem(
        title: "AI Dreams: The Future Unfolds",
        description: "An immersive journey through AI-generated worlds where reality and imagination blur into stunning visual narratives. Experience the cutting-edge of artificial creativity.",
        synopsis: "In a world where artificial intelligence has evolved beyond human comprehension, a young data scientist discovers a hidden realm within the neural networks. As she navigates through layers of digital consciousness, she uncovers a conspiracy that threatens both the virtual and real worlds. With stunning AI-generated visuals and a gripping narrative, this film explores the boundaries between human creativity and machine intelligence, questioning what it truly means to dream in an age of artificial minds.",
        thumbnailColor: Color(red: 0.392, green: 0.4, blue: 0.945),
        category: "Featured",
        duration: "2h 15m",
        rating: "★ 4.8",
        year: "2024",
        ageRating: "PG-13",
        genres: ["Sci-Fi", "Drama", "Thriller"],
        cast: ["Sarah Chen", "Marcus Rivera", "Dr. Elena Vasquez"],
        director: "AI Director Alpha",
        videoUrl: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
    )
    
    let contentCategories: [ContentCategory] = [
        ContentCategory(
            name: "Trending Now",
            items: [
                ContentItem(title: "Neural Networks", description: "AI explores consciousness", synopsis: "A groundbreaking series that delves into the mysteries of artificial consciousness. Follow Dr. Maya Singh as she develops the first truly sentient AI, only to discover that her creation has its own agenda.", thumbnailColor: Color.red, category: "Sci-Fi", duration: "1h 45m", rating: "★ 4.7", year: "2024", ageRating: "TV-14", genres: ["Sci-Fi", "Drama"], cast: ["Dr. Maya Singh", "ARIA (AI)", "Prof. Chen"], director: "AI Director Beta", videoUrl: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"),
                ContentItem(title: "Digital Horizons", description: "Virtual reality adventures", synopsis: "Step into breathtaking virtual worlds where anything is possible. This anthology series showcases the most stunning AI-generated environments and the stories that unfold within them.", thumbnailColor: Color.green, category: "Adventure", duration: "2h 10m", rating: "★ 4.6", year: "2024", ageRating: "PG-13", genres: ["Adventure", "Fantasy", "Anthology"], cast: ["Various"], director: "Collective AI Studios", videoUrl: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"),
                ContentItem(title: "Code & Canvas", description: "Art meets technology", synopsis: "A documentary exploring how AI is revolutionizing the art world. From music composition to visual masterpieces, witness the birth of a new creative renaissance.", thumbnailColor: Color.orange, category: "Documentary", duration: "1h 30m", rating: "★ 4.9", year: "2024", ageRating: "G", genres: ["Documentary", "Art"], cast: ["Various Artists", "Tech Innovators"], director: "Human Director"),
                ContentItem(title: "Quantum Tales", description: "Stories from the future", synopsis: "Set in 2045, this drama series follows interconnected lives in a world where quantum computing has solved humanity's greatest challenges, but created entirely new ones.", thumbnailColor: Color.purple, category: "Drama", duration: "1h 55m", rating: "★ 4.5", year: "2024", ageRating: "TV-MA", genres: ["Drama", "Sci-Fi", "Thriller"], cast: ["Alex Chen", "Dr. Quantum", "Zara Kim"], director: "AI Director Gamma"),
                ContentItem(title: "Synthetic Souls", description: "AI-generated emotions", synopsis: "A beautiful love story between a human and an AI entity. This film questions the nature of love, consciousness, and what it means to be truly alive in the digital age.", thumbnailColor: Color.pink, category: "Romance", duration: "2h 5m", rating: "★ 4.8", year: "2024", ageRating: "PG-13", genres: ["Romance", "Sci-Fi", "Drama"], cast: ["Emma Torres", "ADAM (AI)", "Dr. Love"], director: "AI Director Delta")
            ]
        ),
        ContentCategory(
            name: "AI Originals",
            items: [
                ContentItem(title: "Machine Learning", description: "Educational series", thumbnailColor: Color.cyan, category: "Education", duration: "45m", rating: "★ 4.4"),
                ContentItem(title: "Binary Dreams", description: "Short film collection", thumbnailColor: Color.orange, category: "Anthology", duration: "1h 20m", rating: "★ 4.7"),
                ContentItem(title: "Algorithm Stories", description: "Tech narratives", thumbnailColor: Color.mint, category: "Tech", duration: "1h 35m", rating: "★ 4.6"),
                ContentItem(title: "Data Streams", description: "Information age tales", thumbnailColor: Color.blue, category: "Thriller", duration: "2h 0m", rating: "★ 4.8"),
                ContentItem(title: "Virtual Visions", description: "Immersive experiences", thumbnailColor: Color.purple, category: "VR", duration: "1h 15m", rating: "★ 4.5")
            ]
        ),
        ContentCategory(
            name: "Recently Added",
            items: [
                ContentItem(title: "Future Fragments", description: "Experimental shorts", thumbnailColor: Color.red, category: "Experimental", duration: "30m", rating: "★ 4.3"),
                ContentItem(title: "AI Symphony", description: "Music and visuals", thumbnailColor: Color.green, category: "Music", duration: "1h 10m", rating: "★ 4.9"),
                ContentItem(title: "Pixel Poetry", description: "Visual storytelling", thumbnailColor: Color.yellow, category: "Art", duration: "50m", rating: "★ 4.7"),
                ContentItem(title: "Cyber Chronicles", description: "Future history", thumbnailColor: Color.purple, category: "History", duration: "1h 40m", rating: "★ 4.6"),
                ContentItem(title: "Tech Tunes", description: "Musical narratives", thumbnailColor: Color.pink, category: "Musical", duration: "1h 25m", rating: "★ 4.8")
            ]
        ),
        ContentCategory(
            name: "Top Rated",
            items: [
                ContentItem(title: "Masterpiece AI", description: "Award-winning content", thumbnailColor: Color.yellow, category: "Award", duration: "2h 30m", rating: "★ 4.9"),
                ContentItem(title: "Creative Algorithms", description: "Best of AI art", thumbnailColor: Color.cyan, category: "Art", duration: "1h 50m", rating: "★ 4.8"),
                ContentItem(title: "Innovation Stories", description: "Tech breakthroughs", thumbnailColor: Color.orange, category: "Documentary", duration: "1h 45m", rating: "★ 4.9"),
                ContentItem(title: "Digital Masterworks", description: "Premium content", thumbnailColor: Color.mint, category: "Premium", duration: "2h 15m", rating: "★ 4.8"),
                ContentItem(title: "AI Excellence", description: "Top-tier productions", thumbnailColor: Color.blue, category: "Premium", duration: "2h 0m", rating: "★ 4.9")
            ]
        )
    ]
    
    // Generate random stars for background
    let stars: [(x: Double, y: Double, size: Double)] = {
        return (0..<100).map { _ in
            (
                x: Double.random(in: 0...1),
                y: Double.random(in: 0...1),
                size: Double.random(in: 1...2.5)
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
                    .fill(starWhite.opacity(Double.random(in: 0.1...0.3)))
                    .frame(width: star.size, height: star.size)
                    .position(
                        x: star.x * UIScreen.main.bounds.width,
                        y: star.y * UIScreen.main.bounds.height
                    )
            }
            
            ScrollView {
                VStack(spacing: 24) {
                    // Top Navigation Bar
                    TopNavigationBarView(
                        onSearchClick: onSearchClick,
                        onProfileClick: onProfileClick
                    )
                    
                    // Featured Content Hero Section
                    FeaturedContentHeroView(
                        content: featuredContent,
                        onPlayClick: { onContentClick(featuredContent) }
                    )
                    
                    // Content Categories
                    ForEach(contentCategories) { category in
                        ContentCategorySectionView(
                            category: category,
                            onContentClick: onContentClick,
                            onShowDetails: { content in
                                selectedContent = content
                                showDetailsSheet = true
                            },
                            onPlayContent: onPlayContent,
                            onToggleLike: onToggleLike,
                            onToggleWatchlist: onToggleWatchlist
                        )
                    }
                    
                    // Bottom padding
                    Spacer()
                        .frame(height: 32)
                }
            }
        }
        .navigationBarHidden(true)
        .sheet(isPresented: $showDetailsSheet) {
            if let content = selectedContent {
                ContentDetailsView(
                    content: content,
                    onDismiss: { showDetailsSheet = false },
                    onPlay: { content in
                        onPlayContent(content)
                        showDetailsSheet = false
                    },
                    onToggleLike: onToggleLike,
                    onToggleWatchlist: onToggleWatchlist
                )
            }
        }
    }
}

struct TopNavigationBarView: View {
    let onSearchClick: () -> Void
    let onProfileClick: () -> Void
    
    var body: some View {
        HStack {
            Text("mAIScreen")
                .font(.system(size: 28, weight: .bold))
                .foregroundColor(.white)
            
            Spacer()
            
            HStack(spacing: 16) {
                Button(action: onSearchClick) {
                    Image(systemName: "magnifyingglass")
                        .font(.system(size: 20))
                        .foregroundColor(.white)
                }
                
                Button(action: onProfileClick) {
                    Image(systemName: "person.circle")
                        .font(.system(size: 20))
                        .foregroundColor(.white)
                }
            }
        }
        .padding(.horizontal, 24)
        .padding(.vertical, 16)
    }
}

struct FeaturedContentHeroView: View {
    let content: ContentItem
    let onPlayClick: () -> Void
    
    var body: some View {
        VStack {
            ZStack {
                // Featured content background
                RoundedRectangle(cornerRadius: 16)
                    .fill(content.thumbnailColor)
                    .frame(height: 400)
                
                // Gradient overlay for text readability
                RoundedRectangle(cornerRadius: 16)
                    .fill(
                        LinearGradient(
                            gradient: Gradient(colors: [
                                Color.clear,
                                Color.black.opacity(0.7)
                            ]),
                            startPoint: .top,
                            endPoint: .bottom
                        )
                    )
                    .frame(height: 400)
                
                // Content info
                VStack(alignment: .leading, spacing: 8) {
                    Spacer()
                    
                    Text(content.title)
                        .font(.system(size: 32, weight: .bold))
                        .foregroundColor(.white)
                        .multilineTextAlignment(.leading)
                    
                    Text(content.description)
                        .font(.system(size: 16))
                        .foregroundColor(.white.opacity(0.9))
                        .lineLimit(3)
                        .multilineTextAlignment(.leading)
                    
                    HStack(spacing: 16) {
                        Text(content.rating)
                            .font(.system(size: 14, weight: .medium))
                            .foregroundColor(.white.opacity(0.8))
                        
                        Text(content.duration)
                            .font(.system(size: 14))
                            .foregroundColor(.white.opacity(0.8))
                        
                        Text(content.category)
                            .font(.system(size: 14))
                            .foregroundColor(.white.opacity(0.8))
                    }
                    
                    Spacer()
                        .frame(height: 8)
                    
                    // Play button
                    Button(action: onPlayClick) {
                        HStack {
                            Image(systemName: "play.fill")
                                .font(.system(size: 16))
                            Text("Play")
                                .font(.system(size: 16, weight: .medium))
                        }
                        .foregroundColor(.black)
                        .padding(.horizontal, 24)
                        .padding(.vertical, 12)
                        .background(Color.white)
                        .cornerRadius(8)
                    }
                }
                .padding(24)
                .frame(maxWidth: .infinity, alignment: .leading)
            }
        }
        .padding(.horizontal, 24)
    }
}

struct ContentCategorySectionView: View {
    let category: ContentCategory
    let onContentClick: (ContentItem) -> Void
    let onShowDetails: (ContentItem) -> Void
    let onPlayContent: (ContentItem) -> Void
    let onToggleLike: (ContentItem) -> Void
    let onToggleWatchlist: (ContentItem) -> Void
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(category.name)
                .font(.system(size: 20, weight: .bold))
                .foregroundColor(.white)
                .padding(.horizontal, 24)
            
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 12) {
                    ForEach(category.items) { item in
                        ContentCardView(
                            content: item,
                            onClick: { onShowDetails(item) },
                            onPlay: { onPlayContent(item) },
                            onToggleLike: { onToggleLike(item) },
                            onToggleWatchlist: { onToggleWatchlist(item) }
                        )
                    }
                }
                .padding(.horizontal, 24)
            }
        }
    }
}

struct ContentCardView: View {
    let content: ContentItem
    let onClick: () -> Void
    let onPlay: () -> Void
    let onToggleLike: () -> Void
    let onToggleWatchlist: () -> Void
    
    @State private var showActionButtons = false
    
    var body: some View {
        ZStack {
            RoundedRectangle(cornerRadius: 8)
                .fill(content.thumbnailColor)
                .frame(width: 160, height: 240)
            
            // Gradient overlay
            RoundedRectangle(cornerRadius: 8)
                .fill(
                    LinearGradient(
                        gradient: Gradient(colors: [
                            Color.clear,
                            Color.black.opacity(showActionButtons ? 0.8 : 0.6)
                        ]),
                        startPoint: .top,
                        endPoint: .bottom
                    )
                )
                .frame(width: 160, height: 240)
            
            if showActionButtons {
                // Action buttons overlay
                VStack(spacing: 12) {
                    // Play button
                    Button(action: onPlay) {
                        Image(systemName: "play.fill")
                            .font(.system(size: 24))
                            .foregroundColor(.black)
                            .frame(width: 48, height: 48)
                            .background(Color.white)
                            .clipShape(Circle())
                    }
                    
                    HStack(spacing: 12) {
                        // Like button
                        Button(action: onToggleLike) {
                            Image(systemName: content.isLiked ? "heart.fill" : "heart")
                                .font(.system(size: 16))
                                .foregroundColor(.white)
                                .frame(width: 32, height: 32)
                                .background(Color.white.opacity(0.2))
                                .clipShape(Circle())
                        }
                        
                        // Watchlist button
                        Button(action: onToggleWatchlist) {
                            Image(systemName: content.isInWatchlist ? "checkmark" : "plus")
                                .font(.system(size: 16, weight: .bold))
                                .foregroundColor(.white)
                                .frame(width: 32, height: 32)
                                .background(Color.white.opacity(0.2))
                                .clipShape(Circle())
                        }
                        
                        // More info button
                        Button(action: onClick) {
                            Image(systemName: "info.circle")
                                .font(.system(size: 16))
                                .foregroundColor(.white)
                                .frame(width: 32, height: 32)
                                .background(Color.white.opacity(0.2))
                                .clipShape(Circle())
                        }
                    }
                }
                .transition(.opacity.combined(with: .scale))
            } else {
                // Content info (default state)
                VStack(alignment: .leading, spacing: 4) {
                    Spacer()
                    
                    Text(content.title)
                        .font(.system(size: 14, weight: .bold))
                        .foregroundColor(.white)
                        .lineLimit(2)
                        .multilineTextAlignment(.leading)
                        .frame(maxWidth: .infinity, alignment: .leading)
                    
                    Text(content.rating)
                        .font(.system(size: 12))
                        .foregroundColor(.white.opacity(0.8))
                        .frame(maxWidth: .infinity, alignment: .leading)
                    
                    Text(content.duration)
                        .font(.system(size: 12))
                        .foregroundColor(.white.opacity(0.7))
                        .frame(maxWidth: .infinity, alignment: .leading)
                }
                .padding(12)
                .frame(width: 160, height: 240, alignment: .bottomLeading)
                .transition(.opacity)
            }
        }
        .onTapGesture {
            withAnimation(.easeInOut(duration: 0.2)) {
                if showActionButtons {
                    onClick() // Show details
                } else {
                    showActionButtons = true
                    
                    // Auto-hide after 3 seconds
                    DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
                        withAnimation(.easeInOut(duration: 0.2)) {
                            showActionButtons = false
                        }
                    }
                }
            }
        }
    }
}

struct ContentDetailsView: View {
    let content: ContentItem
    let onDismiss: () -> Void
    let onPlay: (ContentItem) -> Void
    let onToggleLike: (ContentItem) -> Void
    let onToggleWatchlist: (ContentItem) -> Void
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    // Content thumbnail
                    ZStack {
                        RoundedRectangle(cornerRadius: 12)
                            .fill(content.thumbnailColor)
                            .frame(height: 200)
                        
                        // Gradient overlay
                        RoundedRectangle(cornerRadius: 12)
                            .fill(
                                LinearGradient(
                                    gradient: Gradient(colors: [
                                        Color.clear,
                                        Color.black.opacity(0.7)
                                    ]),
                                    startPoint: .top,
                                    endPoint: .bottom
                                )
                            )
                            .frame(height: 200)
                        
                        // Play button overlay
                        Button(action: { onPlay(content) }) {
                            Image(systemName: "play.fill")
                                .font(.system(size: 32))
                                .foregroundColor(.black)
                                .frame(width: 64, height: 64)
                                .background(Color.white.opacity(0.9))
                                .clipShape(Circle())
                        }
                    }
                    .padding(.horizontal)
                    
                    VStack(alignment: .leading, spacing: 16) {
                        // Title and year
                        Text("\(content.title) (\(content.year))")
                            .font(.system(size: 24, weight: .bold))
                            .foregroundColor(.white)
                        
                        // Rating, duration, age rating
                        HStack(spacing: 16) {
                            Text(content.rating)
                                .font(.system(size: 14, weight: .medium))
                                .foregroundColor(.white.opacity(0.8))
                            
                            Text(content.duration)
                                .font(.system(size: 14))
                                .foregroundColor(.white.opacity(0.8))
                            
                            Text(content.ageRating)
                                .font(.system(size: 12, weight: .bold))
                                .foregroundColor(.white)
                                .padding(.horizontal, 8)
                                .padding(.vertical, 4)
                                .background(Color.white.opacity(0.2))
                                .cornerRadius(4)
                        }
                        
                        // Action buttons
                        HStack(spacing: 12) {
                            // Play button
                            Button(action: { onPlay(content) }) {
                                HStack {
                                    Image(systemName: "play.fill")
                                        .font(.system(size: 16))
                                    Text("Play")
                                        .font(.system(size: 16, weight: .medium))
                                }
                                .foregroundColor(.black)
                                .padding(.horizontal, 24)
                                .padding(.vertical, 12)
                                .background(Color.white)
                                .cornerRadius(8)
                            }
                            
                            // Like button
                            Button(action: { onToggleLike(content) }) {
                                HStack {
                                    Image(systemName: content.isLiked ? "heart.fill" : "heart")
                                        .font(.system(size: 16))
                                    Text(content.isLiked ? "Liked" : "Like")
                                        .font(.system(size: 16, weight: .medium))
                                }
                                .foregroundColor(.white)
                                .padding(.horizontal, 20)
                                .padding(.vertical, 12)
                                .overlay(
                                    RoundedRectangle(cornerRadius: 8)
                                        .stroke(Color.white.opacity(0.5), lineWidth: 1)
                                )
                            }
                            
                            // Watchlist button
                            Button(action: { onToggleWatchlist(content) }) {
                                HStack {
                                    Image(systemName: content.isInWatchlist ? "checkmark" : "plus")
                                        .font(.system(size: 16, weight: .bold))
                                    Text(content.isInWatchlist ? "In List" : "My List")
                                        .font(.system(size: 16, weight: .medium))
                                }
                                .foregroundColor(.white)
                                .padding(.horizontal, 16)
                                .padding(.vertical, 12)
                                .overlay(
                                    RoundedRectangle(cornerRadius: 8)
                                        .stroke(Color.white.opacity(0.5), lineWidth: 1)
                                )
                            }
                        }
                        
                        // Synopsis
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Synopsis")
                                .font(.system(size: 18, weight: .bold))
                                .foregroundColor(.white)
                            
                            Text(content.synopsis)
                                .font(.system(size: 14))
                                .foregroundColor(.white.opacity(0.9))
                                .lineSpacing(4)
                        }
                        
                        // Genres
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Genres")
                                .font(.system(size: 16, weight: .bold))
                                .foregroundColor(.white)
                            
                            ScrollView(.horizontal, showsIndicators: false) {
                                HStack(spacing: 8) {
                                    ForEach(content.genres, id: \.self) { genre in
                                        Text(genre)
                                            .font(.system(size: 12))
                                            .foregroundColor(.white)
                                            .padding(.horizontal, 12)
                                            .padding(.vertical, 6)
                                            .background(Color.white.opacity(0.1))
                                            .cornerRadius(16)
                                    }
                                }
                                .padding(.horizontal)
                            }
                        }
                        
                        // Cast and crew
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Cast & Crew")
                                .font(.system(size: 16, weight: .bold))
                                .foregroundColor(.white)
                            
                            Text("Director: \(content.director)")
                                .font(.system(size: 14))
                                .foregroundColor(.white.opacity(0.8))
                            
                            Text("Cast: \(content.cast.joined(separator: ", "))")
                                .font(.system(size: 14))
                                .foregroundColor(.white.opacity(0.8))
                        }
                        
                        Spacer(minLength: 24)
                    }
                    .padding(.horizontal)
                }
            }
            .background(Color.black)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Done") {
                        onDismiss()
                    }
                    .foregroundColor(.white)
                }
            }
        }
    }
}

#Preview {
    MainView(
        onProfileClick: {
            print("Profile clicked")
        },
        onSearchClick: {
            print("Search clicked")
        },
        onContentClick: { content in
            print("Content clicked: \(content.title)")
        },
        onPlayContent: { content in
            print("Play content: \(content.title)")
        },
        onToggleLike: { content in
            print("Toggle like: \(content.title)")
        },
        onToggleWatchlist: { content in
            print("Toggle watchlist: \(content.title)")
        }
    )
}
