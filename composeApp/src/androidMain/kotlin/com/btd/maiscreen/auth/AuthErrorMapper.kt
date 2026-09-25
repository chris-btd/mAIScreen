package com.btd.maiscreen.auth

import com.amplifyframework.auth.AuthException
import com.amplifyframework.auth.cognito.exceptions.service.CodeExpiredException
import com.amplifyframework.auth.cognito.exceptions.service.CodeMismatchException
import com.amplifyframework.auth.cognito.exceptions.service.FailedAttemptsLimitExceededException
import com.amplifyframework.auth.cognito.exceptions.service.InvalidParameterException
import com.amplifyframework.auth.cognito.exceptions.service.InvalidPasswordException
import com.amplifyframework.auth.cognito.exceptions.service.LimitExceededException
import com.amplifyframework.auth.cognito.exceptions.service.PasswordResetRequiredException
import com.amplifyframework.auth.cognito.exceptions.service.TooManyRequestsException
import com.amplifyframework.auth.cognito.exceptions.service.UserNotConfirmedException
import com.amplifyframework.auth.cognito.exceptions.service.UserNotFoundException
import com.amplifyframework.auth.cognito.exceptions.service.UsernameExistsException
import com.amplifyframework.auth.exceptions.NotAuthorizedException
import com.amplifyframework.auth.exceptions.SessionExpiredException

/**
 * Maps Cognito/Amplify AuthException types to user-facing copy.
 * Matches on exception type rather than message text, since Cognito's
 * message strings aren't a stable contract.
 */
object AuthErrorMapper {

    fun toUserMessage(error: AuthException): String = when (error) {
        is UsernameExistsException ->
            "An account with this email already exists. Try signing in instead."

        is InvalidPasswordException ->
            "Password doesn't meet the requirements: at least 8 characters, with an uppercase letter, a lowercase letter, a number, and a symbol."

        is InvalidParameterException -> when {
            error.message?.contains("birthdate", ignoreCase = true) == true ->
                "Please enter a valid birthdate in MM/DD/YYYY format."
            error.message?.contains("email", ignoreCase = true) == true ->
                "Please enter a valid email address."
            else ->
                "One of the fields you entered isn't valid. Please check your details and try again."
        }

        is UserNotFoundException ->
            "No account found with this email."

        is UserNotConfirmedException ->
            "Please verify your email before signing in."

        is NotAuthorizedException ->
            "Incorrect email or password."

        is CodeMismatchException ->
            "That verification code is incorrect. Please try again."

        is CodeExpiredException ->
            "That verification code has expired. Request a new one."

        is PasswordResetRequiredException ->
            "Your password needs to be reset before you can sign in. Use \"Forgot Password\" to continue."

        is LimitExceededException, is TooManyRequestsException, is FailedAttemptsLimitExceededException ->
            "Too many attempts. Please wait a few minutes and try again."

        is SessionExpiredException ->
            "Your session has expired. Please sign in again."

        else ->
            error.message?.takeIf { it.isNotBlank() } ?: "Something went wrong. Please try again."
    }
}
