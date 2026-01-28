import SwiftUI

enum CategoryType {
    case standard
    case top10
    case featured
    case themed
}

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
    let imageUrl: String?
    let backdropUrl: String?

    init(title: String, description: String, synopsis: String = "", thumbnailColor: Color, category: String, duration: String, rating: String, year: String = "2024", ageRating: String = "PG-13", genres: [String] = [], cast: [String] = [], director: String = "", videoUrl: String? = nil, isLiked: Bool = false, isInWatchlist: Bool = false, imageUrl: String? = nil, backdropUrl: String? = nil) {
        self.title = title
        self.description = description
        self.synopsis = synopsis
        self.thumbnailColor = thumbnailColor
        self.category = category
        self.duration = duration
        self.rating = rating
        self.year = year
        self.ageRating = ageRating
        self.genres = genres
        self.cast = cast
        self.director = director
        self.videoUrl = videoUrl
        self.isLiked = isLiked
        self.isInWatchlist = isInWatchlist
        self.imageUrl = imageUrl
        self.backdropUrl = backdropUrl
    }
}

struct ContentCategory: Identifiable {
    let id = UUID()
    let name: String
    let type: CategoryType
    let items: [ContentItem]
    let themeColor: Color?
    let themeIcon: String?

    init(name: String, type: CategoryType = .standard, items: [ContentItem], themeColor: Color? = nil, themeIcon: String? = nil) {
        self.name = name
        self.type = type
        self.items = items
        self.themeColor = themeColor
        self.themeIcon = themeIcon
    }
}

struct MainView: View {
    let onProfileClick: () -> Void
    let onSearchClick: () -> Void
    let onSettingsClick: () -> Void
    let onUploadClick: () -> Void
    let onContentClick: (ContentItem) -> Void
    let onPlayContent: (ContentItem) -> Void
    let onToggleLike: (ContentItem) -> Void
    let onToggleWatchlist: (ContentItem) -> Void
    let recentlyWatched: [ContentItem]
    
    @State private var showDetailsSheet = false
    @State private var selectedContent: ContentItem?
    @State private var showDrawer = false
    
    // Sample content data
    let featuredContent = ContentItem(
        title: "AI Dreams: The Future Unfolds",
        description: "An immersive journey through AI-generated worlds where reality and imagination blur into stunning visual narratives.",
        synopsis: "In a world where artificial intelligence has evolved beyond human comprehension, a young data scientist discovers a hidden realm within the neural networks. As she navigates through layers of digital consciousness, she uncovers a conspiracy that threatens both the virtual and real worlds.",
        thumbnailColor: Color(red: 0.392, green: 0.4, blue: 0.945),
        category: "Featured",
        duration: "2h 15m",
        rating: "4.8",
        year: "2024",
        ageRating: "PG-13",
        genres: ["Sci-Fi", "Drama", "Thriller"],
        cast: ["Sarah Chen", "Marcus Rivera", "Dr. Elena Vasquez"],
        director: "AI Director Alpha",
        videoUrl: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
        imageUrl: "https://images.unsplash.com/photo-1620712943543-bcc4688e7485?w=800",
        backdropUrl: "https://images.unsplash.com/photo-1620712943543-bcc4688e7485?w=1920"
    )
    
