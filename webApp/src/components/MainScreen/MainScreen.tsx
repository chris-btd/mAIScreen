import React, { useState } from 'react';
import './MainScreen.css';

interface ContentItem {
  id: string;
  title: string;
  description: string;
  synopsis: string;
  thumbnailColor: string;
  category: string;
  duration: string;
  rating: string;
  year: string;
  ageRating: string;
  genres: string[];
  cast: string[];
  director: string;
  videoUrl?: string;
  isLiked: boolean;
  isInWatchlist: boolean;
}

interface ContentCategory {
  name: string;
  items: ContentItem[];
}

interface MainScreenProps {
  onProfileClick?: () => void;
  onSearchClick?: () => void;
  onContentClick?: (content: ContentItem) => void;
  onPlayContent?: (content: ContentItem) => void;
  onToggleLike?: (content: ContentItem) => void;
  onToggleWatchlist?: (content: ContentItem) => void;
}

export const MainScreen: React.FC<MainScreenProps> = ({
  onProfileClick = () => {},
  onSearchClick = () => {},
  onContentClick = () => {},
  onPlayContent = () => {},
  onToggleLike = () => {},
  onToggleWatchlist = () => {}
}) => {
  // Note: onContentClick is used via handleShowDetails function below
  const [showDetailsModal, setShowDetailsModal] = useState(false);
  const [selectedContent, setSelectedContent] = useState<ContentItem | null>(null);
  const [showVideoPlayer, setShowVideoPlayer] = useState(false);
  const [playingContent, setPlayingContent] = useState<ContentItem | null>(null);

  // Sample content data
  const featuredContent: ContentItem = {
    id: "featured1",
    title: "AI Dreams: The Future Unfolds",
    description: "An immersive journey through AI-generated worlds where reality and imagination blur into stunning visual narratives. Experience the cutting-edge of artificial creativity.",
    synopsis: "In a world where artificial intelligence has evolved beyond human comprehension, a young data scientist discovers a hidden realm within the neural networks. As she navigates through layers of digital consciousness, she uncovers a conspiracy that threatens both the virtual and real worlds. With stunning AI-generated visuals and a gripping narrative, this film explores the boundaries between human creativity and machine intelligence, questioning what it truly means to dream in an age of artificial minds.",
    thumbnailColor: "#6366F1",
    category: "Featured",
    duration: "2h 15m",
    rating: "★ 4.8",
    year: "2024",
    ageRating: "PG-13",
    genres: ["Sci-Fi", "Drama", "Thriller"],
    cast: ["Sarah Chen", "Marcus Rivera", "Dr. Elena Vasquez"],
    director: "AI Director Alpha",
    videoUrl: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
    isLiked: false,
    isInWatchlist: false
  };

  const contentCategories: ContentCategory[] = [
    {
      name: "Trending Now",
      items: [
        {
          id: "1",
          title: "Neural Networks",
          description: "AI explores consciousness",
          synopsis: "A groundbreaking series that delves into the mysteries of artificial consciousness. Follow Dr. Maya Singh as she develops the first truly sentient AI, only to discover that her creation has its own agenda.",
          thumbnailColor: "#EF4444",
          category: "Sci-Fi",
          duration: "1h 45m",
          rating: "★ 4.7",
          year: "2024",
          ageRating: "TV-14",
          genres: ["Sci-Fi", "Drama"],
          cast: ["Dr. Maya Singh", "ARIA (AI)", "Prof. Chen"],
          director: "AI Director Beta",
          videoUrl: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
          isLiked: false,
          isInWatchlist: false
        },
        {
          id: "2",
          title: "Digital Horizons",
          description: "Virtual reality adventures",
          synopsis: "Step into breathtaking virtual worlds where anything is possible. This anthology series showcases the most stunning AI-generated environments and the stories that unfold within them.",
          thumbnailColor: "#10B981",
          category: "Adventure",
          duration: "2h 10m",
          rating: "★ 4.6",
          year: "2024",
          ageRating: "PG-13",
          genres: ["Adventure", "Fantasy", "Anthology"],
          cast: ["Various"],
          director: "Collective AI Studios",
          videoUrl: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
          isLiked: false,
          isInWatchlist: false
        },
        {
          id: "3",
          title: "Code & Canvas",
          description: "Art meets technology",
          synopsis: "A documentary exploring how AI is revolutionizing the art world. From music composition to visual masterpieces, witness the birth of a new creative renaissance.",
          thumbnailColor: "#F59E0B",
          category: "Documentary",
          duration: "1h 30m",
          rating: "★ 4.9",
          year: "2024",
          ageRating: "G",
          genres: ["Documentary", "Art"],
          cast: ["Various Artists", "Tech Innovators"],
          director: "Human Director",
          isLiked: false,
          isInWatchlist: false
        },
        {
          id: "4",
          title: "Quantum Tales",
          description: "Stories from the future",
          synopsis: "Set in 2045, this drama series follows interconnected lives in a world where quantum computing has solved humanity's greatest challenges, but created entirely new ones.",
          thumbnailColor: "#8B5CF6",
          category: "Drama",
          duration: "1h 55m",
          rating: "★ 4.5",
          year: "2024",
          ageRating: "TV-MA",
          genres: ["Drama", "Sci-Fi", "Thriller"],
          cast: ["Alex Chen", "Dr. Quantum", "Zara Kim"],
          director: "AI Director Gamma",
          isLiked: false,
          isInWatchlist: false
        },
        {
          id: "5",
          title: "Synthetic Souls",
          description: "AI-generated emotions",
          synopsis: "A beautiful love story between a human and an AI entity. This film questions the nature of love, consciousness, and what it means to be truly alive in the digital age.",
          thumbnailColor: "#EC4899",
          category: "Romance",
          duration: "2h 5m",
          rating: "★ 4.8",
          year: "2024",
          ageRating: "PG-13",
          genres: ["Romance", "Sci-Fi", "Drama"],
          cast: ["Emma Torres", "ADAM (AI)", "Dr. Love"],
          director: "AI Director Delta",
          isLiked: false,
          isInWatchlist: false
        }
      ]
    },
    {
      name: "AI Originals",
      items: [
        {
          id: "6",
          title: "Machine Learning",
          description: "Educational series",
          synopsis: "Learn the fundamentals of AI and machine learning through engaging storytelling and stunning visualizations. Perfect for curious minds wanting to understand the technology shaping our future.",
          thumbnailColor: "#06B6D4",
          category: "Education",
          duration: "45m",
          rating: "★ 4.4",
          year: "2024",
          ageRating: "G",
          genres: ["Education", "Technology"],
          cast: ["Prof. DataBot", "Students"],
          director: "Educational AI",
          isLiked: false,
          isInWatchlist: false
        },
        {
          id: "7",
          title: "Binary Dreams",
          description: "Short film collection",
          synopsis: "A collection of thought-provoking short films exploring the intersection of humanity and technology. Each episode presents a unique perspective on our digital future.",
          thumbnailColor: "#F97316",
          category: "Anthology",
          duration: "1h 20m",
          rating: "★ 4.7",
          year: "2024",
          ageRating: "TV-14",
          genres: ["Anthology", "Drama", "Sci-Fi"],
          cast: ["Various"],
          director: "Indie AI Collective",
          isLiked: false,
          isInWatchlist: false
        },
        {
          id: "8",
          title: "Algorithm Stories",
          description: "Tech narratives",
          synopsis: "Behind-the-scenes stories of the algorithms that run our world. From social media feeds to autonomous vehicles, discover the code that shapes human behavior.",
          thumbnailColor: "#84CC16",
          category: "Tech",
          duration: "1h 35m",
          rating: "★ 4.6",
          year: "2024",
          ageRating: "TV-PG",
          genres: ["Technology", "Documentary"],
          cast: ["Tech Experts", "Engineers"],
          director: "Tech Documentary AI",
          isLiked: false,
          isInWatchlist: false
        },
        {
          id: "9",
          title: "Data Streams",
          description: "Information age tales",
          synopsis: "A cyber-thriller following a team of data analysts who uncover a massive conspiracy hidden in the world's information networks. Trust no one, question everything.",
          thumbnailColor: "#3B82F6",
          category: "Thriller",
          duration: "2h 0m",
          rating: "★ 4.8",
          year: "2024",
          ageRating: "R",
          genres: ["Thriller", "Cyber", "Mystery"],
          cast: ["Agent Data", "Hacker Zero", "CEO Corp"],
          director: "AI Thriller Master",
          isLiked: false,
          isInWatchlist: false
        },
        {
          id: "10",
          title: "Virtual Visions",
          description: "Immersive experiences",
          synopsis: "Experience the future of entertainment with these VR-optimized films. Designed specifically for virtual reality, these stories put you at the center of the action.",
          thumbnailColor: "#8B5CF6",
          category: "VR",
          duration: "1h 15m",
          rating: "★ 4.5",
          year: "2024",
          ageRating: "PG-13",
          genres: ["VR", "Interactive", "Adventure"],
          cast: ["You (Viewer)", "Guide AI"],
          director: "VR AI Director",
          isLiked: false,
          isInWatchlist: false
        }
      ]
    }
  ];

  const handleShowDetails = (content: ContentItem) => {
    setSelectedContent(content);
    setShowDetailsModal(true);
    onContentClick(content);
  };

  const handleCloseDetails = () => {
    setShowDetailsModal(false);
    setSelectedContent(null);
  };

  const handlePlayContent = (content: ContentItem) => {
    if (content.videoUrl) {
      setPlayingContent(content);
      setShowVideoPlayer(true);
      setShowDetailsModal(false);
      onPlayContent(content);
    }
  };

  const handleCloseVideoPlayer = () => {
    setShowVideoPlayer(false);
    setPlayingContent(null);
  };

  return (
    <div className="main-screen">
      <div className="starry-background">
        {Array.from({ length: 100 }, (_, i) => (
          <div
            key={i}
            className="star"
            style={{
              left: `${Math.random() * 100}%`,
              top: `${Math.random() * 100}%`,
              animationDelay: `${Math.random() * 3}s`
            }}
          />
        ))}
      </div>

      <div className="content">
        {/* Top Navigation */}
        <nav className="top-nav">
          <h1 className="logo">mAIScreen</h1>
          <div className="nav-actions">
            <button className="nav-button" onClick={onSearchClick}>
              🔍
            </button>
            <button className="nav-button" onClick={onProfileClick}>
              👤
            </button>
          </div>
        </nav>

        {/* Featured Content Hero */}
        <div className="hero-section">
          <div 
            className="hero-content"
            style={{ backgroundColor: featuredContent.thumbnailColor }}
          >
            <div className="hero-overlay">
              <div className="hero-info">
                <h2 className="hero-title">{featuredContent.title}</h2>
                <p className="hero-description">{featuredContent.description}</p>
                <div className="hero-meta">
                  <span>{featuredContent.rating}</span>
                  <span>{featuredContent.duration}</span>
                  <span>{featuredContent.category}</span>
                </div>
                <button
                  className="play-button"
                  onClick={() => handlePlayContent(featuredContent)}
                >
                  ▶️ Play
                </button>
              </div>
            </div>
          </div>
        </div>

        {/* Content Categories */}
        <div className="categories-section">
          {contentCategories.map((category) => (
            <div key={category.name} className="category">
              <h3 className="category-title">{category.name}</h3>
              <div className="content-row">
                {category.items.map((item) => (
                  <ContentCard
                    key={item.id}
                    content={item}
                    onShowDetails={() => handleShowDetails(item)}
                    onPlay={() => handlePlayContent(item)}
                    onToggleLike={() => onToggleLike(item)}
                    onToggleWatchlist={() => onToggleWatchlist(item)}
                  />
                ))}
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Details Modal */}
      {showDetailsModal && selectedContent && (
        <ContentDetailsModal
          content={selectedContent}
          onClose={handleCloseDetails}
          onPlay={(content) => {
            handlePlayContent(content);
          }}
          onToggleLike={onToggleLike}
          onToggleWatchlist={onToggleWatchlist}
        />
      )}

      {/* Video Player */}
      {showVideoPlayer && playingContent && (
        <VideoPlayer
          content={playingContent}
          onClose={handleCloseVideoPlayer}
        />
      )}
    </div>
  );
};

// Content Card Component with Netflix-style hover effects
interface ContentCardProps {
  content: ContentItem;
  onShowDetails: () => void;
  onPlay: () => void;
  onToggleLike: () => void;
  onToggleWatchlist: () => void;
}

const ContentCard: React.FC<ContentCardProps> = ({
  content,
  onShowDetails,
  onPlay,
  onToggleLike,
  onToggleWatchlist
}) => {
  const [isHovered, setIsHovered] = useState(false);

  return (
    <div 
      className={`content-card ${isHovered ? 'hovered' : ''}`}
      style={{ backgroundColor: content.thumbnailColor }}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
      onClick={onShowDetails}
    >
      <div className="card-overlay">
        {isHovered ? (
          <div className="hover-actions">
            <button className="action-button play-btn" onClick={(e) => {
              e.stopPropagation();
              onPlay();
            }}>
              ▶️
            </button>
            <div className="secondary-actions">
              <button className="action-button" onClick={(e) => {
                e.stopPropagation();
                onToggleLike();
              }}>
                {content.isLiked ? '❤️' : '🤍'}
              </button>
              <button className="action-button" onClick={(e) => {
                e.stopPropagation();
                onToggleWatchlist();
              }}>
                {content.isInWatchlist ? '✓' : '+'}
              </button>
              <button className="action-button" onClick={(e) => {
                e.stopPropagation();
                onShowDetails();
              }}>
                i
              </button>
            </div>
          </div>
        ) : (
          <div className="card-info">
            <h4 className="card-title">{content.title}</h4>
            <p className="card-rating">{content.rating}</p>
            <p className="card-duration">{content.duration}</p>
          </div>
        )}
      </div>
    </div>
  );
};

// Content Details Modal Component
interface ContentDetailsModalProps {
  content: ContentItem;
  onClose: () => void;
  onPlay: (content: ContentItem) => void;
  onToggleLike: (content: ContentItem) => void;
  onToggleWatchlist: (content: ContentItem) => void;
}

const ContentDetailsModal: React.FC<ContentDetailsModalProps> = ({
  content,
  onClose,
  onPlay,
  onToggleLike,
  onToggleWatchlist
}) => {
  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <button className="close-button" onClick={onClose}>
          ×
        </button>
        
        <div 
          className="modal-hero"
          style={{ backgroundColor: content.thumbnailColor }}
        >
          <div className="modal-hero-overlay">
            <button 
              className="modal-play-button"
              onClick={() => onPlay(content)}
            >
              ▶️
            </button>
          </div>
        </div>

        <div className="modal-details">
          <h2 className="modal-title">{content.title} ({content.year})</h2>
          
          <div className="modal-meta">
            <span className="rating">{content.rating}</span>
            <span className="duration">{content.duration}</span>
            <span className="age-rating">{content.ageRating}</span>
          </div>

          <div className="modal-actions">
            <button 
              className="modal-action-button primary"
              onClick={() => onPlay(content)}
            >
              ▶️ Play
            </button>
            <button 
              className="modal-action-button secondary"
              onClick={() => onToggleLike(content)}
            >
              {content.isLiked ? '❤️' : '🤍'} {content.isLiked ? 'Liked' : 'Like'}
            </button>
            <button 
              className="modal-action-button secondary"
              onClick={() => onToggleWatchlist(content)}
            >
              {content.isInWatchlist ? '✓' : '+'} {content.isInWatchlist ? 'In List' : 'My List'}
            </button>
          </div>

          <div className="modal-synopsis">
            <h3>Synopsis</h3>
            <p>{content.synopsis}</p>
          </div>

          <div className="modal-genres">
            <h3>Genres</h3>
            <div className="genre-tags">
              {content.genres.map((genre) => (
                <span key={genre} className="genre-tag">{genre}</span>
              ))}
            </div>
          </div>

          <div className="modal-cast">
            <h3>Cast & Crew</h3>
            <p><strong>Director:</strong> {content.director}</p>
            <p><strong>Cast:</strong> {content.cast.join(', ')}</p>
          </div>
        </div>
      </div>
    </div>
  );
};

// Video Player Component
interface VideoPlayerProps {
  content: ContentItem;
  onClose: () => void;
}

const VideoPlayer: React.FC<VideoPlayerProps> = ({ content, onClose }) => {
  const [isLoading, setIsLoading] = useState(true);
  const [hasError, setHasError] = useState(false);

  const handleVideoLoad = () => {
    setIsLoading(false);
  };

  const handleVideoError = () => {
    setIsLoading(false);
    setHasError(true);
  };

  return (
    <div className="video-player-overlay">
      <div className="video-player-container">
        {/* Close Button */}
        <button className="video-close-button" onClick={onClose}>
          ✕
        </button>

        {/* Loading State */}
        {isLoading && !hasError && (
          <div className="video-loading">
            <div className="loading-spinner"></div>
            <p>Loading {content.title}...</p>
          </div>
        )}

        {/* Error State */}
        {hasError && (
          <div className="video-error">
            <div className="error-icon">⚠️</div>
            <h3>Playback Error</h3>
            <p>Unable to load the video. Please try again.</p>
            <button onClick={onClose} className="error-button">Go Back</button>
          </div>
        )}

        {/* Video Player */}
        {content.videoUrl && (
          <video
            className="video-element"
            src={content.videoUrl}
            controls
            autoPlay
            onLoadedData={handleVideoLoad}
            onError={handleVideoError}
            onCanPlayThrough={handleVideoLoad}
          >
            Your browser does not support the video tag.
          </video>
        )}

        {/* Video Info Overlay */}
        <div className="video-info-overlay">
          <h2>{content.title}</h2>
          <p>{content.description}</p>
        </div>
      </div>
    </div>
  );
};

export default MainScreen;
