/**
 * Enum for special category display types
 */
export type CategoryType = 'standard' | 'top10' | 'featured' | 'themed';

/**
 * Configuration for a single category
 */
export interface CategoryConfig {
  id: string;
  name: string;
  type: CategoryType;
  order: number;
  enabled: boolean;
  isTestOnly: boolean;
  contentIds: string[];
  themeColor?: string;
  themeIcon?: string;
  maxItems?: number;
}

/**
 * Configuration for hero/featured card
 */
export interface HeroConfig {
  contentId: string;
  fallbackContentId?: string;
}

/**
 * Root configuration object
 */
export interface ContentConfiguration {
  version: string;
  hero: HeroConfig;
  categories: CategoryConfig[];
  testCategories: CategoryConfig[];
}

/**
 * Content item interface
 */
export interface ContentItem {
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

/**
 * Content category with type info
 */
export interface ContentCategory {
  id: string;
  name: string;
  type: CategoryType;
  items: ContentItem[];
  themeColor?: string;
  themeIcon?: string;
}
