package com.btd.maiscreen.common

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Base ViewModel with generic UI state management
 * Provides type-safe, immutable state management for all ViewModels
 */
open class BaseViewModel<T : UiState>(initialState: T) {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<T> = _state.asStateFlow()

    /**
     * Updates the UI state using functional update pattern
     * Ensures immutable state updates with copy() semantics
     */
    protected fun setState(update: T.() -> T) {
        _state.update { currentState -> currentState.update() }
    }
}

/**
 * Marker interface for all UI state classes
 * Ensures type safety in BaseViewModel
 */
interface UiState

