package com.btd.maiscreen.config

import kotlinx.serialization.json.Json

/**
 * Loader for content configuration
 * Currently loads from embedded JSON string, can be extended to load from file or API
 */
object ContentConfigLoader {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // In-memory cache
    private var cachedConfig: ContentConfiguration? = null

    /**
     * Get the current configuration
     * Loads default config if not already loaded
     */
    fun getConfig(): ContentConfiguration {
        return cachedConfig ?: loadDefaultConfig().also {
            cachedConfig = it
        }
    }

    /**
     * Load configuration from a JSON string
     */
    fun loadConfig(jsonString: String): ContentConfiguration {
        return json.decodeFromString<ContentConfiguration>(jsonString).also {
            cachedConfig = it
        }
    }

    /**
     * Clear the cached configuration
     */
    fun clearCache() {
        cachedConfig = null
    }

    /**
     * Load the default embedded configuration
     */
    private fun loadDefaultConfig(): ContentConfiguration {
        return json.decodeFromString(DEFAULT_CONFIG_JSON)
    }
}

/**
 * Default embedded configuration JSON
 * This can be replaced with file loading or API fetching in the future
 */
private const val DEFAULT_CONFIG_JSON = """
{
  "version": "1.0",
  "hero": {
    "contentId": "featured1",
    "fallbackContentId": "1"
  },
  "categories": [
    {
      "id": "top10",
      "name": "Top 10 Trending",
      "type": "top10",
      "order": 1,
      "enabled": true,
      "isTestOnly": false,
      "contentIds": ["1", "2", "3", "4", "5", "6", "7", "comic1", "comic2", "comic3"],
      "maxItems": 10
    },
    {
      "id": "bhm",
      "name": "Black History Month",
      "type": "themed",
      "order": 2,
      "enabled": true,
      "isTestOnly": false,
      "contentIds": ["5", "6", "7"],
      "themeColor": "#FFC107",
      "themeIcon": "..."
    },
    {
      "id": "trending",
      "name": "Trending Now",
      "type": "standard",
      "order": 3,
      "enabled": true,
      "isTestOnly": false,
      "contentIds": ["1", "2", "3", "4"]
    },
    {
      "id": "ai_originals",
      "name": "AI Originals",
      "type": "standard",
      "order": 4,
      "enabled": true,
      "isTestOnly": false,
      "contentIds": ["5", "6"]
    },
    {
      "id": "popular",
      "name": "Popular This Week",
      "type": "standard",
      "order": 5,
      "enabled": true,
      "isTestOnly": false,
      "contentIds": ["7"]
    },
    {
      "id": "comics",
      "name": "Comic Books",
      "type": "standard",
      "order": 6,
      "enabled": true,
      "isTestOnly": false,
      "contentIds": ["comic1", "comic2", "comic3", "comic4"]
    }
  ],
  "testCategories": [
    {
      "id": "test",
      "name": "Test",
      "type": "standard",
      "order": 0,
      "enabled": true,
      "isTestOnly": true,
      "contentIds": ["TEST001"]
    }
  ]
}
"""
