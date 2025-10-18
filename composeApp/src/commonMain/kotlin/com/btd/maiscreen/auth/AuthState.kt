package com.btd.maiscreen.auth

import androidx.compose.runtime.*

/**
 * Global authentication state that can be observed by the UI
 */
object AppAuthState {
    private var _isAuthenticated = mutableStateOf(false)
    val isAuthenticated: State<Boolean> = _isAuthenticated
    
    private var _userEmail = mutableStateOf<String?>(null)
    val userEmail: State<String?> = _userEmail
    
    fun setAuthenticated(authenticated: Boolean, email: String? = null) {
        _isAuthenticated.value = authenticated
        _userEmail.value = email
        println("🔐 AuthState updated: authenticated=$authenticated, email=$email")
    }
    
    fun logout() {
        _isAuthenticated.value = false
        _userEmail.value = null
        println("🚪 AuthState: User logged out")
    }
}
