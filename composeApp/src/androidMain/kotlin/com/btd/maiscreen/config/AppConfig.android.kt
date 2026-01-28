package com.btd.maiscreen.config

import com.btd.maiscreen.BuildConfig

/**
 * Android-specific configuration implementation
 * Uses BuildConfig values from gradle
 */
actual object AppConfig {
    actual val cloudFrontDomain: String = BuildConfig.CLOUDFRONT_DOMAIN
    actual val testEmail: String = BuildConfig.TEST_EMAIL
    actual val testPassword: String = BuildConfig.TEST_PASSWORD

    // Demo video URLs for testing
    actual val demoVideo1: String = BuildConfig.DEMO_VIDEO_1
    actual val demoVideo2: String = BuildConfig.DEMO_VIDEO_2
    actual val demoVideo3: String = BuildConfig.DEMO_VIDEO_3

    // AWS Cognito Configuration
    actual val cognitoIssuerUri: String = BuildConfig.COGNITO_ISSUER_URI
    actual val cognitoClientId: String = BuildConfig.COGNITO_CLIENT_ID
    actual val cognitoRedirectUri: String = BuildConfig.COGNITO_REDIRECT_URI
    actual val cognitoLogoutUri: String = BuildConfig.COGNITO_LOGOUT_URI
    actual val cognitoScope: String = BuildConfig.COGNITO_SCOPE

    // Build-time test mode flag (secure - cannot be changed at runtime)
    actual val isTestMode: Boolean = BuildConfig.IS_TEST_MODE
}
