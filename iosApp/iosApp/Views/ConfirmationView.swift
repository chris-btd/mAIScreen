import SwiftUI

struct ConfirmationView: View {
    let email: String
    let onConfirmClick: (String) -> Void
    let onResendClick: () -> Void
    let onBackClick: () -> Void

    @State private var confirmationCode: String = ""
    @State private var isLoading: Bool = false
    var errorMessage: String? = nil
    var onErrorDismiss: () -> Void = {}

    var body: some View {
        ZStack {
            Color.black.ignoresSafeArea()

            VStack(spacing: 16) {
                Spacer()

                Text("Verify Your Email")
                    .font(.system(size: 28, weight: .bold))
                    .foregroundColor(.white)
                    .multilineTextAlignment(.center)

                Text("We've sent a verification code to:")
                    .font(.system(size: 16))
                    .foregroundColor(.gray)
                    .multilineTextAlignment(.center)

                Text(email)
                    .font(.system(size: 16, weight: .medium))
                    .foregroundColor(.white)
                    .multilineTextAlignment(.center)

                Spacer().frame(height: 16)

                TextField("", text: $confirmationCode)
                    .keyboardType(.numberPad)
                    .foregroundColor(.white)
                    .padding()
                    .background(Color.clear)
                    .overlay(
                        RoundedRectangle(cornerRadius: 8)
                            .stroke(Color.gray, lineWidth: 1)
                    )
                    .placeholder(when: confirmationCode.isEmpty) {
                        Text("Enter 6-digit code").foregroundColor(.gray).padding(.leading, 16)
                    }

                Button(action: {
                    guard !confirmationCode.isEmpty else { return }
                    isLoading = true
                    onConfirmClick(confirmationCode)
                }) {
                    HStack {
                        if isLoading {
                            ProgressView().progressViewStyle(CircularProgressViewStyle(tint: .white))
                        } else {
                            Text("Verify Email")
                                .font(.system(size: 16, weight: .medium))
                        }
                    }
                    .frame(maxWidth: .infinity)
                    .frame(height: 56)
                    .background(Color.red)
                    .foregroundColor(.white)
                    .cornerRadius(8)
                }
                .disabled(confirmationCode.isEmpty || isLoading)

                Button(action: onResendClick) {
                    Text("Didn't receive the code? Resend")
                        .foregroundColor(.gray)
                        .font(.system(size: 14))
                }
                .disabled(isLoading)

                Spacer().frame(height: 16)

                Button(action: onBackClick) {
                    Text("← Back to Sign Up")
                        .foregroundColor(.white)
                        .font(.system(size: 16))
                }
                .disabled(isLoading)

                Spacer()
            }
            .padding(24)
        }
        .onChange(of: errorMessage) { newValue in
            if newValue != nil { isLoading = false }
        }
        .alert(
            "Verification Failed",
            isPresented: Binding(get: { errorMessage != nil }, set: { _ in onErrorDismiss() }),
            actions: { Button("OK", action: onErrorDismiss) },
            message: { Text(errorMessage ?? "") }
        )
    }
}

private extension View {
    func placeholder<Content: View>(
        when shouldShow: Bool,
        alignment: Alignment = .leading,
        @ViewBuilder placeholder: () -> Content
    ) -> some View {
        ZStack(alignment: alignment) {
            placeholder().opacity(shouldShow ? 1 : 0)
            self
        }
    }
}

#Preview {
    ConfirmationView(
        email: "test@example.com",
        onConfirmClick: { code in print("Confirm: \(code)") },
        onResendClick: { print("Resend tapped") },
        onBackClick: { print("Back tapped") }
    )
}
