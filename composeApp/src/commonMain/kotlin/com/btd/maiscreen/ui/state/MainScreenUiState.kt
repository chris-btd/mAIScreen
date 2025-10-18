package com.btd.maiscreen.ui.state

import com.btd.maiscreen.common.UiState
import com.btd.maiscreen.ui.screens.ContentCategory
import com.btd.maiscreen.ui.screens.ContentItem

/**
 * UI State for MainScreen following Clean Architecture pattern
 * Immutable data class that represents all possible UI states
 */
data class MainScreenUiState(
    val featuredContent: ContentItem? = null,
    val contentCategories: List<ContentCategory> = emptyList(),
    val isLoading: Boolean = false,
    val hasError: Boolean = false,
    val errorMessage: String? = null,
    val showDetailsDialog: Boolean = false,
    val selectedContent: ContentItem? = null,
    val watchList: List<ContentItem> = emptyList()
) : UiState

