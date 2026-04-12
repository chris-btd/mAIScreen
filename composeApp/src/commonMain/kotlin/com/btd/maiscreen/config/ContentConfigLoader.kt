package com.btd.maiscreen.config

import kotlinx.serialization.json.Json
import maiscreen.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

/**
 * Loader for content configuration
 * Loads from resource files
 */
object ContentConfigLoader {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // Thread-safe cache using @Volatile and synchronization
    @Volatile
    private var cachedConfig: ContentConfiguration? = null
    private val cacheLock = Any()

    /**
     * Get the current configuration
     * Loads default config if not already loaded
     */
    @OptIn(ExperimentalResourceApi::class)
    suspend fun getConfig(): ContentConfiguration {
        // Check if already cached
        cachedConfig?.let { return it }

        // Load config (thread-safe via synchronized on assignment only)
        val newConfig = loadDefaultConfig()

        // Thread-safe cache update
        synchronized(cacheLock) {
            // Check again in case another thread loaded it
            cachedConfig?.let { return it }
            cachedConfig = newConfig
            return newConfig
        }
    }

    /**
     * Load configuration from a JSON string
     */
    fun loadConfig(jsonString: String): ContentConfiguration {
        return synchronized(cacheLock) {
            json.decodeFromString<ContentConfiguration>(jsonString).also {
                cachedConfig = it
            }
        }
    }

    /**
     * Clear the cached configuration
     */
    fun clearCache() {
        synchronized(cacheLock) {
            cachedConfig = null
        }
    }

    /**
     * Load the default configuration from resource file
     */
    @OptIn(ExperimentalResourceApi::class)
    private suspend fun loadDefaultConfig(): ContentConfiguration {
        return try {
            val jsonString = Res.readBytes("files/content_config.json")
                .decodeToString()
            json.decodeFromString(jsonString)
        } catch (e: Exception) {
            println("Error loading config from resources: ${e.message}")
            // Fallback to minimal config if resource loading fails
            throw IllegalStateException("Failed to load content configuration: ${e.message}", e)
        }
    }
}

