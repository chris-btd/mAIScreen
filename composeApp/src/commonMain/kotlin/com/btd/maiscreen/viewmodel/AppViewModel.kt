package com.btd.maiscreen.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

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
}

