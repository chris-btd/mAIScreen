# Android Espresso Tests

This directory contains UI tests for the mAIScreen Android app using Espresso and Compose Testing.

## Test Files

### LoginTest.kt
Basic unit tests for the Login Screen component:
- `testLoginWithValidCredentials()` - Tests login with chris@test.com / Test@123
- `testLoginWithEmptyFields()` - Verifies Sign In button is disabled with empty fields
- `testLoginWithOnlyEmail()` - Verifies Sign In button is disabled with only email
- `testLoginWithOnlyPassword()` - Verifies Sign In button is disabled with only password
- `testPasswordVisibilityToggle()` - Tests password visibility toggle functionality
- `testSignUpButton()` - Tests navigation to sign up screen
- `testForgotPasswordButton()` - Tests forgot password functionality

### LoginFlowTest.kt
End-to-end tests for the complete login flow:
- `testCompleteLoginFlow()` - Full login flow with chris@test.com / Test@123
- `testLoginWithInvalidCredentials()` - Tests login with invalid credentials
- `testLoginFormValidation()` - Tests form validation logic
- `testNavigateToSignUp()` - Tests navigation to sign up screen
- `testPasswordVisibilityToggle()` - Tests password visibility toggle

## Running the Tests

### Run All Tests
```bash
./gradlew :composeApp:connectedAndroidTest
```

### Run Specific Test Class
```bash
./gradlew :composeApp:connectedAndroidTest --tests "com.btd.maiscreen.LoginTest"
```

### Run Specific Test Method
```bash
./gradlew :composeApp:connectedAndroidTest --tests "com.btd.maiscreen.LoginTest.testLoginWithValidCredentials"
```

### Run Tests from Android Studio
1. Open the test file in Android Studio
2. Right-click on the test class or method
3. Select "Run 'TestName'"
4. Or click the green play button next to the test

## Prerequisites

1. **Android Emulator or Device**
   - Make sure you have an Android emulator running or a physical device connected
   - Check with: `adb devices`

2. **Test Credentials**
   - Email: chris@test.com
   - Password: Test@123
   - Make sure this account exists in your AWS Cognito User Pool

3. **Build the App**
   ```bash
   ./gradlew :composeApp:assembleDebug
   ```

## Test Configuration

### Test Credentials
The tests use the following credentials:
- **Email**: chris@test.com
- **Password**: Test@123

### Test Timeouts
- Default timeout: 3 seconds for splash screen
- Login processing: 2 seconds
- Navigation: 500ms

## Writing New Tests

### Basic Test Structure
```kotlin
@Test
fun testYourFeature() {
    // 1. Navigate to the screen you want to test
    composeTestRule.setContent {
        YourScreen(onAction = {})
    }
    
    // 2. Find UI elements
    composeTestRule.onNodeWithText("Button Text").assertExists()
    
    // 3. Perform actions
    composeTestRule.onNodeWithText("Button Text").performClick()
    
    // 4. Verify results
    composeTestRule.onNodeWithText("Expected Result").assertExists()
}
```

### Common Test Operations
- **Click**: `performClick()`
- **Enter Text**: `performTextInput("text")`
- **Clear Text**: `performTextClearance()`
- **Assert Exists**: `assertExists()`
- **Assert Enabled**: `assertIsEnabled()`
- **Assert Disabled**: `assertIsNotEnabled()`

### Finding Elements
- **By Text**: `onNodeWithText("text")`
- **By Content Description**: `onNodeWithContentDescription("description")`
- **By Tag**: `onNodeWithTag("tag")`
- **With Substring**: `onNodeWithText("partial", substring = true)`

## Troubleshooting

### Tests Fail with "No Activity Found"
- Make sure the app is installed: `./gradlew :composeApp:installDebug`
- Check if emulator/device is connected: `adb devices`

### Tests Timeout
- Increase wait times: `Thread.sleep(milliseconds)`
- Check if the UI element is actually visible
- Add `assertExists()` before performing actions

### Cannot Find UI Elements
- Use Android Studio's Layout Inspector to find the exact text/content
- Check if the element is in a different screen/state
- Use `onAllNodesWithText()` for multiple matches

## Best Practices

1. **Use Descriptive Test Names**
   - `testLoginWithValidCredentials()` ✅
   - `test1()` ❌

2. **One Assertion Per Test**
   - Each test should verify one specific behavior
   - Makes failures easier to debug

3. **Clean Up After Tests**
   - Log out users between tests
   - Reset app state if needed

4. **Use Waits Appropriately**
   - Don't use `Thread.sleep()` excessively
   - Use `waitForIdle()` for Compose state updates

5. **Test User Flows, Not Implementation**
   - Test what the user sees and does
   - Don't test internal implementation details

## Future Test Ideas

- [ ] Video player tests (play, pause, seek)
- [ ] Settings screen tests
- [ ] Purchase minutes flow tests
- [ ] Content browsing tests
- [ ] Search functionality tests
- [ ] Profile screen tests
- [ ] Subscription flow tests

## Resources

- [Compose Testing Documentation](https://developer.android.com/jetpack/compose/testing)
- [Espresso Documentation](https://developer.android.com/training/testing/espresso)
- [Android Testing Guide](https://developer.android.com/training/testing)

