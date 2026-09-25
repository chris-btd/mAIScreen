package com.btd.maiscreen.auth

/**
 * Platform-specific access to the current signed-in user's ID token (JWT),
 * used to authenticate calls to AppSync/API Gateway. Returns null if signed out.
 */
expect object IdTokenProvider {
    suspend fun getIdToken(): String?
}
