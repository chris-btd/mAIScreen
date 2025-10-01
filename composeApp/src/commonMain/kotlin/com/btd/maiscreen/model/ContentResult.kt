package com.btd.maiscreen.model

/**
 * Sealed class for handling API responses and errors
 * Provides exhaustive result handling for repository operations
 */
sealed class ContentResult<out T>

/**
 * Successful result containing data
 */
data class Success<out T>(val data: T) : ContentResult<T>()

/**
 * API response error with message
 */
data class ApiResponseError(val message: String) : ContentResult<Nothing>()

/**
 * Network or general error
 */
data class NetworkError(val exception: Throwable) : ContentResult<Nothing>()

