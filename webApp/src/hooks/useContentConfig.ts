import { useState, useEffect, useCallback } from 'react';
import { ContentConfiguration, CategoryConfig, ContentCategory, ContentItem, CategoryType } from '../types/ContentConfig';
import contentConfig from '../config/content_config.json';
import { buildContentMap } from '../data/contentData';

interface UseContentConfigOptions {
  useTestData: boolean;
}

interface UseContentConfigReturn {
  config: ContentConfiguration | null;
  categories: ContentCategory[];
  heroContent: ContentItem | null;
  isLoading: boolean;
}

export const useContentConfig = ({ useTestData }: UseContentConfigOptions): UseContentConfigReturn => {
  const [config, setConfig] = useState<ContentConfiguration | null>(null);
  const [categories, setCategories] = useState<ContentCategory[]>([]);
  const [heroContent, setHeroContent] = useState<ContentItem | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // Load config (in the future, this could fetch from an API)
    setConfig(contentConfig as ContentConfiguration);
  }, []);

  useEffect(() => {
    if (!config) return;

    setIsLoading(true);

    const allContent = buildContentMap();

    // Get active categories based on useTestData
    const activeCategories: CategoryConfig[] = useTestData
      ? [...config.categories, ...config.testCategories]
      : config.categories.filter(c => !c.isTestOnly);

    // Build categories with content
    const builtCategories = activeCategories
      .filter(c => c.enabled)
      .sort((a, b) => a.order - b.order)
      .map(categoryConfig => {
        const items = categoryConfig.contentIds
          .map(id => allContent[id])
          .filter((item): item is ContentItem => item !== undefined);

        const limitedItems = categoryConfig.maxItems
          ? items.slice(0, categoryConfig.maxItems)
          : items;

        return {
          id: categoryConfig.id,
          name: categoryConfig.name,
          type: categoryConfig.type as CategoryType,
          items: limitedItems,
          themeColor: categoryConfig.themeColor,
          themeIcon: categoryConfig.themeIcon,
        };
      })
      .filter(category => category.items.length > 0);

    setCategories(builtCategories);

    // Get hero content
    const hero = allContent[config.hero.contentId] ||
      (config.hero.fallbackContentId ? allContent[config.hero.fallbackContentId] : null);
    setHeroContent(hero);

    setIsLoading(false);
  }, [config, useTestData]);

  return {
    config,
    categories,
    heroContent,
    isLoading,
  };
};
