import Amplify
import AWSCognitoAuthPlugin

/// Maps Cognito/Amplify AuthError values to user-facing copy.
/// Matches on the underlying AWSCognitoAuthError case rather than message text,
/// since Cognito's message strings aren't a stable contract.
enum AuthErrorMapper {
    static func toUserMessage(_ error: AuthError) -> String {
        if let cognitoError = error.underlyingError as? AWSCognitoAuthError {
            switch cognitoError {
            case .usernameExists:
                return "An account with this email already exists. Try signing in instead."
            case .invalidPassword:
                return "Password doesn't meet the requirements: at least 8 characters, with an uppercase letter, a lowercase letter, a number, and a symbol."
            case .invalidParameter(let message):
                if message.lowercased().contains("birthdate") {
                    return "Please enter a valid birthdate in MM/DD/YYYY format."
                } else if message.lowercased().contains("email") {
                    return "Please enter a valid email address."
                }
                return "One of the fields you entered isn't valid. Please check your details and try again."
            case .userNotFound:
                return "No account found with this email."
            case .userNotConfirmed:
                return "Please verify your email before signing in."
            case .codeMismatch:
                return "That verification code is incorrect. Please try again."
            case .codeExpired:
                return "That verification code has expired. Request a new one."
            case .passwordResetRequired:
                return "Your password needs to be reset before you can sign in. Use \"Forgot Password\" to continue."
            case .limitExceeded:
                return "Too many attempts. Please wait a few minutes and try again."
            default:
                break
            }
        }

        switch error {
        case .notAuthorized:
            return "Incorrect email or password."
        case .sessionExpired:
            return "Your session has expired. Please sign in again."
        default:
            return error.errorDescription
        }
    }
}
