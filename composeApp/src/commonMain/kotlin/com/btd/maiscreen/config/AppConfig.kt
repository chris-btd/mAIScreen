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

<<<<<<< Updated upstream
    // Build-time test mode flag (secure)
    val isTestMode: Boolean
=======
    // AWS AppSync (content catalog GraphQL endpoint)
    val appSyncEndpoint: String

    // AWS API Gateway (REST edge routes behind CloudFront /api/*, e.g. /api/generate-video)
    val apiGatewayBaseUrl: String
>>>>>>> Stashed changes
}
