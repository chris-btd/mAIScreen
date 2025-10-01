import SwiftUI

struct LoginView: View {
    @State private var email: String = ""
    @State private var password: String = ""
    @State private var isPasswordVisible: Bool = false
    @State private var isLoading: Bool = false
    
    let onLoginClick: (String, String) -> Void
    let onSignUpClick: () -> Void
    
    // Define colors
    let navyBlue = Color(red: 0.106, green: 0.161, blue: 0.318) // #1B2951
    let black = Color.black
    let starWhite = Color.white
    
    // Generate random stars
    let stars: [(x: Double, y: Double, size: Double)] = {
        return (0..<80).map { _ in
            (
                x: Double.random(in: 0...1),
                y: Double.random(in: 0...0.5), // Stars only in top 50%
                size: Double.random(in: 1.5...4)
            )
        }
    }()
    
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
            
            ScrollView {
                VStack(spacing: 24) {
                    Spacer()
                        .frame(height: 60)
                    
                    // App Title
                    VStack(spacing: 8) {
                        Text("mAIScreen")
                            .font(.system(size: 36, weight: .bold))
                            .foregroundColor(.white)
                        
                        Text("Welcome back!")
                            .font(.system(size: 18))
                            .foregroundColor(.white.opacity(0.8))
                    }
                    
                    Spacer()
                        .frame(height: 32)
                    
                    // Email Input
                    VStack(alignment: .leading, spacing: 8) {
                        HStack {
                            Image(systemName: "envelope")
                                .foregroundColor(.white.opacity(0.7))
                                .frame(width: 20)
                            
                            TextField("Email", text: $email)
                                .keyboardType(.emailAddress)
                                .autocapitalization(.none)
                                .disableAutocorrection(true)
                                .foregroundColor(.white)
                        }
                        .padding()
                        .background(Color.clear)
                        .cornerRadius(12)
                        .overlay(
                            RoundedRectangle(cornerRadius: 12)
                                .stroke(Color.white.opacity(0.5), lineWidth: 1)
                        )
                    }
                    
                    // Password Input
                    VStack(alignment: .leading, spacing: 8) {
                        HStack {
                            Image(systemName: "lock")
                                .foregroundColor(.white.opacity(0.7))
                                .frame(width: 20)
                            
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
                    }
                    
                    Spacer()
                        .frame(height: 24)
                    
                    // Login Button
                    Button(action: {
                        if !email.isEmpty && !password.isEmpty {
                            isLoading = true
                            onLoginClick(email, password)
                        }
                    }) {
                        HStack {
                            if isLoading {
                                ProgressView()
                                    .scaleEffect(0.8)
                                    .progressViewStyle(CircularProgressViewStyle(tint: navyBlue))
                            } else {
                                Text("Login")
                                    .font(.system(size: 16, weight: .medium))
                            }
                        }
                        .frame(maxWidth: .infinity)
                        .frame(height: 56)
                        .background(
                            (!email.isEmpty && !password.isEmpty && !isLoading) ? 
                            Color.white : Color.white.opacity(0.5)
                        )
                        .foregroundColor(navyBlue)
                        .cornerRadius(12)
                    }
                    .disabled(email.isEmpty || password.isEmpty || isLoading)
                    
                    Spacer()
                        .frame(height: 16)
                    
                    // Divider with "OR"
                    HStack {
                        Rectangle()
                            .frame(height: 1)
                            .foregroundColor(Color.white.opacity(0.3))
                        
                        Text("OR")
                            .foregroundColor(.white.opacity(0.6))
                            .padding(.horizontal, 16)
                        
                        Rectangle()
                            .frame(height: 1)
                            .foregroundColor(Color.white.opacity(0.3))
                    }
                    
                    Spacer()
                        .frame(height: 16)
                    
                    // Sign Up Button
                    Button(action: onSignUpClick) {
                        Text("Create Account")
                            .font(.system(size: 16, weight: .medium))
                            .frame(maxWidth: .infinity)
                            .frame(height: 56)
                            .background(Color.clear)
                            .foregroundColor(.white)
                            .overlay(
                                RoundedRectangle(cornerRadius: 12)
                                    .stroke(Color.white, lineWidth: 1)
                            )
                    }
                    .disabled(isLoading)
                    
                    Spacer()
                        .frame(height: 24)
                    
                    // Forgot Password
                    Button(action: {
                        // TODO: Implement forgot password
                    }) {
                        Text("Forgot Password?")
                            .foregroundColor(.white.opacity(0.8))
                    }
                    .disabled(isLoading)
                    
                    Spacer()
                }
                .padding(.horizontal, 24)
            }
        }
    }
}

#Preview {
    LoginView(
        onLoginClick: { email, password in
            print("Login: \(email)")
        },
        onSignUpClick: {
            print("Sign up tapped")
        }
    )
}