    let contentCategories: [ContentCategory] = [
        ContentCategory(
            name: "Top 10 Trending",
            type: .top10,
            items: [
                ContentItem(title: "Neural Networks", description: "AI explores consciousness", synopsis: "A groundbreaking series that delves into the mysteries of artificial consciousness.", thumbnailColor: Color.red, category: "Sci-Fi", duration: "1h 45m", rating: "4.7", imageUrl: "https://images.unsplash.com/photo-1677442136019-21780ecad995?w=800"),
                ContentItem(title: "Digital Horizons", description: "Virtual reality adventures", thumbnailColor: Color.green, category: "Adventure", duration: "2h 10m", rating: "4.6", imageUrl: "https://images.unsplash.com/photo-1478760329108-5c3ed9d495a0?w=800"),
                ContentItem(title: "Code & Canvas", description: "Art meets technology", thumbnailColor: Color.orange, category: "Documentary", duration: "1h 30m", rating: "4.5", imageUrl: "https://images.unsplash.com/photo-1547826039-bfc35e0f1ea8?w=800"),
                ContentItem(title: "Quantum Leap", description: "Time travel reimagined", thumbnailColor: Color.purple, category: "Sci-Fi", duration: "2h 5m", rating: "4.9", imageUrl: "https://images.unsplash.com/photo-1635070041078-e363dbe005cb?w=800"),
                ContentItem(title: "Machine Dreams", description: "What do AIs dream about?", thumbnailColor: Color.pink, category: "Experimental", duration: "1h 20m", rating: "4.3", imageUrl: "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=800")
            ]
        ),
        ContentCategory(
            name: "AI Originals",
            type: .standard,
            items: [
                ContentItem(title: "The Algorithm", description: "Behind the code", thumbnailColor: Color.cyan, category: "Thriller", duration: "1h 55m", rating: "4.8", imageUrl: "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=800"),
                ContentItem(title: "Binary Dreams", description: "Short film collection", thumbnailColor: Color.orange, category: "Anthology", duration: "1h 20m", rating: "4.7", imageUrl: "https://images.unsplash.com/photo-1614624532983-4ce03382d63d?w=800"),
                ContentItem(title: "Algorithm Stories", description: "Tech narratives", thumbnailColor: Color.mint, category: "Tech", duration: "1h 35m", rating: "4.6", imageUrl: "https://images.unsplash.com/photo-1534796636912-3b95b3ab5986?w=800"),
                ContentItem(title: "Data Streams", description: "Information age tales", thumbnailColor: Color.blue, category: "Thriller", duration: "2h 0m", rating: "4.8", imageUrl: "https://images.unsplash.com/photo-1509248961895-e95c500d082e?w=800"),
                ContentItem(title: "Virtual Visions", description: "Immersive experiences", thumbnailColor: Color.purple, category: "VR", duration: "1h 15m", rating: "4.5", imageUrl: "https://images.unsplash.com/photo-1516589178581-6cd7833ae3b2?w=800")
            ]
        ),
        ContentCategory(
            name: "Popular This Week",
            type: .standard,
            items: [
                ContentItem(title: "Synthetic Love", description: "Romance in the digital age", thumbnailColor: Color.pink, category: "Romance", duration: "1h 40m", rating: "4.4", imageUrl: "https://images.unsplash.com/photo-1516589178581-6cd7833ae3b2?w=800"),
                ContentItem(title: "AI Symphony", description: "Music and visuals", thumbnailColor: Color.green, category: "Music", duration: "1h 10m", rating: "4.9", imageUrl: "https://images.unsplash.com/photo-1547826039-bfc35e0f1ea8?w=800"),
                ContentItem(title: "Pixel Poetry", description: "Visual storytelling", thumbnailColor: Color.yellow, category: "Art", duration: "50m", rating: "4.7", imageUrl: "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=800"),
                ContentItem(title: "Cyber Chronicles", description: "Future history", thumbnailColor: Color.purple, category: "History", duration: "1h 40m", rating: "4.6", imageUrl: "https://images.unsplash.com/photo-1614624532983-4ce03382d63d?w=800"),
                ContentItem(title: "Tech Tunes", description: "Musical narratives", thumbnailColor: Color.cyan, category: "Musical", duration: "1h 25m", rating: "4.8", imageUrl: "https://images.unsplash.com/photo-1534796636912-3b95b3ab5986?w=800")
            ]
        ),
        ContentCategory(
            name: "Comic Books",
            type: .standard,
            items: [
                ContentItem(title: "Cyber Samurai", description: "A warrior reborn", thumbnailColor: Color.red, category: "Action", duration: "12 Issues", rating: "4.9", imageUrl: "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=800"),
                ContentItem(title: "The Last Algorithm", description: "Code becomes destiny", thumbnailColor: Color.purple, category: "Mystery", duration: "8 Issues", rating: "4.7", imageUrl: "https://images.unsplash.com/photo-1614624532983-4ce03382d63d?w=800"),
                ContentItem(title: "Neon Knights", description: "Heroes of the digital realm", thumbnailColor: Color.cyan, category: "Superhero", duration: "24 Issues", rating: "4.8", imageUrl: "https://images.unsplash.com/photo-1534796636912-3b95b3ab5986?w=800"),
                ContentItem(title: "Ghost in the Machine", description: "A haunting digital tale", thumbnailColor: Color.gray, category: "Horror", duration: "6 Issues", rating: "4.6", imageUrl: "https://images.unsplash.com/photo-1509248961895-e95c500d082e?w=800")
            ]
        )
    ]
    
