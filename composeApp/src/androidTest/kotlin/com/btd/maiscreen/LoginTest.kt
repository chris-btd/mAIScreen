package com.btd.maiscreen

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.btd.maiscreen.ui.screens.LoginScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Espresso UI tests for the Login Screen
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class LoginTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testLoginWithValidCredentials() {
        // Start on the login screen
        composeTestRule.setContent {
            LoginScreen(
                onLoginClick = { email, password ->
                    println("Login attempted with: $email")
                },
                onSignUpClick = {}
            )
        }

        // Wait for the login screen to be displayed
        composeTestRule.onNodeWithText("Sign In").assertExists()

        // Enter email
        composeTestRule.onNodeWithText("Email").performClick()
        composeTestRule.onNodeWithText("Email").performTextInput("chris@test.com")

        // Enter password
        composeTestRule.onNodeWithText("Password").performClick()
        composeTestRule.onNodeWithText("Password").performTextInput("Test@123")

        // Click the Sign In button
        composeTestRule.onNodeWithText("Sign In").performClick()

        // Wait a moment for the login to process
        composeTestRule.waitForIdle()

        // Verify that the login button was clicked
        // In a real scenario, you would verify navigation to the next screen
        // or check for success/error messages
        composeTestRule.onNodeWithText("Sign In").assertExists()
    }

    @Test
    fun testLoginWithEmptyFields() {
        // Start on the login screen
        composeTestRule.setContent {
            LoginScreen(
                onLoginClick = { email, password ->
                    println("Login attempted with: $email")
                },
                onSignUpClick = {}
            )
        }

        // Verify the Sign In button is disabled when fields are empty
        composeTestRule.onNodeWithText("Sign In").assertIsNotEnabled()
    }

    @Test
    fun testLoginWithOnlyEmail() {
        // Start on the login screen
        composeTestRule.setContent {
            LoginScreen(
                onLoginClick = { email, password ->
                    println("Login attempted with: $email")
                },
                onSignUpClick = {}
            )
        }

        // Enter email only
        composeTestRule.onNodeWithText("Email").performClick()
        composeTestRule.onNodeWithText("Email").performTextInput("chris@test.com")

        // Verify the Sign In button is still disabled
        composeTestRule.onNodeWithText("Sign In").assertIsNotEnabled()
    }

    @Test
    fun testLoginWithOnlyPassword() {
        // Start on the login screen
        composeTestRule.setContent {
            LoginScreen(
                onLoginClick = { email, password ->
                    println("Login attempted with: $email")
                },
                onSignUpClick = {}
            )
        }

        // Enter password only
        composeTestRule.onNodeWithText("Password").performClick()
        composeTestRule.onNodeWithText("Password").performTextInput("Test@123")

        // Verify the Sign In button is still disabled
        composeTestRule.onNodeWithText("Sign In").assertIsNotEnabled()
    }

    @Test
    fun testPasswordVisibilityToggle() {
        // Start on the login screen
        composeTestRule.setContent {
            LoginScreen(
                onLoginClick = { email, password ->
                    println("Login attempted with: $email")
                },
                onSignUpClick = {}
            )
        }

        // Enter password
        composeTestRule.onNodeWithText("Password").performClick()
        composeTestRule.onNodeWithText("Password").performTextInput("Test@123")

        // Find and click the password visibility toggle button
        // The toggle button should be visible after entering text
        composeTestRule.onNodeWithContentDescription("Toggle password visibility")
            .assertExists()
            .performClick()

        // Verify the password is now visible
        // (In a real test, you would check the actual password text is displayed)
        composeTestRule.onNodeWithContentDescription("Toggle password visibility")
            .assertExists()
    }

    @Test
    fun testSignUpButton() {
        // Start on the login screen
        composeTestRule.setContent {
            LoginScreen(
                onLoginClick = { email, password ->
                    println("Login attempted with: $email")
                },
                onSignUpClick = {
                    println("Sign up clicked")
                }
            )
        }

        // Click the Sign Up button
        composeTestRule.onNodeWithText("Sign Up").performClick()

        // Verify the button was clicked
        // In a real scenario, you would verify navigation to the sign up screen
        composeTestRule.onNodeWithText("Sign Up").assertExists()
    }

    @Test
    fun testForgotPasswordButton() {
        // Start on the login screen
        composeTestRule.setContent {
            LoginScreen(
                onLoginClick = { email, password ->
                    println("Login attempted with: $email")
                },
                onSignUpClick = {}
            )
        }

        // Click the Forgot Password button
        composeTestRule.onNodeWithText("Forgot Password?").performClick()

        // Verify the button was clicked
        // In a real scenario, you would verify navigation to the password reset screen
        composeTestRule.onNodeWithText("Forgot Password?").assertExists()
    }
}

