package com.btd.maiscreen.viewmodel

import androidx.lifecycle.viewModelScope
import com.btd.maiscreen.common.BaseViewModel
import com.btd.maiscreen.di.ContentModule
import com.btd.maiscreen.model.ApiResponseError
import com.btd.maiscreen.model.NetworkError
import com.btd.maiscreen.model.Success
import com.btd.maiscreen.repository.ContentRepository
import com.btd.maiscreen.ui.screens.ContentItem
import com.btd.maiscreen.ui.state.MainScreenUiState
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ViewModel for MainScreen following Clean Architecture pattern
 * Extends BaseViewModel for type-safe state management
 */
class MainScreenViewModel(
    private val repository: ContentRepository = ContentModule.provideContentRepository()
) : BaseViewModel<MainScreenUiState>(MainScreenUiState()) {

    // Debouncing timestamps for user actions
    private var lastLikeActionTime = 0L
    private var lastWatchlistActionTime = 0L
    private val debounceDelay = 500L // milliseconds

    init {
        loadContent()
    }

    /**
     * Sanitize error messages to prevent XSS and limit sensitive information exposure
     */
    private fun sanitizeErrorMessage(message: String): String {
        // Remove any potential HTML/script tags and limit length
        val sanitized = message
            .replace(Regex("<[^>]*>"), "")
            .replace(Regex("javascript:"), "")
            .take(200) // Limit message length

        // If message contains stack traces or paths, use generic message
        return if (sanitized.contains(Regex("at \\w+\\.\\w+")) || sanitized.contains("/")) {
            "An error occurred. Please try again later."
        } else {
            sanitized.ifEmpty { "An error occurred. Please try again later." }
        }
    }

    /**
     * Validate content ID to prevent injection attacks
     */
    private fun isValidContentId(id: String): Boolean {
        // Content IDs should be alphanumeric with hyphens and underscores only
        return id.matches(Regex("^[a-zA-Z0-9_-]+$")) && id.length <= 100
    }
    
    /**
     * Load all content data (featured content and categories)
     */
    private fun loadContent() {
        viewModelScope.launch {
            setState { copy(isLoading = true, hasError = false) }
            
            try {
                // Load featured content and categories in parallel
                val featuredContentDeferred = async { repository.getFeaturedContent() }
                val categoriesDeferred = async { repository.getContentCategories() }
                
                val featuredResult = featuredContentDeferred.await()
                val categoriesResult = categoriesDeferred.await()
                
                // Handle featured content result
                when (featuredResult) {
                    is Success -> {
                        setState { copy(featuredContent = featuredResult.data) }
                    }
                    is ApiResponseError -> {
                        setState {
                            copy(
                                hasError = true,
                                errorMessage = sanitizeErrorMessage("Failed to load featured content: ${featuredResult.message}")
                            )
                        }
                    }
                    is NetworkError -> {
                        setState {
                            copy(
                                hasError = true,
                                errorMessage = "Network error. Please check your connection and try again."
                            )
                        }
                    }
                }

                // Handle categories result
                when (categoriesResult) {
                    is Success -> {
                        setState {
                            copy(
                                contentCategories = categoriesResult.data,
                                isLoading = false
                            )
                        }
                    }
                    is ApiResponseError -> {
                        setState {
                            copy(
                                hasError = true,
                                errorMessage = sanitizeErrorMessage("Failed to load categories: ${categoriesResult.message}"),
                                isLoading = false
                            )
                        }
                    }
                    is NetworkError -> {
                        setState {
                            copy(
                                hasError = true,
                                errorMessage = "Network error. Please check your connection and try again.",
                                isLoading = false
                            )
                        }
                    }
                }

            } catch (e: Exception) {
                setState {
                    copy(
                        hasError = true,
                        errorMessage = "An error occurred. Please try again later.",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    /**
     * Show details dialog for selected content
     */
    fun showContentDetails(content: ContentItem) {
        setState { 
            copy(
                showDetailsDialog = true,
                selectedContent = content
            ) 
        }
    }
    
    /**
     * Hide details dialog
     */
    fun hideContentDetails() {
        setState { 
            copy(
                showDetailsDialog = false,
                selectedContent = null
            ) 
        }
    }
    
    /**
     * Toggle like status for content with debouncing and validation
     */
    fun toggleLike(content: ContentItem) {
        // Debounce: Prevent rapid-fire requests
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastLikeActionTime < debounceDelay) {
            return
        }
        lastLikeActionTime = currentTime

        // Validate content ID
        if (!isValidContentId(content.id)) {
            setState {
                copy(
                    hasError = true,
                    errorMessage = "Invalid content ID"
                )
            }
            return
        }

        viewModelScope.launch {
            try {
                when (val result = repository.toggleLike(content.id)) {
                    is Success -> {
                        // Update the content in our state
                        updateContentInState(result.data)
                    }
                    is ApiResponseError -> {
                        setState {
                            copy(
                                hasError = true,
                                errorMessage = sanitizeErrorMessage("Failed to update like: ${result.message}")
                            )
                        }
                    }
                    is NetworkError -> {
                        setState {
                            copy(
                                hasError = true,
                                errorMessage = "Network error. Please check your connection and try again."
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                setState {
                    copy(
                        hasError = true,
                        errorMessage = "An error occurred. Please try again later."
                    )
                }
            }
        }
    }
    
    /**
     * Toggle watchlist status for content with debouncing and validation
     */
    fun toggleWatchlist(content: ContentItem) {
        // Debounce: Prevent rapid-fire requests
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastWatchlistActionTime < debounceDelay) {
            return
        }
        lastWatchlistActionTime = currentTime

        // Validate content ID
        if (!isValidContentId(content.id)) {
            setState {
                copy(
                    hasError = true,
                    errorMessage = "Invalid content ID"
                )
            }
            return
        }

        viewModelScope.launch {
            try {
                when (val result = repository.toggleWatchlist(content.id)) {
                    is Success -> {
                        // Update the content in our state
                        val updatedContent = result.data
                        updateContentInState(updatedContent)

                        // Update watchlist based on the new status
                        val currentState = state.value
                        val newWatchList = if (updatedContent.isInWatchlist) {
                            // Add to watchlist if not already present
                            if (currentState.watchList.none { it.id == updatedContent.id }) {
                                currentState.watchList + updatedContent
                            } else {
                                currentState.watchList
                            }
                        } else {
                            // Remove from watchlist
                            currentState.watchList.filter { it.id != updatedContent.id }
                        }
                        setState { copy(watchList = newWatchList) }
                    }
                    is ApiResponseError -> {
                        setState {
                            copy(
                                hasError = true,
                                errorMessage = sanitizeErrorMessage("Failed to update watchlist: ${result.message}")
                            )
                        }
                    }
                    is NetworkError -> {
                        setState {
                            copy(
                                hasError = true,
                                errorMessage = "Network error. Please check your connection and try again."
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                setState {
                    copy(
                        hasError = true,
                        errorMessage = "An error occurred. Please try again later."
                    )
                }
            }
        }
    }
    
    /**
     * Clear error state
     */
    fun clearError() {
        setState { copy(hasError = false, errorMessage = null) }
    }
    
    /**
     * Retry loading content
     */
    fun retryLoadContent() {
        loadContent()
    }
    
    /**
     * Update a content item in the current state
     */
    private fun updateContentInState(updatedContent: ContentItem) {
        setState { 
            val updatedCategories = contentCategories.map { category ->
                category.copy(
                    items = category.items.map { item ->
                        if (item.id == updatedContent.id) updatedContent else item
                    }
                )
            }
            
            val updatedFeaturedContent = if (featuredContent?.id == updatedContent.id) {
                updatedContent
            } else {
                featuredContent
            }
            
            val updatedSelectedContent = if (selectedContent?.id == updatedContent.id) {
                updatedContent
            } else {
                selectedContent
            }
            
            copy(
                featuredContent = updatedFeaturedContent,
                contentCategories = updatedCategories,
                selectedContent = updatedSelectedContent
            )
        }
    }
}
