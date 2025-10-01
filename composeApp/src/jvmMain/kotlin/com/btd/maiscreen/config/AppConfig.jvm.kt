package com.btd.maiscreen.config

/**
 * JVM/Desktop-specific configuration implementation
 * Uses system properties or defaults for desktop testing
 */
actual object AppConfig {
    actual val cloudFrontDomain: String = System.getProperty("cloudfront.domain") ?: "d2ofvyi6ramrc2.cloudfront.net"
    actual val testEmail: String = System.getProperty("test.email") ?: "test@test.com"
    actual val testPassword: String = System.getProperty("test.password") ?: "1111111"
}
