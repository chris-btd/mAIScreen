package com.btd.maiscreen.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.btd.maiscreen.ui.screens.ContentItem

/**
 * ViewModel for managing app-wide UI state and error messages
 */
class AppViewModel : ViewModel() {
    
    // Error message state
    var errorMessage by mutableStateOf<String?>(null)
        private set
    
    // Loading state
    var isLoading by mutableStateOf(false)
        private set
    
    // Success message state
    var successMessage by mutableStateOf<String?>(null)
        private set
    
    // Recently watched content (max 10 items)
    var recentlyWatchedContent by mutableStateOf<List<ContentItem>>(emptyList())
        private set
    
    // Show error message
    fun showError(message: String) {
        errorMessage = message
        println("❌ Error shown: $message")
    }
    
    // Clear error message
    fun clearError() {
        errorMessage = null
        println("✅ Error cleared")
    }
    
    // Show success message
    fun showSuccess(message: String) {
        successMessage = message
        println("✅ Success shown: $message")
    }
    
    // Clear success message
    fun clearSuccess() {
        successMessage = null
        println("✅ Success cleared")
    }
    
    // Set loading state
    fun setAppLoading(loading: Boolean) {
        isLoading = loading
        println("⏳ Loading state: $loading")
    }
    
    // Clear all messages
    fun clearAllMessages() {
        errorMessage = null
        successMessage = null
        isLoading = false
    }
    
    // Add content to recently watched
    fun addToRecentlyWatched(content: ContentItem) {
        val currentList = recentlyWatchedContent.toMutableList()
        
        // Remove if already exists to avoid duplicates
        currentList.removeAll { it.id == content.id }
        
        // Add to the beginning
        currentList.add(0, content)
        
        // Keep only the last 10 items
        if (currentList.size > 10) {
            recentlyWatchedContent = currentList.take(10)
        } else {
            recentlyWatchedContent = currentList
        }
        
        println("📺 Added to recently watched: ${content.title}")
    }
    
    // Clear recently watched
    fun clearRecentlyWatched() {
        recentlyWatchedContent = emptyList()
        println("🗑️ Recently watched cleared")
    }
}

