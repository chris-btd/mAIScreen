package com.btd.maiscreen.auth

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual object IdTokenProvider {
    actual suspend fun getIdToken(): String? = suspendCancellableCoroutine { continuation ->
        val service = AuthManager.amplifyAuthService as? AmplifyAuthService
        if (service == null) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }
        service.getIdToken { token ->
            if (continuation.isActive) continuation.resume(token)
        }
    }
}
