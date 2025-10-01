package com.btd.maiscreen.di

import com.btd.maiscreen.repository.ContentRepository
import com.btd.maiscreen.repository.ContentRepositoryImpl

/**
 * Simple dependency injection module for content-related dependencies
 * Provides singleton instances following the dependency injection pattern
 */
object ContentModule {
    
    private var contentRepository: ContentRepository? = null
    
    /**
     * Provides singleton instance of ContentRepository
     * Following the Singleton pattern for dependency injection
     */
    fun provideContentRepository(): ContentRepository {
        return contentRepository ?: ContentRepositoryImpl().also {
            contentRepository = it
        }
    }
    
    /**
     * Clear all instances (useful for testing)
     */
    fun clearInstances() {
        contentRepository = null
    }
}

