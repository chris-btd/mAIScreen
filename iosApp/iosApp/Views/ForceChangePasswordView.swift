import SwiftUI

/// Shown when Cognito reports that an account must set a new password before
/// finishing sign-in (e.g. an admin-created user still in FORCE_CHANGE_PASSWORD status).
struct ForceChangePasswordView: View {
    let email: String
    let onSubmit: (_ newPassword: String, _ firstName: String, _ lastName: String) -> Void

    @State private var firstName: String = ""
    @State private var lastName: String = ""
    @State private var newPassword: String = ""
    @State private var confirmPassword: String = ""
    @State private var isPasswordVisible: Bool = false
    @State private var isConfirmPasswordVisible: Bool = false
    @State private var isLoading: Bool = false
    var errorMessage: String? = nil
    var onErrorDismiss: () -> Void = {}

    let navyBlue = Color(red: 0.106, green: 0.161, blue: 0.318)
    let black = Color.black
    let starWhite = Color.white

    let stars: [(x: Double, y: Double, size: Double)] = {
        (0..<80).map { _ in
            (x: Double.random(in: 0...1), y: Double.random(in: 0...0.5), size: Double.random(in: 1.5...4))
        }
    }()

    private var passwordsMatch: Bool {
        newPassword == confirmPassword || confirmPassword.isEmpty
    }

    private var isFormValid: Bool {
        newPassword.count >= 8 && newPassword == confirmPassword
    }

    var body: some View {
        ZStack {
            LinearGradient(gradient: Gradient(colors: [black, navyBlue]), startPoint: .top, endPoint: .bottom)
                .ignoresSafeArea()

            ForEach(0..<stars.count, id: \.self) { index in
                let star = stars[index]
                Circle()
                    .fill(starWhite.opacity(Double.random(in: 0.3...0.8)))
                    .frame(width: star.size, height: star.size)
                    .position(x: star.x * UIScreen.main.bounds.width, y: star.y * UIScreen.main.bounds.height)
            }

            ScrollView {
                VStack(spacing: 20) {
                    Spacer().frame(height: 40)

                    Text("Set a New Password")
                        .font(.system(size: 30, weight: .bold))
                        .foregroundColor(.white)
                        .multilineTextAlignment(.center)

                    Text("Your account (\(email)) needs a new password before you can continue.")
                        .font(.system(size: 15))
                        .foregroundColor(.white.opacity(0.8))
                        .multilineTextAlignment(.center)

                    Spacer().frame(height: 8)

                    TextField("First Name (optional)", text: $firstName)
                        .foregroundColor(.white)
                        .padding()
                        .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.white.opacity(0.5), lineWidth: 1))

                    TextField("Last Name (optional)", text: $lastName)
                        .foregroundColor(.white)
                        .padding()
                        .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.white.opacity(0.5), lineWidth: 1))

                    VStack(alignment: .leading, spacing: 8) {
                        HStack {
                            if isPasswordVisible {
                                TextField("New Password", text: $newPassword).foregroundColor(.white)
                            } else {
                                SecureField("New Password", text: $newPassword).foregroundColor(.white)
                            }
                            Button(action: { isPasswordVisible.toggle() }) {
                                Image(systemName: isPasswordVisible ? "eye" : "eye.slash")
                                    .foregroundColor(.white.opacity(0.7))
                            }
                        }
                        .padding()
                        .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.white.opacity(0.5), lineWidth: 1))

                        if !newPassword.isEmpty && newPassword.count < 8 {
                            Text("Password must be at least 8 characters")
                                .foregroundColor(.red)
                                .font(.system(size: 12))
                        }
                    }

                    VStack(alignment: .leading, spacing: 8) {
                        HStack {
                            if isConfirmPasswordVisible {
                                TextField("Confirm New Password", text: $confirmPassword).foregroundColor(.white)
                            } else {
                                SecureField("Confirm New Password", text: $confirmPassword).foregroundColor(.white)
                            }
                            Button(action: { isConfirmPasswordVisible.toggle() }) {
                                Image(systemName: isConfirmPasswordVisible ? "eye" : "eye.slash")
                                    .foregroundColor(.white.opacity(0.7))
                            }
                        }
                        .padding()
                        .overlay(
                            RoundedRectangle(cornerRadius: 12)
                                .stroke(passwordsMatch ? Color.white.opacity(0.5) : Color.red.opacity(0.7), lineWidth: 1)
                        )

                        if !passwordsMatch {
                            Text("Passwords do not match")
                                .foregroundColor(.red)
                                .font(.system(size: 12))
                        }
                    }

                    Spacer().frame(height: 8)

                    Button(action: {
                        guard isFormValid else { return }
                        isLoading = true
                        onSubmit(newPassword, firstName, lastName)
                    }) {
                        HStack {
                            if isLoading {
                                ProgressView().progressViewStyle(CircularProgressViewStyle(tint: navyBlue))
                            } else {
                                Text("Set Password & Sign In")
                                    .font(.system(size: 16, weight: .medium))
                            }
                        }
                        .frame(maxWidth: .infinity)
                        .frame(height: 56)
                        .background((!isLoading && isFormValid) ? Color.white : Color.white.opacity(0.5))
                        .foregroundColor(navyBlue)
                        .cornerRadius(12)
                    }
                    .disabled(isLoading || !isFormValid)

                    Spacer()
                }
                .padding(.horizontal, 24)
            }
        }
        .onChange(of: errorMessage) { newValue in
            if newValue != nil { isLoading = false }
        }
        .alert(
            "Couldn't Set Password",
            isPresented: Binding(get: { errorMessage != nil }, set: { _ in onErrorDismiss() }),
            actions: { Button("OK", action: onErrorDismiss) },
            message: { Text(errorMessage ?? "") }
        )
    }
}

#Preview {
    ForceChangePasswordView(
        email: "test@example.com",
        onSubmit: { newPassword, firstName, lastName in
            print("Submit: \(newPassword), \(firstName), \(lastName)")
        }
    )
}
