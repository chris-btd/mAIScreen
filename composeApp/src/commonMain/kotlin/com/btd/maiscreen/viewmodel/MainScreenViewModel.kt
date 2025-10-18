package com.btd.maiscreen.viewmodel

import com.btd.maiscreen.common.BaseViewModel
import com.btd.maiscreen.di.ContentModule
import com.btd.maiscreen.model.ApiResponseError
import com.btd.maiscreen.model.NetworkError
import com.btd.maiscreen.model.Success
import com.btd.maiscreen.repository.ContentRepository
import com.btd.maiscreen.ui.screens.ContentItem
import com.btd.maiscreen.ui.state.MainScreenUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * ViewModel for MainScreen following Clean Architecture pattern
 * Extends BaseViewModel for type-safe state management
 */
class MainScreenViewModel(
    private val repository: ContentRepository = ContentModule.provideContentRepository()
) : BaseViewModel<MainScreenUiState>(MainScreenUiState()) {
    
    // Create a CoroutineScope for this ViewModel
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    init {
        loadContent()
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
                                errorMessage = "Failed to load featured content: ${featuredResult.message}"
                            ) 
                        }
                    }
                    is NetworkError -> {
                        setState { 
                            copy(
                                hasError = true, 
                                errorMessage = "Network error: ${featuredResult.exception.message}"
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
                                errorMessage = "Failed to load categories: ${categoriesResult.message}",
                                isLoading = false
                            ) 
                        }
                    }
                    is NetworkError -> {
                        setState { 
                            copy(
                                hasError = true, 
                                errorMessage = "Network error: ${categoriesResult.exception.message}",
                                isLoading = false
                            ) 
                        }
                    }
                }
                
            } catch (e: Exception) {
                setState { 
                    copy(
                        hasError = true, 
                        errorMessage = "Unexpected error: ${e.message}",
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
     * Toggle like status for content
     */
    fun toggleLike(content: ContentItem) {
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
                                errorMessage = "Failed to update like: ${result.message}"
                            ) 
                        }
                    }
                    is NetworkError -> {
                        setState { 
                            copy(
                                hasError = true,
                                errorMessage = "Network error: ${result.exception.message}"
                            ) 
                        }
                    }
                }
            } catch (e: Exception) {
                setState { 
                    copy(
                        hasError = true,
                        errorMessage = "Error updating like: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    /**
     * Toggle watchlist status for content
     */
    fun toggleWatchlist(content: ContentItem) {
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
                                errorMessage = "Failed to update watchlist: ${result.message}"
                            ) 
                        }
                    }
                    is NetworkError -> {
                        setState { 
                            copy(
                                hasError = true,
                                errorMessage = "Network error: ${result.exception.message}"
                            ) 
                        }
                    }
                }
            } catch (e: Exception) {
                setState { 
                    copy(
                        hasError = true,
                        errorMessage = "Error updating watchlist: ${e.message}"
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
