package com.btd.maiscreen.config

/**
 * Application configuration constants
 * Platform-specific implementations provide actual values
 */
expect object AppConfig {
    val cloudFrontDomain: String
    val testEmail: String
    val testPassword: String

    // Demo video URLs for testing
    val demoVideo1: String
    val demoVideo2: String
    val demoVideo3: String

    // AWS Cognito Configuration
    val cognitoIssuerUri: String
    val cognitoClientId: String
    val cognitoRedirectUri: String
    val cognitoLogoutUri: String
    val cognitoScope: String

    // Build-time test mode flag (secure)
    val isTestMode: Boolean
}
