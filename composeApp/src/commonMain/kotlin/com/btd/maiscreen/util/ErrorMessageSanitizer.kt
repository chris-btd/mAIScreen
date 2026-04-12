package com.btd.maiscreen.util

/**
 * Shared utility for sanitizing error messages to prevent XSS and limit sensitive
 * information exposure. Single source of truth used by all ViewModels.
 */
object ErrorMessageSanitizer {

    private val STACK_TRACE_PATTERN = Regex("at \\w+\\.\\w+")
    // Match absolute Unix paths (/foo/bar) or Windows paths (C:\foo)
    private val ABSOLUTE_PATH_PATTERN = Regex("(^|\\s)/[\\w/.]|[A-Za-z]:\\\\")

    fun sanitize(message: String): String {
        val sanitized = message
            .replace(Regex("<[^>]*>"), "")
            .replace("javascript:", "")
            .take(200)

        return if (sanitized.contains(STACK_TRACE_PATTERN) || sanitized.contains(ABSOLUTE_PATH_PATTERN)) {
            "An error occurred. Please try again later."
        } else {
            sanitized.ifEmpty { "An error occurred. Please try again later." }
        }
    }
}
