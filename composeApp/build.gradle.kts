import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    jvm()
    
    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.stripe.android)
            implementation("androidx.media3:media3-exoplayer:1.4.1")
            implementation("androidx.media3:media3-ui:1.4.1")
            implementation("androidx.media3:media3-common:1.4.1")
            implementation("androidx.media3:media3-exoplayer-hls:1.4.1")
            implementation("androidx.media3:media3-datasource-okhttp:1.4.1")
            implementation(libs.okhttp)
            implementation(libs.appauth)
            implementation("androidx.browser:browser:1.7.0")
            
            // AWS Amplify for Cognito User Pool management
            implementation("com.amplifyframework:aws-auth-cognito:2.14.5")
            implementation("com.amplifyframework:core:2.14.5")
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(projects.shared)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }
    }
}

android {
    namespace = "com.btd.maiscreen"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.btd.maiscreen"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
        
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Build configuration for sensitive values
        buildConfigField("String", "CLOUDFRONT_DOMAIN", "\"d2ofvyi6ramrc2.cloudfront.net\"")
        buildConfigField("String", "TEST_EMAIL", "\"test@test.com\"")
        buildConfigField("String", "TEST_PASSWORD", "\"1111111\"")
        
        // Demo video URLs for testing
        buildConfigField("String", "DEMO_VIDEO_1", "\"https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4\"")
        buildConfigField("String", "DEMO_VIDEO_2", "\"https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4\"")
        buildConfigField("String", "DEMO_VIDEO_3", "\"https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4\"")
        
        // AWS Cognito Configuration
        buildConfigField("String", "COGNITO_ISSUER_URI", "\"https://cognito-idp.us-east-1.amazonaws.com/us-east-1_rty4feGNd\"")
        buildConfigField("String", "COGNITO_CLIENT_ID", "\"11jljm542ir3b5ejqqeu9nn20q\"")
        buildConfigField("String", "COGNITO_REDIRECT_URI", "\"maiscreen.auth://callback\"")
        buildConfigField("String", "COGNITO_LOGOUT_URI", "\"maiscreen.auth://logout\"")
        buildConfigField("String", "COGNITO_SCOPE", "\"email openid phone\"")
        
        // Manifest placeholders for Cognito redirect handling
        manifestPlaceholders["appAuthRedirectScheme"] = "maiscreen.auth"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildFeatures {
        buildConfig = true
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
    
    // Android Test Dependencies
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.testExt.junit)
    androidTestImplementation(libs.kotlin.testJunit)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.9.0")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test:rules:1.6.1")
}

compose.desktop {
    application {
        mainClass = "com.btd.maiscreen.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.btd.maiscreen"
            packageVersion = "1.0.0"
        }
    }
}
