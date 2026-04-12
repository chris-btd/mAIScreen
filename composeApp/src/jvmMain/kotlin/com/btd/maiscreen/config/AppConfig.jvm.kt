package com.btd.maiscreen.config

/**
 * JVM/Desktop-specific configuration implementation
 * Uses system properties or defaults for desktop testing
 */
actual object AppConfig {
    actual val cloudFrontDomain: String = System.getProperty("cloudfront.domain") ?: "d2ofvyi6ramrc2.cloudfront.net"
    actual val testEmail: String = System.getProperty("test.email") ?: "test@test.com"
    actual val testPassword: String = System.getProperty("test.password") ?: "1111111"
    
    // Demo video URLs for testing
    actual val demoVideo1: String = System.getProperty("demo.video1") ?: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
    actual val demoVideo2: String = System.getProperty("demo.video2") ?: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
    actual val demoVideo3: String = System.getProperty("demo.video3") ?: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
    
    // AWS Cognito Configuration (JVM/Desktop doesn't use Cognito, but provide defaults)
    actual val cognitoIssuerUri: String = System.getProperty("cognito.issuer") ?: "https://cognito-idp.us-east-1.amazonaws.com/us-east-1_rty4feGNd"
    actual val cognitoClientId: String = System.getProperty("cognito.client.id") ?: "11jljm542ir3b5ejqqeu9nn20q"
    actual val cognitoRedirectUri: String = System.getProperty("cognito.redirect.uri") ?: "https://d84l1y8p4kdic.cloudfront.net"
    actual val cognitoLogoutUri: String = System.getProperty("cognito.logout.uri") ?: "https://d84l1y8p4kdic.cloudfront.net/logout"
    actual val cognitoScope: String = System.getProperty("cognito.scope") ?: "email openid phone"

    // Build-time test mode flag (for JVM/Desktop, enable test mode by default)
    actual val isTestMode: Boolean = System.getProperty("test.mode")?.toBoolean() ?: true
}
