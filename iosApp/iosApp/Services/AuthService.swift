import Foundation
import Combine
import Amplify
import AWSCognitoAuthPlugin

/// Outcome of a sign-in attempt, mirroring the Cognito next-step cases
/// the app knows how to route (new-password-required, unconfirmed account).
enum SignInOutcome {
    case success
    case needsNewPassword
    case needsConfirmation
    case failed
}

/// iOS Authentication Service
/// Wraps AWS Amplify Auth (Cognito User Pools) for sign-up, confirmation, and sign-in.
class AuthService: ObservableObject {
    static let shared = AuthService()

    @Published var isAuthenticated: Bool = false
    @Published var userEmail: String? = nil

    private static var isAmplifyConfigured = false

    private init() {
        configureAmplifyIfNeeded()
        checkAuthenticationStatus()
    }

    private func configureAmplifyIfNeeded() {
        guard !AuthService.isAmplifyConfigured else { return }
        do {
            try Amplify.add(plugin: AWSCognitoAuthPlugin())
            try Amplify.configure()
            AuthService.isAmplifyConfigured = true
            print("✅ iOS: Amplify configured")
        } catch {
            print("❌ iOS: Failed to configure Amplify: \(error)")
        }
    }

    // MARK: - Sign Up

    /// firstName, lastName, and birthdate are optional — omitted from the attribute
    /// list entirely when blank, rather than sent as empty strings.
    func signUp(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        birthdate: String,
        completion: @escaping (_ success: Bool, _ needsConfirmation: Bool, _ error: String?) -> Void
    ) {
        var attributes: [AuthUserAttribute] = []
        if !firstName.isEmpty { attributes.append(AuthUserAttribute(.givenName, value: firstName)) }
        if !lastName.isEmpty { attributes.append(AuthUserAttribute(.familyName, value: lastName)) }
        if let isoBirthdate = Self.toIsoBirthdate(birthdate) {
            attributes.append(AuthUserAttribute(.birthDate, value: isoBirthdate))
        }

        let options = AuthSignUpRequest.Options(userAttributes: attributes)

        Task {
            do {
                let result = try await Amplify.Auth.signUp(username: email, password: password, options: options)
                print("✅ iOS: Sign up succeeded for \(email), complete=\(result.isSignUpComplete)")
                await MainActor.run {
                    completion(true, !result.isSignUpComplete, nil)
                }
            } catch let authError as AuthError {
                print("❌ iOS: Sign up failed: \(authError)")
                await MainActor.run {
                    completion(false, false, AuthErrorMapper.toUserMessage(authError))
                }
            } catch {
                await MainActor.run {
                    completion(false, false, error.localizedDescription)
                }
            }
        }
    }

    // MARK: - Confirm Sign Up

    func confirmSignUp(
        username: String,
        confirmationCode: String,
        completion: @escaping (_ success: Bool, _ error: String?) -> Void
    ) {
        Task {
            do {
                _ = try await Amplify.Auth.confirmSignUp(for: username, confirmationCode: confirmationCode)
                print("✅ iOS: Confirm sign up succeeded for \(username)")
                await MainActor.run { completion(true, nil) }
            } catch let authError as AuthError {
                print("❌ iOS: Confirm sign up failed: \(authError)")
                await MainActor.run { completion(false, AuthErrorMapper.toUserMessage(authError)) }
            } catch {
                await MainActor.run { completion(false, error.localizedDescription) }
            }
        }
    }

    func resendConfirmationCode(username: String) {
        Task {
            do {
                _ = try await Amplify.Auth.resendSignUpCode(for: username)
                print("✅ iOS: Resent confirmation code to \(username)")
            } catch {
                print("❌ iOS: Failed to resend confirmation code: \(error)")
            }
        }
    }

    // MARK: - Sign In