    var body: some View {
        ZStack {
            // Pure black background
            Color.black
                .ignoresSafeArea()

            ScrollView {
                VStack(spacing: 24) {
                    // Top Navigation Bar
                    TopNavigationBarView(
                        onSearchClick: onSearchClick,
                        onProfileClick: onProfileClick,
                        onMenuClick: { showDrawer = true }
                    )
                    
                    // Featured Content Hero Section
                    FeaturedContentHeroView(
                        content: featuredContent,
                        onPlayClick: { onContentClick(featuredContent) }
                    )
                    
                    // Recently Watched Section (only show if there are items)
                    if !recentlyWatched.isEmpty {
                        RecentlyWatchedSectionView(
                            recentlyWatched: recentlyWatched,
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
        .overlay(
            // Sliding Drawer
            Group {
                if showDrawer {
                    SlidingDrawerView(
                        isShowing: $showDrawer,
                        onSettingsClick: {
                            showDrawer = false
                            onSettingsClick()
                        },
                        onUploadClick: {
                            showDrawer = false
                            onUploadClick()
                        }
                    )
                }
            }
        )
    }
}

struct TopNavigationBarView: View {
    let onSearchClick: () -> Void
    let onProfileClick: () -> Void
    let onMenuClick: () -> Void
    
    var body: some View {
        HStack {
            // Left side: Profile image + Title
            HStack(spacing: 12) {
                // Circular profile image button
                Button(action: onMenuClick) {
                    ZStack {
                        LinearGradient(
                            gradient: Gradient(colors: [
                                Color(red: 0.4, green: 0.495, blue: 0.918),
                                Color(red: 0.463, green: 0.294, blue: 0.635)
                            ]),
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                        .frame(width: 40, height: 40)
                        .clipShape(Circle())
                        
                        Text("👤")
                            .font(.system(size: 20))
                    }
                }
                
                Text("mAIScreen")
                    .font(.system(size: 28, weight: .bold))
                    .foregroundColor(.white)
            }
            
            Spacer()
            
            // Right side: Search and Profile icons
            HStack(spacing: 16) {
                Button(action: onSearchClick) {
                    Image(systemName: "magnifyingglass")
                        .font(.system(size: 20))
                        .foregroundColor(.white)
                }
                
                Button(action: onProfileClick) {
                    Text("📊")
                        .font(.system(size: 20))
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
        ZStack(alignment: .bottomLeading) {
            // Image background
            if let backdropUrl = content.backdropUrl, let url = URL(string: backdropUrl) {
                AsyncImage(url: url) { phase in
                    switch phase {
                    case .success(let image):
                        image
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                    case .failure(_):
                        Rectangle().fill(content.thumbnailColor)
                    case .empty:
                        Rectangle().fill(content.thumbnailColor)
                    @unknown default:
                        Rectangle().fill(content.thumbnailColor)
                    }
                }
                .frame(height: 500)
                .clipped()
            } else {
                Rectangle()
                    .fill(content.thumbnailColor)
                    .frame(height: 500)
            }

            // Bottom gradient overlay
            VStack {
                Spacer()
                LinearGradient(
                    gradient: Gradient(colors: [
                        Color.clear,
                        Color.black.opacity(0.5),
                        Color.black
                    ]),
                    startPoint: .top,
                    endPoint: .bottom
                )
                .frame(height: 350)
            }

            // Left gradient overlay
            HStack {
                LinearGradient(
                    gradient: Gradient(colors: [
                        Color.black.opacity(0.8),
                        Color.clear
                    ]),
                    startPoint: .leading,
                    endPoint: .trailing
                )
                .frame(width: UIScreen.main.bounds.width * 0.6)
                Spacer()
            }

            // Content info
            VStack(alignment: .leading, spacing: 12) {
                Text(content.title)
                    .font(.system(size: 40, weight: .bold))
                    .foregroundColor(.white)
                    .multilineTextAlignment(.leading)
                    .lineLimit(2)
                    .shadow(color: .black.opacity(0.5), radius: 4, x: 2, y: 2)

                Text(content.description)
                    .font(.system(size: 16))
                    .foregroundColor(.white.opacity(0.9))
                    .lineLimit(3)
                    .multilineTextAlignment(.leading)

                HStack(spacing: 16) {
                    // Rating badge
                    Text(content.rating)
                        .font(.system(size: 14, weight: .bold))
                        .foregroundColor(.white)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 4)
                        .background(Color(red: 0.275, green: 0.827, blue: 0.412))
                        .cornerRadius(4)

                    Text(content.duration)
                        .font(.system(size: 14))
                        .foregroundColor(.white.opacity(0.8))

                    Text(content.year)
                        .font(.system(size: 14))
                        .foregroundColor(.white.opacity(0.8))

                    // Age rating badge
                    Text(content.ageRating)
                        .font(.system(size: 12, weight: .bold))
                        .foregroundColor(.white)
                        .padding(.horizontal, 6)
                        .padding(.vertical, 2)
                        .background(Color.white.opacity(0.2))
                        .cornerRadius(4)
                }

                Spacer()
                    .frame(height: 8)

                // Action buttons
                HStack(spacing: 12) {
                    // Play button
                    Button(action: onPlayClick) {
                        HStack {
                            Image(systemName: "play.fill")
                                .font(.system(size: 16))
                            Text("Play")
                                .font(.system(size: 16, weight: .bold))
                        }
                        .foregroundColor(.black)
                        .padding(.horizontal, 28)
                        .padding(.vertical, 14)
                        .background(Color.white)
                        .cornerRadius(4)
                    }

                    // More Info button
                    Button(action: {}) {
                        HStack {
                            Image(systemName: "info.circle")
                                .font(.system(size: 16))
                            Text("More Info")
                                .font(.system(size: 16, weight: .medium))
                        }
                        .foregroundColor(.white)
                        .padding(.horizontal, 20)
                        .padding(.vertical, 14)
                        .background(Color.white.opacity(0.3))
                        .cornerRadius(4)
                    }
                }
            }
            .padding(.horizontal, 48)
            .padding(.bottom, 48)
            .frame(maxWidth: UIScreen.main.bounds.width * 0.7, alignment: .leading)
        }
        .frame(height: 500)
    }
}

struct RecentlyWatchedSectionView: View {
    let recentlyWatched: [ContentItem]
    let onContentClick: (ContentItem) -> Void
    let onShowDetails: (ContentItem) -> Void
    let onPlayContent: (ContentItem) -> Void
    let onToggleLike: (ContentItem) -> Void
    let onToggleWatchlist: (ContentItem) -> Void
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text("🕐")
                    .font(.system(size: 20))
                Text("Recently Watched")
                    .font(.system(size: 20, weight: .bold))
                    .foregroundColor(.white)
                
                Spacer()
                
                Text("\(recentlyWatched.count) \(recentlyWatched.count == 1 ? "video" : "videos")")
                    .font(.system(size: 14))
                    .foregroundColor(.white.opacity(0.7))
            }
            .padding(.horizontal, 24)
            
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 12) {
                    ForEach(recentlyWatched) { item in
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

struct ContentCategorySectionView: View {
    let category: ContentCategory
    let onContentClick: (ContentItem) -> Void
    let onShowDetails: (ContentItem) -> Void
    let onPlayContent: (ContentItem) -> Void
    let onToggleLike: (ContentItem) -> Void
    let onToggleWatchlist: (ContentItem) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            // Category header
            HStack(spacing: 8) {
                if let icon = category.themeIcon {
                    Text(icon)
                        .font(.system(size: 24))
                }
                Text(category.name)
                    .font(.system(size: 22, weight: .bold))
                    .foregroundColor(category.themeColor ?? .white)

                if category.themeColor != nil {
                    Rectangle()
                        .fill(category.themeColor!.opacity(0.5))
                        .frame(height: 2)
                }
            }
            .padding(.horizontal, 24)

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: category.type == .top10 ? 48 : 16) {
                    ForEach(Array(category.items.enumerated()), id: \.element.id) { index, item in
                        if category.type == .top10 {
                            Top10CardView(
                                content: item,
                                rank: index + 1,
                                onClick: { onShowDetails(item) },
                                onPlay: { onPlayContent(item) }
                            )
                        } else {
                            ContentCardView(
                                content: item,
                                onClick: { onShowDetails(item) },
                                onPlay: { onPlayContent(item) },
                                onToggleLike: { onToggleLike(item) },
                                onToggleWatchlist: { onToggleWatchlist(item) }
                            )
                        }
                    }
                }
                .padding(.horizontal, 24)
            }
        }
    }
}

struct Top10CardView: View {
    let content: ContentItem
    let rank: Int
    let onClick: () -> Void
    let onPlay: () -> Void

    @State private var isPressed = false

    var body: some View {
        HStack(alignment: .bottom, spacing: -25) {
            // Large rank number with outline effect
            ZStack {
                // Black fill
                Text("\(rank)")
                    .font(.system(size: 120, weight: .black))
                    .foregroundColor(.black)

                // White outline (using stroke)
                Text("\(rank)")
                    .font(.system(size: 120, weight: .black))
                    .foregroundColor(.clear)
                    .overlay(
                        Text("\(rank)")
                            .font(.system(size: 120, weight: .black))
                            .foregroundStyle(
                                .linearGradient(
                                    colors: [.gray, .white],
                                    startPoint: .top,
                                    endPoint: .bottom
                                )
                            )
                            .mask(
                                Text("\(rank)")
                                    .font(.system(size: 120, weight: .black))
                            )
                    )
            }
            .offset(y: 20)

            // Content card - 2:3 aspect ratio (poster style)
            ZStack {
                if let imageUrl = content.imageUrl, let url = URL(string: imageUrl) {
                    AsyncImage(url: url) { phase in
                        switch phase {
                        case .success(let image):
                            image
                                .resizable()
                                .aspectRatio(contentMode: .fill)
                        case .failure(_):
                            Rectangle().fill(content.thumbnailColor)
                        case .empty:
                            Rectangle().fill(content.thumbnailColor)
                        @unknown default:
                            Rectangle().fill(content.thumbnailColor)
                        }
                    }
                } else {
                    Rectangle().fill(content.thumbnailColor)
                }

                // Gradient overlay
                LinearGradient(
                    gradient: Gradient(colors: [
                        Color.clear,
                        Color.clear,
                        Color.black.opacity(0.8)
                    ]),
                    startPoint: .top,
                    endPoint: .bottom
                )

                // Title at bottom
                VStack {
                    Spacer()
                    Text(content.title)
                        .font(.system(size: 12, weight: .semibold))
                        .foregroundColor(.white)
                        .multilineTextAlignment(.center)
                        .lineLimit(2)
                        .padding(8)
                }
            }
            .frame(width: 130, height: 195) // 2:3 aspect ratio
            .cornerRadius(8)
            .scaleEffect(isPressed ? 1.05 : 1.0)
            .animation(.easeInOut(duration: 0.2), value: isPressed)
            .onTapGesture {
                onClick()
            }
            .onLongPressGesture(minimumDuration: 0.1, pressing: { pressing in
                isPressed = pressing
            }) {
                onPlay()
            }
        }
        .frame(height: 200)
    }
}

struct ContentCardView: View {
    let content: ContentItem
    let onClick: () -> Void
    let onPlay: () -> Void
    let onToggleLike: () -> Void
    let onToggleWatchlist: () -> Void

    @State private var showActionButtons = false
    @State private var isPressed = false

    var body: some View {
        ZStack {
            // Image background
            if let imageUrl = content.imageUrl, let url = URL(string: imageUrl) {
                AsyncImage(url: url) { phase in
                    switch phase {
                    case .success(let image):
                        image
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                    case .failure(_):
                        Rectangle().fill(content.thumbnailColor)
                    case .empty:
                        Rectangle().fill(content.thumbnailColor)
                    @unknown default:
                        Rectangle().fill(content.thumbnailColor)
                    }
                }
            } else {
                Rectangle().fill(content.thumbnailColor)
            }

            // Gradient overlay
            LinearGradient(
                gradient: Gradient(colors: [
                    Color.clear,
                    Color.clear,
                    Color.black.opacity(showActionButtons ? 0.9 : 0.7)
                ]),
                startPoint: .top,
                endPoint: .bottom
            )

            if showActionButtons {
                // Action buttons overlay
                Color.black.opacity(0.6)

                VStack(spacing: 8) {
                    // Play button
                    Button(action: onPlay) {
                        Image(systemName: "play.fill")
                            .font(.system(size: 18))
                            .foregroundColor(.black)
                            .frame(width: 44, height: 44)
                            .background(Color.white)
                            .clipShape(Circle())
                    }

                    HStack(spacing: 8) {
                        // Like button
                        Button(action: onToggleLike) {
                            Image(systemName: content.isLiked ? "heart.fill" : "heart")
                                .font(.system(size: 12))
                                .foregroundColor(.white)
                                .frame(width: 28, height: 28)
                                .background(Color.white.opacity(0.2))
                                .overlay(
                                    Circle().stroke(Color.white.opacity(0.5), lineWidth: 1)
                                )
                                .clipShape(Circle())
                        }

                        // Watchlist button
                        Button(action: onToggleWatchlist) {
                            Image(systemName: content.isInWatchlist ? "checkmark" : "plus")
                                .font(.system(size: 12, weight: .bold))
                                .foregroundColor(.white)
                                .frame(width: 28, height: 28)
                                .background(Color.white.opacity(0.2))
                                .overlay(
                                    Circle().stroke(Color.white.opacity(0.5), lineWidth: 1)
                                )
                                .clipShape(Circle())
                        }

                        // More info button
                        Button(action: onClick) {
                            Image(systemName: "info.circle")
                                .font(.system(size: 12))
                                .foregroundColor(.white)
                                .frame(width: 28, height: 28)
                                .background(Color.white.opacity(0.2))
                                .overlay(
                                    Circle().stroke(Color.white.opacity(0.5), lineWidth: 1)
                                )
                                .clipShape(Circle())
                        }
                    }
                }
                .transition(.opacity.combined(with: .scale))
            }

            // Title overlay at bottom (visible on tap)
            if showActionButtons {
                VStack {
                    Spacer()
                    LinearGradient(
                        gradient: Gradient(colors: [Color.clear, Color.black]),
                        startPoint: .top,
                        endPoint: .bottom
                    )
                    .frame(height: 40)
                    .overlay(
                        Text(content.title)
                            .font(.system(size: 14, weight: .semibold))
                            .foregroundColor(.white)
                            .lineLimit(1)
                            .padding(.horizontal, 12)
                            .padding(.bottom, 8),
                        alignment: .bottom
                    )
                }
            }
        }
        .frame(width: 200, height: 112.5)  // 16:9 aspect ratio
        .cornerRadius(8)
        .scaleEffect(isPressed ? 1.05 : 1.0)
        .shadow(color: .black.opacity(isPressed ? 0.5 : 0.2), radius: isPressed ? 12 : 4)
        .animation(.easeInOut(duration: 0.2), value: isPressed)
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
        .onLongPressGesture(minimumDuration: 0.1, pressing: { pressing in
            isPressed = pressing
        }) {
            onPlay()
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

struct SlidingDrawerView: View {
    @Binding var isShowing: Bool
    let onSettingsClick: () -> Void
    let onUploadClick: () -> Void
    
    var body: some View {
        ZStack(alignment: .leading) {
            // Dark overlay background
            Color.black.opacity(0.5)
                .ignoresSafeArea()
                .onTapGesture {
                    withAnimation {
                        isShowing = false
                    }
                }
            
            // Drawer panel
            HStack(spacing: 0) {
                VStack(alignment: .leading, spacing: 0) {
                    // Header with profile
                    VStack(spacing: 12) {
                        // Profile image
                        ZStack {
                            Circle()
                                .fill(Color.white.opacity(0.2))
                                .frame(width: 80, height: 80)
                            
                            Text("👤")
                                .font(.system(size: 40))
                        }
                        
                        Text("My Account")
                            .font(.system(size: 20, weight: .bold))
                            .foregroundColor(.white)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 32)
                    .padding(.horizontal, 24)
                    .background(
                        LinearGradient(
                            gradient: Gradient(colors: [
                                Color(red: 0.4, green: 0.495, blue: 0.918),
                                Color(red: 0.463, green: 0.294, blue: 0.635)
                            ]),
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
                    
                    Spacer()
                        .frame(height: 24)
                    
                    // Menu items
                    VStack(alignment: .leading, spacing: 0) {
                        // Upload option
                        Button(action: onUploadClick) {
                            HStack(spacing: 16) {
                                Text("📤")
                                    .font(.system(size: 24))
                                
                                Text("Upload")
                                    .font(.system(size: 16, weight: .medium))
                                    .foregroundColor(.white)
                                
                                Spacer()
                            }
                            .padding(.horizontal, 24)
                            .padding(.vertical, 16)
                        }
                        
                        Divider()
                            .background(Color.white.opacity(0.1))
                            .padding(.horizontal, 24)
                        
                        // Settings option
                        Button(action: onSettingsClick) {
                            HStack(spacing: 16) {
                                Text("⚙️")
                                    .font(.system(size: 24))
                                
                                Text("Settings")
                                    .font(.system(size: 16, weight: .medium))
                                    .foregroundColor(.white)
                                
                                Spacer()
                            }
                            .padding(.horizontal, 24)
                            .padding(.vertical, 16)
                        }
                        
                        Divider()
                            .background(Color.white.opacity(0.1))
                            .padding(.horizontal, 24)
                    }
                    
                    Spacer()
                    
                    // Footer
                    Text("mAIScreen v1.0.0")
                        .font(.system(size: 12))
                        .foregroundColor(.white.opacity(0.5))
                        .frame(maxWidth: .infinity)
                        .padding(.bottom, 24)
                }
                .frame(width: 280)
                .frame(maxHeight: .infinity)
                .background(Color(red: 0.106, green: 0.161, blue: 0.318))
                
                Spacer()
            }
        }
        .transition(.move(edge: .leading))
    }
}
