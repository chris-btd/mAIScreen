package com.btd.maiscreen.util

import android.util.Log
import com.btd.maiscreen.BuildConfig

/**
 * Secure logging utility that removes sensitive data from logs in production builds
 * and completely disables debug logs in release builds.
 */
object SecureLogger {

    /**
     * Log a debug message
     * In release builds, this does nothing
     */
    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, sanitizeMessage(message))
        }
    }

    /**
     * Log an info message
     */
    fun i(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, sanitizeMessage(message))
        }
    }

    /**
     * Log a warning message
     * Suppressed in release builds — warning messages can contain sensitive context.
     */
    fun w(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.w(tag, sanitizeMessage(message))
        }
    }

    /**
     * Log an error message
     * Suppressed in release builds — error messages can contain sensitive context
     * such as server responses or internal state. Use crash reporting tools for
     * production error observability instead.
     */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            if (throwable != null) {
                Log.e(tag, sanitizeMessage(message), throwable)
            } else {
                Log.e(tag, sanitizeMessage(message))
            }
        }
    }

    /**
     * Sanitize a message to remove sensitive data patterns
     */
    private fun sanitizeMessage(message: String): String {
        if (BuildConfig.DEBUG) {
            // In debug builds, only redact actual token values
            return message
                .replace(Regex("Bearer\\s+[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+"), "Bearer [REDACTED]")
                .replace(Regex("token[\"']?\\s*[:=]\\s*[\"']?[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+"), "token: [REDACTED]")
                .replace(Regex("password[\"']?\\s*[:=]\\s*[\"']?\\S+"), "password: [REDACTED]")
                .replace(Regex("code[\"']?\\s*[:=]\\s*[\"']?\\d{6,}"), "code: [REDACTED]")
        } else {
            // In release builds, be more aggressive with redaction
            return message
                .replace(Regex("Bearer\\s+\\S+"), "Bearer [REDACTED]")
                .replace(Regex("token[\"']?\\s*[:=]\\s*[\"']?\\S+"), "token: [REDACTED]")
                .replace(Regex("password[\"']?\\s*[:=]\\s*[\"']?\\S+"), "password: [REDACTED]")
                .replace(Regex("code[\"']?\\s*[:=]\\s*[\"']?\\S+"), "code: [REDACTED]")
                .replace(Regex("[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+"), "[REDACTED_JWT]")
        }
    }
}
