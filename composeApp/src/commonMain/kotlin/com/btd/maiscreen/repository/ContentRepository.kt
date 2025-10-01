package com.btd.maiscreen.repository

import com.btd.maiscreen.model.ContentResult
import com.btd.maiscreen.streaming.PlaybackResult
import com.btd.maiscreen.ui.screens.ContentCategory
import com.btd.maiscreen.ui.screens.ContentItem

/**
 * Repository interface for content data operations
 * Follows Repository pattern for data abstraction
 */
interface ContentRepository {
    /**
     * Get featured content for the main screen
     */
    suspend fun getFeaturedContent(): ContentResult<ContentItem>
    
    /**
     * Get all content categories with their items
     */
    suspend fun getContentCategories(): ContentResult<List<ContentCategory>>
    
    /**
     * Toggle like status for a content item
     */
    suspend fun toggleLike(contentId: String): ContentResult<ContentItem>
    
    /**
     * Toggle watchlist status for a content item
     */
    suspend fun toggleWatchlist(contentId: String): ContentResult<ContentItem>
    
    /**
     * Get HLS manifest URL for streaming a content item
     */
    suspend fun getStreamingUrl(
        contentItem: ContentItem, 
        tier: String = "free", 
        mode: String = "video"
    ): PlaybackResult<String>
}
