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
}
