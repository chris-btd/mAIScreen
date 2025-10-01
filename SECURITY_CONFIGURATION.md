# Security Configuration Guide

This document explains how sensitive configuration is handled in the mAIScreen project.

## 🔒 Security Approach

### Build Configuration (Android)
Sensitive values are stored in `build.gradle.kts` using `buildConfigField`:

```kotlin
buildConfigField("String", "CLOUDFRONT_DOMAIN", "\"your-domain.cloudfront.net\"")
buildConfigField("String", "TEST_EMAIL", "\"test@example.com\"")
buildConfigField("String", "TEST_PASSWORD", "\"secure-password\"")
```

### Multiplatform Configuration
The `AppConfig` expect/actual pattern provides platform-specific access:

- **Android**: Uses `BuildConfig` values from Gradle
- **JVM/Desktop**: Uses system properties with fallbacks
- **iOS**: Can be extended to use iOS-specific configuration

## 🛡️ Production Setup

### 1. Environment Variables (Recommended)
For production, use environment variables instead of hardcoded values:

```bash
export CLOUDFRONT_DOMAIN="your-production-domain.cloudfront.net"
export TEST_EMAIL="test@yourcompany.com"
export TEST_PASSWORD="secure-random-password"
```

### 2. Build Variants
Create different build variants for different environments:

```kotlin
buildTypes {
    debug {
        buildConfigField("String", "CLOUDFRONT_DOMAIN", "\"dev.example.com\"")
    }
    release {
        buildConfigField("String", "CLOUDFRONT_DOMAIN", "\"prod.example.com\"")
    }
}
```

### 3. Secrets Management
For enterprise deployments, consider:
- AWS Secrets Manager
- Azure Key Vault
- HashiCorp Vault
- Android Keystore (for mobile-specific secrets)

## 📝 Configuration Files

### `.gitignore` Protection
The following patterns are excluded from version control:
```
.env*
*.key
*.pem
secrets/
config/local.properties
*.keystore
*.jks
```

### Example Configuration
See `config.example.properties` for a template of local configuration.

## ⚠️ Security Checklist

Before committing code:
- [ ] No hardcoded domains or URLs
- [ ] No test credentials in source code
- [ ] Sensitive values use build configuration
- [ ] `.gitignore` excludes secret files
- [ ] Production uses environment variables

## 🔧 Development Workflow

1. **Local Development**: Use build configuration with development values
2. **CI/CD**: Inject environment variables during build
3. **Production**: Use secure secret management systems

## 📚 References

- [Android Build Configuration](https://developer.android.com/studio/build/gradle-tips#share-properties)
- [Kotlin Multiplatform expect/actual](https://kotlinlang.org/docs/multiplatform-connect-to-apis.html)
- [Security Best Practices](https://owasp.org/www-project-mobile-top-10/)
