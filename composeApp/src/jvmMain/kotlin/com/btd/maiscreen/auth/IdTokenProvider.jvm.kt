package com.btd.maiscreen.auth

/**
 * Desktop/JVM target has no Cognito session — content catalog falls back to
 * test/config-driven data there (see ContentRepositoryImpl).
 */
actual object IdTokenProvider {
    actual suspend fun getIdToken(): String? = null
}
