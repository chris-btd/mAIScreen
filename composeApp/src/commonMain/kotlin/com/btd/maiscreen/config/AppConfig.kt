package com.btd.maiscreen.config

/**
 * Application configuration constants
 * Platform-specific implementations provide actual values
 */
expect object AppConfig {
    val cloudFrontDomain: String
    val testEmail: String
    val testPassword: String
}
