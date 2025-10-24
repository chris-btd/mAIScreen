import SwiftUI

struct SignUpView: View {
    @State private var firstName: String = ""
    @State private var lastName: String = ""
    @State private var email: String = ""
    @State private var confirmEmail: String = ""
    @State private var password: String = ""
    @State private var confirmPassword: String = ""
    @State private var isPasswordVisible: Bool = false
    @State private var isConfirmPasswordVisible: Bool = false
    @State private var isLoading: Bool = false
    
    let onSignUpClick: (String, String, String, String) -> Void
    let onBackToLoginClick: () -> Void
    
    // Define colors
    let navyBlue = Color(red: 0.106, green: 0.161, blue: 0.318) // #1B2951
    let black = Color.black
    let starWhite = Color.white
    
    // Generate random stars
    let stars: [(x: Double, y: Double, size: Double)] = {
        return (0..<80).map { _ in
            (
                x: Double.random(in: 0...1),
                y: Double.random(in: 0...0.4), // Stars only in top 40%
                size: Double.random(in: 1.5...4)
            )
        }
    }()
    
    // Form validation
    private var isFormValid: Bool {
        !firstName.isEmpty &&
        !lastName.isEmpty &&
        !email.isEmpty &&
        !confirmEmail.isEmpty &&
        !password.isEmpty &&
        !confirmPassword.isEmpty &&
        email == confirmEmail &&
        password == confirmPassword &&
        password.count >= 6
    }
    
    private var emailsMatch: Bool {
        email == confirmEmail || confirmEmail.isEmpty
    }
    
    private var passwordsMatch: Bool {
        password == confirmPassword || confirmPassword.isEmpty
    }
    