    func signIn(
        email: String,
        password: String,
        completion: @escaping (_ outcome: SignInOutcome, _ error: String?) -> Void
    ) {
        Task {
            do {
                let result = try await Amplify.Auth.signIn(username: email, password: password)
                if result.isSignedIn {
                    print("✅ iOS: Sign in succeeded for \(email)")
                    await MainActor.run {
                        self.setAuthenticated(true, email: email)
                        completion(.success, nil)
                    }
                    return
                }

                switch result.nextStep {
                case .confirmSignInWithNewPassword:
                    print("🔐 iOS: Account requires a new password: \(email)")
                    await MainActor.run { completion(.needsNewPassword, nil) }
                case .confirmSignUp:
                    print("📧 iOS: Account not yet confirmed: \(email)")
                    await MainActor.run { completion(.needsConfirmation, nil) }
                default:
                    print("Additional steps required: \(result.nextStep)")
                    await MainActor.run {
                        completion(.failed, "Additional verification is required to sign in.")
                    }
                }
            } catch let authError as AuthError {
                print("❌ iOS: Sign in failed: \(authError)")
                await MainActor.run { completion(.failed, AuthErrorMapper.toUserMessage(authError)) }
            } catch {
                await MainActor.run { completion(.failed, error.localizedDescription) }
            }
        }
    }

    /// Complete a sign-in that Cognito flagged as requiring a new password
    /// (e.g. an admin-created user with FORCE_CHANGE_PASSWORD status).
    /// firstName/lastName are optional and only sent if the user pool still
    /// requires them as attributes.
    func confirmSignInNewPassword(
        newPassword: String,
        email: String,
        firstName: String = "",
        lastName: String = "",
        completion: @escaping (_ success: Bool, _ error: String?) -> Void
    ) {
        var attributes: [AuthUserAttribute] = []
        if !firstName.isEmpty { attributes.append(AuthUserAttribute(.givenName, value: firstName)) }
        if !lastName.isEmpty { attributes.append(AuthUserAttribute(.familyName, value: lastName)) }

        let options = AWSAuthConfirmSignInOptions(userAttributes: attributes)

        Task {
            do {
                let result = try await Amplify.Auth.confirmSignIn(challengeResponse: newPassword, options: options)
                if result.isSignedIn {
                    print("✅ iOS: Sign-in completed after password change")
                    await MainActor.run {
                        self.setAuthenticated(true, email: email)
                        completion(true, nil)
                    }
                } else {
                    print("Additional steps still required: \(result.nextStep)")
                    await MainActor.run {
                        completion(false, "Additional verification is required to finish signing in.")
                    }
                }
            } catch let authError as AuthError {
                print("❌ iOS: Failed to set new password: \(authError)")
                await MainActor.run { completion(false, AuthErrorMapper.toUserMessage(authError)) }
            } catch {
                await MainActor.run { completion(false, error.localizedDescription) }
            }
        }
    }

    // MARK: - Session

    func checkAuthenticationStatus() {
        Task {
            do {
                let session = try await Amplify.Auth.fetchAuthSession()
                if session.isSignedIn {
                    let user = try? await Amplify.Auth.getCurrentUser()
                    print("✅ iOS: User is authenticated - \(user?.username ?? "unknown")")
                    await MainActor.run { self.setAuthenticated(true, email: user?.username) }
                } else {
                    print("❌ iOS: User is not authenticated")
                    await MainActor.run { self.setAuthenticated(false) }
                }
            } catch {
                print("❌ iOS: Failed to fetch auth session: \(error)")
                await MainActor.run { self.setAuthenticated(false) }
            }
        }
    }

    /// Set authentication state (called after a successful native sign-in above,
    /// or during the startup session check).
    func setAuthenticated(_ authenticated: Bool, email: String? = nil) {
        isAuthenticated = authenticated
        userEmail = email
    }

    func logout() {
        Task {
            _ = await Amplify.Auth.signOut()
            print("🚪 iOS: Signed out")
            await MainActor.run {
                self.isAuthenticated = false
                self.userEmail = nil
            }
        }
    }

    /// Converts the app's MM/DD/YYYY input into the ISO 8601 (YYYY-MM-DD) format
    /// Cognito's standard `birthdate` attribute expects. Returns nil when blank
    /// or malformed, so callers can simply omit the attribute.
    private static func toIsoBirthdate(_ birthdate: String) -> String? {
        guard !birthdate.isEmpty else { return nil }
        let parts = birthdate.split(separator: "/", omittingEmptySubsequences: false)
        guard parts.count == 3,
              parts[0].count == 2, parts[1].count == 2, parts[2].count == 4 else {
            return nil
        }
        return "\(parts[2])-\(parts[0])-\(parts[1])"
    }
}
