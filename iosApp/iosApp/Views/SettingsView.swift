import SwiftUI

struct SettingsView: View {
    let onBackClick: () -> Void
    let onLogoutClick: () -> Void
    
    var body: some View {
        ZStack {
            Color(red: 0.97, green: 0.97, blue: 0.98)
                .ignoresSafeArea()
            
            VStack(spacing: 0) {
                // Top Navigation Bar
                HStack {
                    Button(action: onBackClick) {
                        HStack(spacing: 8) {
                            Image(systemName: "chevron.left")
                                .font(.system(size: 18, weight: .semibold))
                            Text("Back")
                                .font(.system(size: 17, weight: .regular))
                        }
                        .foregroundColor(.white)
                    }
                    .padding(.leading, 16)
                    
                    Spacer()
                    
                    Text("Settings")
                        .font(.system(size: 20, weight: .bold))
                        .foregroundColor(.white)
                    
                    Spacer()
                    
                    // Invisible button for balance
                    Button(action: {}) {
                        Image(systemName: "chevron.left")
                            .font(.system(size: 18))
                    }
                    .opacity(0)
                    .padding(.trailing, 16)
                }
                .padding(.vertical, 16)
                .background(Color(red: 0.118, green: 0.227, blue: 0.541))
                
                // Settings Content
                ScrollView {
                    VStack(spacing: 16) {
                        // Account Section
                        VStack(alignment: .leading, spacing: 12) {
                            Text("Account")
                                .font(.system(size: 18, weight: .bold))
                                .foregroundColor(Color(red: 0.118, green: 0.227, blue: 0.541))
                                .padding(.horizontal, 20)
                                .padding(.top, 20)
                            
                            VStack(spacing: 0) {
                                Text("Manage your account settings")
                                    .font(.system(size: 14))
                                    .foregroundColor(.gray)
                                    .frame(maxWidth: .infinity, alignment: .leading)
                                    .padding(.horizontal, 20)
                                    .padding(.bottom, 12)
                                
                                Text("Coming soon...")
                                    .font(.system(size: 14))
                                    .foregroundColor(.gray)
                                    .frame(maxWidth: .infinity, alignment: .leading)
                                    .padding(.horizontal, 20)
                                    .padding(.bottom, 20)
                            }
                        }
                        .background(Color.white)
                        .cornerRadius(12)
                        .shadow(color: Color.black.opacity(0.1), radius: 4, x: 0, y: 2)
                        .padding(.horizontal, 16)
                        
                        // Account Actions Section
                        VStack(alignment: .leading, spacing: 12) {
                            Text("Account Actions")
                                .font(.system(size: 18, weight: .bold))
                                .foregroundColor(Color(red: 0.118, green: 0.227, blue: 0.541))
                                .padding(.horizontal, 20)
                                .padding(.top, 20)
                            
                            VStack(spacing: 0) {
                                Spacer()
                                    .frame(height: 8)
                                
                                // Logout option
                                Button(action: onLogoutClick) {
                                    VStack(alignment: .leading, spacing: 4) {
                                        Text("Logout")
                                            .font(.system(size: 16, weight: .medium))
                                            .foregroundColor(Color(red: 0.863, green: 0.149, blue: 0.149))
                                            .frame(maxWidth: .infinity, alignment: .leading)
                                        
                                        Text("Sign out of your account")
                                            .font(.system(size: 12))
                                            .foregroundColor(.gray)
                                            .frame(maxWidth: .infinity, alignment: .leading)
                                    }
                                    .padding(.horizontal, 20)
                                    .padding(.vertical, 12)
                                }
                                
                                Spacer()
                                    .frame(height: 8)
                            }
                        }
                        .background(Color.white)
                        .cornerRadius(12)
                        .shadow(color: Color.black.opacity(0.1), radius: 4, x: 0, y: 2)
                        .padding(.horizontal, 16)
                        
                        // About Section
                        VStack(alignment: .leading, spacing: 12) {
                            Text("About")
                                .font(.system(size: 18, weight: .bold))
                                .foregroundColor(Color(red: 0.118, green: 0.227, blue: 0.541))
                                .padding(.horizontal, 20)
                                .padding(.top, 20)
                            
                            VStack(spacing: 4) {
                                Text("mAIScreen v1.0.0")
                                    .font(.system(size: 14))
                                    .foregroundColor(.gray)
                                    .frame(maxWidth: .infinity, alignment: .leading)
                                    .padding(.horizontal, 20)
                                
                                Text("Streaming platform for content creators")
                                    .font(.system(size: 14))
                                    .foregroundColor(.gray)
                                    .frame(maxWidth: .infinity, alignment: .leading)
                                    .padding(.horizontal, 20)
                                    .padding(.bottom, 20)
                            }
                        }
                        .background(Color.white)
                        .cornerRadius(12)
                        .shadow(color: Color.black.opacity(0.1), radius: 4, x: 0, y: 2)
                        .padding(.horizontal, 16)
                        
                        Spacer()
                            .frame(height: 32)
                    }
                    .padding(.top, 16)
                }
            }
        }
        .navigationBarHidden(true)
    }
}

#Preview {
    SettingsView(
        onBackClick: {
            print("Back clicked")
        },
        onLogoutClick: {
            print("Logout clicked")
        }
    )
}