    var body: some View {
        ZStack {
            // Background gradient
            LinearGradient(
                gradient: Gradient(colors: [black, navyBlue]),
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()
            
            // Stars
            ForEach(0..<stars.count, id: \.self) { index in
                let star = stars[index]
                Circle()
                    .fill(starWhite.opacity(Double.random(in: 0.3...0.8)))
                    .frame(width: star.size, height: star.size)
                    .position(
                        x: star.x * UIScreen.main.bounds.width,
                        y: star.y * UIScreen.main.bounds.height
                    )
            }
            
            VStack(spacing: 0) {
                // Back Button
                HStack {
                    Button(action: onBackToLoginClick) {
                        HStack(spacing: 8) {
                            Image(systemName: "chevron.left")
                                .font(.system(size: 18, weight: .semibold))
                            Text("Back")
                                .font(.system(size: 17, weight: .regular))
                        }
                        .foregroundColor(.white)
                    }
                    .disabled(isLoading)
                    .padding(.leading, 16)
                    .padding(.top, 16)
                    
                    Spacer()
                }
                
                ScrollView {
                    VStack(spacing: 20) {
                        Spacer()
                            .frame(height: 20)
                        
                        // App Title
                        VStack(spacing: 8) {
                            Text("Create Account")
                                .font(.system(size: 32, weight: .bold))
                                .foregroundColor(.white)
                            
                            Text("Join mAIScreen today")
                                .font(.system(size: 16))
                                .foregroundColor(.white.opacity(0.8))
                        }
                    
                    Spacer()
                        .frame(height: 20)
                    
                    // First Name Input
                    VStack(alignment: .leading, spacing: 8) {
                        TextField("First Name", text: $firstName)
                            .foregroundColor(.white)
                            .padding()
                            .background(Color.clear)
                            .cornerRadius(12)
                            .overlay(
                                RoundedRectangle(cornerRadius: 12)
                                    .stroke(Color.white.opacity(0.5), lineWidth: 1)
                            )
                    }
                    
                    // Last Name Input
                    VStack(alignment: .leading, spacing: 8) {
                        TextField("Last Name", text: $lastName)
                            .foregroundColor(.white)
                            .padding()
                            .background(Color.clear)
                            .cornerRadius(12)
                            .overlay(
                                RoundedRectangle(cornerRadius: 12)
                                    .stroke(Color.white.opacity(0.5), lineWidth: 1)
                            )
                    }
                    
                    // Email Input
                    VStack(alignment: .leading, spacing: 8) {
                        TextField("Email", text: $email)
                            .keyboardType(.emailAddress)
                            .autocapitalization(.none)
                            .disableAutocorrection(true)
                            .foregroundColor(.white)
                            .padding()
                            .background(Color.clear)
                            .cornerRadius(12)
                            .overlay(
                                RoundedRectangle(cornerRadius: 12)
                                    .stroke(Color.white.opacity(0.5), lineWidth: 1)
                            )
                    }
                    
                    // Confirm Email Input
                    VStack(alignment: .leading, spacing: 8) {
                        TextField("Confirm Email", text: $confirmEmail)
                            .keyboardType(.emailAddress)
                            .autocapitalization(.none)
                            .disableAutocorrection(true)
                            .foregroundColor(.white)
                            .padding()
                            .background(Color.clear)
                            .cornerRadius(12)
                            .overlay(
                                RoundedRectangle(cornerRadius: 12)
                                    .stroke(emailsMatch ? Color.white.opacity(0.5) : Color.red.opacity(0.7), lineWidth: 1)
                            )
                        
                        if !emailsMatch {
                            Text("Emails do not match")
                                .foregroundColor(.red)
                                .font(.system(size: 12))
                        }
                    }
                    
                    // Password Input
                    VStack(alignment: .leading, spacing: 8) {
                        HStack {
                            if isPasswordVisible {
                                TextField("Password", text: $password)
                                    .foregroundColor(.white)
                            } else {
                                SecureField("Password", text: $password)
                                    .foregroundColor(.white)
                            }
                            
                            Button(action: {
                                isPasswordVisible.toggle()
                            }) {
                                Image(systemName: isPasswordVisible ? "eye" : "eye.slash")
                                    .foregroundColor(.white.opacity(0.7))
                            }
                        }
                        .padding()
                        .background(Color.clear)
                        .cornerRadius(12)
                        .overlay(
                            RoundedRectangle(cornerRadius: 12)
                                .stroke(Color.white.opacity(0.5), lineWidth: 1)
                        )
                        
                        if !password.isEmpty && password.count < 6 {
                            Text("Password must be at least 6 characters")
                                .foregroundColor(.red)
                                .font(.system(size: 12))
                        }
                    }
                    
                    // Confirm Password Input
                    VStack(alignment: .leading, spacing: 8) {
                        HStack {
                            if isConfirmPasswordVisible {
                                TextField("Confirm Password", text: $confirmPassword)
                                    .foregroundColor(.white)
                            } else {
                                SecureField("Confirm Password", text: $confirmPassword)
                                    .foregroundColor(.white)
                            }
                            
                            Button(action: {
                                isConfirmPasswordVisible.toggle()
                            }) {
                                Image(systemName: isConfirmPasswordVisible ? "eye" : "eye.slash")
                                    .foregroundColor(.white.opacity(0.7))
                            }
                        }
                        .padding()
                        .background(Color.clear)
                        .cornerRadius(12)
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
                    
                    Spacer()
                        .frame(height: 20)
                    
                    // Sign Up Button
                    Button(action: {
                        if isFormValid {
                            isLoading = true
                            onSignUpClick(firstName, lastName, email, password)
                        }
                    }) {
                        HStack {
                            if isLoading {
                                ProgressView()
                                    .scaleEffect(0.8)
                                    .progressViewStyle(CircularProgressViewStyle(tint: navyBlue))
                            } else {
                                Text("Create Account")
                                    .font(.system(size: 16, weight: .medium))
                            }
                        }
                        .frame(maxWidth: .infinity)
                        .frame(height: 56)
                        .background(
                            (!isLoading && isFormValid) ? 
                            Color.white : Color.white.opacity(0.5)
                        )
                        .foregroundColor(navyBlue)
                        .cornerRadius(12)
                    }
                    .disabled(isLoading || !isFormValid)
                    
                    Spacer()
                        .frame(height: 16)
                    
                    // Back to Login
                    Button(action: onBackToLoginClick) {
                        Text("Already have an account? Sign In")
                            .foregroundColor(.white.opacity(0.8))
                    }
                    .disabled(isLoading)
                    
                        Spacer()
                            .frame(height: 20)
                    }
                    .padding(.horizontal, 24)
                }
            }
        }
    }
}

#Preview {
    SignUpView(
        onSignUpClick: { firstName, lastName, email, password in
            print("Sign up: \(firstName) \(lastName), \(email)")
        },
        onBackToLoginClick: {
            print("Back to login tapped")
        }
    )
}
