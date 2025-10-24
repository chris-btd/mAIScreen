import SwiftUI

struct UploadTemplateView: View {
    let onBackClick: () -> Void
    let onNextClick: (String, String) -> Void
    
    @State private var selectedType: String = ""
    @State private var selectedSubType: String = ""
    @State private var showTypeDropdown = false
    
    let navyBlue = Color(red: 0.106, green: 0.161, blue: 0.318)
    let black = Color.black
    let starWhite = Color.white
    
    var body: some View {
        ZStack {
            // Background gradient with stars
            LinearGradient(
                gradient: Gradient(colors: [black, navyBlue]),
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()
            
            // Stars background
            GeometryReader { geometry in
                ForEach(0..<50, id: \.self) { _ in
                    Circle()
                        .fill(starWhite.opacity(Double.random(in: 0.1...0.4)))
                        .frame(width: CGFloat.random(in: 1...3))
                        .position(
                            x: CGFloat.random(in: 0...geometry.size.width),
                            y: CGFloat.random(in: 0...geometry.size.height)
                        )
                }
            }
            
            ScrollView {
                VStack(spacing: 24) {
                    // Top Navigation Bar
                    HStack {
                        Button(action: onBackClick) {
                            Text("←")
                                .font(.system(size: 24))
                                .foregroundColor(.white)
                        }
                        
                        Spacer()
                        
                        Text("Create Upload Template")
                            .font(.system(size: 20, weight: .bold))
                            .foregroundColor(.white)
                        
                        Spacer()
                        
                        // Placeholder for balance
                        Rectangle()
                            .fill(Color.clear)
                            .frame(width: 24, height: 24)
                    }
                    .padding(.horizontal, 24)
                    .padding(.vertical, 16)
                    
                    Spacer()
                        .frame(height: 32)
                    
                    // Main Content
                    VStack(spacing: 24) {
                        // Header Card
                        VStack(spacing: 12) {
                            Text("📤")
                                .font(.system(size: 48))
                            
                            Text("Create Upload Template")
                                .font(.system(size: 24, weight: .bold))
                                .foregroundColor(.white)
                                .multilineTextAlignment(.center)
                            
                            Text("Choose the type of content you want to upload and select the appropriate category.")
                                .font(.system(size: 16))
                                .foregroundColor(.white.opacity(0.8))
                                .multilineTextAlignment(.center)
                        }
                        .padding(24)
                        .background(
                            RoundedRectangle(cornerRadius: 16)
                                .fill(Color.white.opacity(0.1))
                                .shadow(color: .black.opacity(0.3), radius: 8, x: 0, y: 4)
                        )
                        
                        // Type Selection Card
                        VStack(alignment: .leading, spacing: 16) {
                            Text("Content Type")
                                .font(.system(size: 18, weight: .bold))
                                .foregroundColor(navyBlue)
                            
                            // Type Dropdown
                            VStack {
                                Button(action: { showTypeDropdown.toggle() }) {
                                    HStack {
                                        Text(selectedType.isEmpty ? "Select Type" : selectedType)
                                            .foregroundColor(selectedType.isEmpty ? .gray : navyBlue)
                                            .frame(maxWidth: .infinity, alignment: .leading)
                                        
                                        Text("▼")
                                            .foregroundColor(navyBlue)
                                    }
                                    .padding()
                                    .background(
                                        RoundedRectangle(cornerRadius: 8)
                                            .stroke(Color.gray.opacity(0.3), lineWidth: 1)
                                    )
                                }
                                
                                if showTypeDropdown {
                                    VStack(spacing: 0) {
                                        ForEach(["Audio", "Video"], id: \.self) { type in
                                            Button(action: {
                                                selectedType = type
                                                selectedSubType = "" // Reset sub-type when type changes
                                                showTypeDropdown = false
                                            }) {
                                                HStack {
                                                    Text(type)
                                                        .foregroundColor(navyBlue)
                                                        .frame(maxWidth: .infinity, alignment: .leading)
                                                    
                                                    if selectedType == type {
                                                        Text("✓")
                                                            .foregroundColor(navyBlue)
                                                    }
                                                }
                                                .padding()
                                                .background(
                                                    RoundedRectangle(cornerRadius: 8)
                                                        .fill(selectedType == type ? navyBlue.opacity(0.1) : Color.clear)
                                                )
                                            }
                                        }
                                    }
                                    .background(Color.white)
                                    .cornerRadius(8)
                                    .shadow(color: .black.opacity(0.1), radius: 4, x: 0, y: 2)
                                }
                            }
                        }
                        .padding(20)
                        .background(Color.white)
                        .cornerRadius(12)
                        .shadow(color: .black.opacity(0.1), radius: 4, x: 0, y: 2)
                        
                        // Sub-Type Selection Card (conditional)
                        if !selectedType.isEmpty {
                            VStack(alignment: .leading, spacing: 16) {
                                Text(selectedType == "Audio" ? "Audio Category" : "Video Category")
                                    .font(.system(size: 18, weight: .bold))
                                    .foregroundColor(navyBlue)
                                
                                // Sub-type options based on selected type
                                let subTypes = selectedType == "Audio" ? 
                                    ["Podcast", "AudioBook"] : 
                                    ["Series", "Movie", "Short Film", "Podcast"]
                                
                                VStack(spacing: 8) {
                                    ForEach(subTypes, id: \.self) { subType in
                                        Button(action: { selectedSubType = subType }) {
                                            HStack {
                                                Image(systemName: selectedSubType == subType ? "circle.fill" : "circle")
                                                    .foregroundColor(selectedSubType == subType ? navyBlue : .gray)
                                                
                                                Text(subType)
                                                    .foregroundColor(navyBlue)
                                                    .frame(maxWidth: .infinity, alignment: .leading)
                                            }
                                            .padding(.vertical, 8)
                                        }
                                    }
                                }
                            }
                            .padding(20)
                            .background(Color.white)
                            .cornerRadius(12)
                            .shadow(color: .black.opacity(0.1), radius: 4, x: 0, y: 2)
                            .transition(.asymmetric(
                                insertion: .move(edge: .top).combined(with: .opacity),
                                removal: .move(edge: .top).combined(with: .opacity)
                            ))
                        }
                        
                        Spacer()
                            .frame(height: 32)
                        
                        // Next Button
                        Button(action: {
                            if !selectedType.isEmpty && !selectedSubType.isEmpty {
                                onNextClick(selectedType, selectedSubType)
                            }
                        }) {
                            Text("Next")
                                .font(.system(size: 18, weight: .bold))
                                .foregroundColor(.white)
                                .frame(maxWidth: .infinity)
                                .frame(height: 56)
                                .background(
                                    RoundedRectangle(cornerRadius: 12)
                                        .fill(selectedType.isEmpty || selectedSubType.isEmpty ? 
                                              Color.gray.opacity(0.3) : Color(red: 0.388, green: 0.424, blue: 0.945))
                                )
                        }
                        .disabled(selectedType.isEmpty || selectedSubType.isEmpty)
                        
                        // Back Button
                        Button(action: onBackClick) {
                            Text("Cancel")
                                .font(.system(size: 16))
                                .foregroundColor(.white.opacity(0.7))
                        }
                    }
                    .padding(.horizontal, 24)
                    
                    // Bottom padding
                    Spacer()
                        .frame(height: 32)
                }
            }
        }
        .navigationBarHidden(true)
    }
}

#Preview {
    UploadTemplateView(
        onBackClick: { print("Back clicked") },
        onNextClick: { type, subType in
            print("Next clicked: \(type) - \(subType)")
        }
    )
}
