package com.btd.maiscreen.config

/**
 * iOS-specific configuration implementation
 * Uses hardcoded values for iOS (can be extended to use Info.plist)
 */
actual object AppConfig {
    actual val cloudFrontDomain: String = "d2ofvyi6ramrc2.cloudfront.net"
    actual val testEmail: String = "test@test.com"
    actual val testPassword: String = "1111111"

    // Demo video URLs for testing
    actual val demoVideo1: String = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
    actual val demoVideo2: String = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
    actual val demoVideo3: String = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"

    // AWS Cognito Configuration
    actual val cognitoIssuerUri: String = "https://cognito-idp.us-east-1.amazonaws.com/us-east-1_rty4feGNd"
    actual val cognitoClientId: String = "11jljm542ir3b5ejqqeu9nn20q"
    actual val cognitoRedirectUri: String = "maiscreen.auth://callback"
    actual val cognitoLogoutUri: String = "maiscreen.auth://logout"
    actual val cognitoScope: String = "email openid phone"

    // Build-time test mode flag (for iOS, enable in debug builds only)
    // In production this should be controlled via build schemes
    actual val isTestMode: Boolean = true  // TODO: Use build configuration from Xcode
}
