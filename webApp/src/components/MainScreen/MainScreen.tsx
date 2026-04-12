import React, { useState } from 'react';
import './MainScreen.css';
import { ContentItem, ContentCategory, CategoryType } from '../../types/ContentConfig';
import { useContentConfig } from '../../hooks/useContentConfig';

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
  const [showDetailsModal, setShowDetailsModal] = useState(false);
  const [selectedContent, setSelectedContent] = useState<ContentItem | null>(null);
  const [showVideoPlayer, setShowVideoPlayer] = useState(false);
  const [playingContent, setPlayingContent] = useState<ContentItem | null>(null);

  // Test data toggle - can be stored in localStorage
  const [useTestData, setUseTestData] = useState(() => {
    const stored = localStorage.getItem('useTestData');
    return stored === 'true';
  });

  // Use config-driven content
  const { categories, heroContent, isLoading } = useContentConfig({ useTestData });

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

  const toggleTestData = () => {
    const newValue = !useTestData;
    setUseTestData(newValue);
    localStorage.setItem('useTestData', String(newValue));
  };

  if (isLoading) {
    return (
      <div className="main-screen loading">
        <div className="loading-spinner"></div>
        <p>Loading content...</p>
      </div>
    );
  }

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
            <button
              className={`nav-button test-toggle ${useTestData ? 'active' : ''}`}
              onClick={toggleTestData}
              title={useTestData ? 'Test Data: ON' : 'Test Data: OFF'}
            >
              {useTestData ? '...' : '...'}
            </button>
            <button className="nav-button" onClick={onSearchClick}>
              ...
            </button>
            <button className="nav-button" onClick={onProfileClick}>
              ...
            </button>
          </div>
        </nav>

        {/* Featured Content Hero */}
        {heroContent && (
          <div className="hero-section">
            <div
              className="hero-content"
              style={{ backgroundColor: heroContent.thumbnailColor }}
            >
              <div className="hero-overlay">
                <div className="hero-info">
                  <h2 className="hero-title">{heroContent.title}</h2>
                  <p className="hero-description">{heroContent.description}</p>
                  <div className="hero-meta">
                    <span>{heroContent.rating}</span>
                    <span>{heroContent.duration}</span>
                    <span>{heroContent.category}</span>
                  </div>
                  <button
                    className="play-button"
                    onClick={() => handlePlayContent(heroContent)}
                  >
                    ... Play
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Content Categories */}
        <div className="categories-section">
          {categories.map((category) => (
            <CategorySection
              key={category.id}
              category={category}
              onShowDetails={handleShowDetails}
              onPlay={handlePlayContent}
              onToggleLike={onToggleLike}
              onToggleWatchlist={onToggleWatchlist}
            />
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

// Category Section Component - handles different category types
interface CategorySectionProps {
  category: ContentCategory;
  onShowDetails: (content: ContentItem) => void;
  onPlay: (content: ContentItem) => void;
  onToggleLike: (content: ContentItem) => void;
  onToggleWatchlist: (content: ContentItem) => void;
}

const CategorySection: React.FC<CategorySectionProps> = ({
  category,
  onShowDetails,
  onPlay,
  onToggleLike,
  onToggleWatchlist
}) => {
  return (
    <div className={`category ${category.type}`}>
      {/* Category Header */}
      {category.type === 'themed' ? (
        <ThemedCategoryHeader
          name={category.name}
          themeIcon={category.themeIcon}
          themeColor={category.themeColor}
        />
      ) : (
        <h3 className="category-title">{category.name}</h3>
      )}

      {/* Content Row */}
      <div className={`content-row ${category.type === 'top10' ? 'top10-row' : ''}`}>
        {category.items.map((item, index) => (
          category.type === 'top10' ? (
            <Top10Card
              key={item.id}
              content={item}
              rank={index + 1}
              onShowDetails={() => onShowDetails(item)}
              onPlay={() => onPlay(item)}
              onToggleLike={() => onToggleLike(item)}
              onToggleWatchlist={() => onToggleWatchlist(item)}
            />
          ) : (
            <ContentCard
              key={item.id}
              content={item}
              onShowDetails={() => onShowDetails(item)}
              onPlay={() => onPlay(item)}
              onToggleLike={() => onToggleLike(item)}
              onToggleWatchlist={() => onToggleWatchlist(item)}
            />
          )
        ))}
      </div>
    </div>
  );
};

// Themed Category Header Component
interface ThemedCategoryHeaderProps {
  name: string;
  themeIcon?: string;
  themeColor?: string;
}

const ThemedCategoryHeader: React.FC<ThemedCategoryHeaderProps> = ({
  name,
  themeIcon,
  themeColor
}) => {
  return (
    <div className="themed-category-header">
      {themeIcon && <span className="theme-icon">{themeIcon}</span>}
      <h3 className="category-title" style={{ color: themeColor }}>{name}</h3>
      {themeColor && <div className="theme-indicator" style={{ backgroundColor: themeColor }} />}
    </div>
  );
};

// Top 10 Card Component with rank badge
interface Top10CardProps {
  content: ContentItem;
  rank: number;
  onShowDetails: () => void;
  onPlay: () => void;
  onToggleLike: () => void;
  onToggleWatchlist: () => void;
}

const Top10Card: React.FC<Top10CardProps> = ({
  content,
  rank,
  onShowDetails,
  onPlay,
  onToggleLike,
  onToggleWatchlist
}) => {
  const [isHovered, setIsHovered] = useState(false);

  return (
    <div className="top10-card-container">
      <div className="rank-number">{rank}</div>
      <div
        className={`content-card top10 ${isHovered ? 'hovered' : ''}`}
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
                ...
              </button>
              <div className="secondary-actions">
                <button className="action-button" onClick={(e) => {
                  e.stopPropagation();
                  onToggleLike();
                }}>
                  {content.isLiked ? '...' : '...'}
                </button>
                <button className="action-button" onClick={(e) => {
                  e.stopPropagation();
                  onToggleWatchlist();
                }}>
                  {content.isInWatchlist ? '...' : '+'}
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
              ...
            </button>
            <div className="secondary-actions">
              <button className="action-button" onClick={(e) => {
                e.stopPropagation();
                onToggleLike();
              }}>
                {content.isLiked ? '...' : '...'}
              </button>
              <button className="action-button" onClick={(e) => {
                e.stopPropagation();
                onToggleWatchlist();
              }}>
                {content.isInWatchlist ? '...' : '+'}
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
          x
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
              ...
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
              ... Play
            </button>
            <button
              className="modal-action-button secondary"
              onClick={() => onToggleLike(content)}
            >
              {content.isLiked ? '...' : '...'} {content.isLiked ? 'Liked' : 'Like'}
            </button>
            <button
              className="modal-action-button secondary"
              onClick={() => onToggleWatchlist(content)}
            >
              {content.isInWatchlist ? '...' : '+'} {content.isInWatchlist ? 'In List' : 'My List'}
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
          x
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
            <div className="error-icon">...</div>
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
