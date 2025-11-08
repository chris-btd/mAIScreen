# Security Documentation

This document outlines the security measures implemented in mAIScreen and provides setup instructions.

## Security Fixes Implemented

### Critical Security Fixes

#### 1. Removed Hardcoded Secrets ✅
**Issue**: Sensitive configuration values were hardcoded in `build.gradle.kts`
**Fix**: Moved all sensitive values to environment variables
**Action Required**: Set up environment variables (see Setup section below)

#### 2. Removed Sensitive Data Logging ✅
**Issue**: Tokens, passwords, and other sensitive data were logged in plain text
**Fix**:
- Created `SecureLogger` utility that automatically sanitizes sensitive data
- Removed all token/credential logging
- Debug logs are completely disabled in release builds
**Files Updated**:
- `AndroidPlaybackApi.kt`
- `CognitoAuthService.kt`
- `AmplifyAuthService.kt`
- `AppViewModel.kt`

#### 3. Password Strength Requirements ✅
**Issue**: Weak password requirements
**Fix**: Created `PasswordValidator` utility with requirements:
- Minimum 12 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one number
- At least one special character
- Check against common passwords
**File Created**: `util/PasswordValidator.kt`

### High Priority Security Fixes

#### 4. Input Validation in ViewModels ✅
**Issue**: No input validation on user actions
**Fix**:
- Added content ID validation (alphanumeric, hyphens, underscores only)
- Added plan and minutes validation in ProfileViewModel
**Files Updated**:
- `MainScreenViewModel.kt`
- `ProfileViewModel.kt`

#### 5. Fixed ViewModel Lifecycle/Memory Leaks ✅
**Issue**: Custom CoroutineScope could cause memory leaks
**Fix**: Use `androidx.lifecycle.viewModelScope` instead of custom scope
**File Updated**: `MainScreenViewModel.kt`

#### 6. Error Message Sanitization ✅
**Issue**: Raw error messages exposed in UI could lead to XSS
**Fix**:
- Added `sanitizeErrorMessage()` function to remove HTML tags and scripts
- Generic error messages for stack traces/internal errors
- Limited error message length to 200 characters
**Files Updated**:
- `AppViewModel.kt`
- `MainScreenViewModel.kt`

#### 7. Request Timeout Configuration ✅
**Issue**: No timeout configuration on HTTP requests
**Fix**: Added 30-second timeouts for connect, read, and write operations
**File Updated**: `AndroidPlaybackApi.kt`

#### 8. Rate Limiting/Debouncing ✅
**Issue**: No client-side rate limiting on user actions
**Fix**: Added 500ms debouncing on like and watchlist toggle actions
**File Updated**: `MainScreenViewModel.kt`

#### 9. Network Security Configuration ✅
**Issue**: No network security policy for Android
**Fix**:
- Created `network_security_config.xml` with:
  - Cleartext (HTTP) traffic disabled
  - Certificate pinning configuration (TODO: add actual pins)
  - Separate debug configuration for localhost
**Files Created/Updated**:
- `res/xml/network_security_config.xml`
- `AndroidManifest.xml`

## Required Setup

### Environment Variables

You must configure the following environment variables before building the app:

#### Option 1: local.properties (Recommended for Development)

Copy `local.properties.template` to `local.properties` and fill in your values:

```properties
CLOUDFRONT_DOMAIN=your-cloudfront-domain.cloudfront.net
TEST_EMAIL=your-test-email@example.com
TEST_PASSWORD=your-secure-test-password
COGNITO_ISSUER_URI=https://cognito-idp.us-east-1.amazonaws.com/us-east-1_XXXXXXXXX
COGNITO_CLIENT_ID=your-cognito-client-id-here
```

**IMPORTANT**: `local.properties` is already in `.gitignore`. Never commit this file!

#### Option 2: Environment Variables (Recommended for CI/CD)

Set these as environment variables in your CI/CD pipeline or shell:

```bash
export CLOUDFRONT_DOMAIN="your-cloudfront-domain.cloudfront.net"
export TEST_EMAIL="your-test-email@example.com"
export TEST_PASSWORD="your-secure-test-password"
export COGNITO_ISSUER_URI="https://cognito-idp.us-east-1.amazonaws.com/us-east-1_XXXXXXXXX"
export COGNITO_CLIENT_ID="your-cognito-client-id-here"
```

### Certificate Pinning (TODO)

To enable certificate pinning for CloudFront:

1. Get your CloudFront domain's certificate pin:
```bash
openssl s_client -connect your-domain.cloudfront.net:443 | openssl x509 -pubkey -noout | openssl pkey -pubin -outform der | openssl dgst -sha256 -binary | openssl enc -base64
```

2. Update `network_security_config.xml` with the pin value

3. Uncomment the certificate pinning code in `AndroidPlaybackApi.kt`

## Security Best Practices

### For Developers

1. **Never commit secrets**: Always use environment variables or local.properties
2. **Use SecureLogger**: Use `SecureLogger` instead of `Log` or `println` for logging
3. **Validate all inputs**: Always validate user inputs before processing
4. **Sanitize error messages**: Never show raw error messages to users
5. **Use strong passwords**: Enforce password strength requirements
6. **Keep dependencies updated**: Regularly update dependencies to patch vulnerabilities

### For Production Deployments

1. **Set environment variables securely**: Use your CI/CD system's secrets management
2. **Enable ProGuard/R8**: Ensure code obfuscation is enabled for release builds
3. **Implement certificate pinning**: Add actual certificate pins before production
4. **Regular security audits**: Perform periodic security reviews
5. **Monitor logs**: Set up log monitoring for security events
6. **Use secure storage**: Consider implementing secure token storage (Android Keystore/iOS Keychain)

## Known Security TODOs

The following security improvements are marked as TODO in the codebase:

1. **Certificate Pinning**: Add actual certificate pins for CloudFront domain
2. **Secure Token Storage**: Implement Android Keystore/iOS Keychain for token storage
3. **Replace Reflection**: Replace reflection-based token retrieval with proper SDK APIs
4. **Biometric Authentication**: Implement fingerprint/face authentication as an option
5. **Payment Backend**: Implement proper backend for payment processing

## Vulnerability Reporting

If you discover a security vulnerability, please report it to the repository maintainers privately. Do not create public issues for security vulnerabilities.

## Security Audit History

- **2025-11-08**: Initial security audit performed
  - Fixed critical and high-priority vulnerabilities
  - Implemented logging sanitization
  - Added input validation
  - Created network security configuration
