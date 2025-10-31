import SwiftUI

struct MediaSubmissionView: View {
    let contentType: String // "Audio" or "Video"
    let subType: String // "Movie", "Series", "Short Film", "Podcast", "AudioBook"
    let onBackClick: () -> Void
    let onSubmitClick: ([String: String]) -> Void
    
    @State private var title: String = ""
    @State private var description: String = ""
    @State private var genre: String = ""
    @State private var duration: String = ""
    @State private var year: String = ""
    @State private var director: String = ""
    @State private var cast: String = ""
    @State private var episodeCount: String = ""
    @State private var host: String = ""
    @State private var author: String = ""
    
    let navyBlue = Color(red: 0.106, green: 0.161, blue: 0.318)
    let black = Color.black
    let starWhite = Color.white
    
    // Get screen title based on sub-type
    var screenTitle: String {
        switch subType {
        case "Movie": return "Movie Submission"
        case "Series": return "Series Submission"
        case "Short Film": return "Short Film Submission"
        case "Podcast": return "Podcast Submission"
        case "AudioBook": return "AudioBook Submission"
        default: return "Media Submission"
        }
    }
    
    var icon: String {
        switch subType {
        case "Movie": return "🎬"
        case "Series": return "📺"
        case "Short Film": return "🎥"
        case "Podcast": return "🎙️"
        case "AudioBook": return "📚"
        default: return "📤"
        }
    }
    
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
                        
                        Text(screenTitle)
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
                    VStack(spacing: 16) {
                        // Header Card
                        VStack(spacing: 8) {
                            Text(icon)
                                .font(.system(size: 48))
                            
                            Text(screenTitle)
                                .font(.system(size: 24, weight: .bold))
                                .foregroundColor(.white)
                        }
                        .padding(24)
                        .frame(maxWidth: .infinity)
                        .background(
                            RoundedRectangle(cornerRadius: 16)
                                .fill(Color.white.opacity(0.1))
                                .shadow(color: .black.opacity(0.3), radius: 8, x: 0, y: 4)
                        )
                        
                        // Common Fields
                        VStack(alignment: .leading, spacing: 4) {
                            Text("Title *")
                                .font(.system(size: 14, weight: .medium))
                                .foregroundColor(.white.opacity(0.8))
                                .padding(.leading, 4)
                            
                            TextField("Enter title", text: $title)
                                .textFieldStyle(RoundedTextFieldStyle())
                        }
                        
                        VStack(alignment: .leading, spacing: 4) {
                            Text("Description *")
                                .font(.system(size: 14, weight: .medium))
                                .foregroundColor(.white.opacity(0.8))
                                .padding(.leading, 4)
                            
                            TextEditor(text: $description)
                                .frame(height: 120)
                                .cornerRadius(12)
                                .overlay(
                                    RoundedRectangle(cornerRadius: 12)
                                        .stroke(Color.gray.opacity(0.5), lineWidth: 1)
                                )
                        }
                        
                        // Contextual Fields based on sub-type
                        if subType == "Movie" || subType == "Short Film" {
                            VStack(alignment: .leading, spacing: 4) {
                                Text("Duration (minutes)")
                                    .font(.system(size: 14, weight: .medium))
                                    .foregroundColor(.white.opacity(0.8))
                                    .padding(.leading, 4)
                                TextField("e.g., 120", text: $duration)
                                    .keyboardType(.numberPad)
                                    .textFieldStyle(RoundedTextFieldStyle())
                            }
                        
                        if subType == "Movie" || subType == "Short Film" {
                            VStack(alignment: .leading, spacing: 4) {
                                Text("Release Year")
                                    .font(.system(size: 14, weight: .medium))
                                    .foregroundColor(.white.opacity(0.8))
                                    .padding(.leading, 4)
                                TextField("e.g., 2024", text: $year)
                                    .keyboardType(.numberPad)
                                    .textFieldStyle(RoundedTextFieldStyle())
                            }
                            
                            VStack(alignment: .leading, spacing: 4) {
                                Text("Director")
                                    .font(.system(size: 14, weight: .medium))
                                    .foregroundColor(.white.opacity(0.8))
                                    .padding(.leading, 4)
                                TextField("Director name", text: $director)
                                    .textFieldStyle(RoundedTextFieldStyle())
                            }
                            
                            VStack(alignment: .leading, spacing: 4) {
                                Text("Cast")
                                    .font(.system(size: 14, weight: .medium))
                                    .foregroundColor(.white.opacity(0.8))
                                    .padding(.leading, 4)
                                TextField("Actor names (comma separated)", text: $cast)
                                    .textFieldStyle(RoundedTextFieldStyle())
                            }
                        }
                        
                        if subType == "Series" {
                            VStack(alignment: .leading, spacing: 4) {
                                Text("Number of Episodes")
                                    .font(.system(size: 14, weight: .medium))
                                    .foregroundColor(.white.opacity(0.8))
                                    .padding(.leading, 4)
                                TextField("e.g., 10", text: $episodeCount)
                                    .keyboardType(.numberPad)
                                    .textFieldStyle(RoundedTextFieldStyle())
                            }
                            
                            VStack(alignment: .leading, spacing: 4) {
                                Text("Showrunner / Creator")
                                    .font(.system(size: 14, weight: .medium))
                                    .foregroundColor(.white.opacity(0.8))
                                    .padding(.leading, 4)
                                TextField("Creator name", text: $director)
                                    .textFieldStyle(RoundedTextFieldStyle())
                            }
                            
                            VStack(alignment: .leading, spacing: 4) {
                                Text("Main Cast")
                                    .font(.system(size: 14, weight: .medium))
                                    .foregroundColor(.white.opacity(0.8))
                                    .padding(.leading, 4)
                                TextField("Actor names (comma separated)", text: $cast)
                                    .textFieldStyle(RoundedTextFieldStyle())
                            }
                            
                            VStack(alignment: .leading, spacing: 4) {
                                Text("Release Year")
                                    .font(.system(size: 14, weight: .medium))
                                    .foregroundColor(.white.opacity(0.8))
                                    .padding(.leading, 4)
                                TextField("e.g., 2024", text: $year)
                                    .keyboardType(.numberPad)
                                    .textFieldStyle(RoundedTextFieldStyle())
                            }
                        }
                        
                        if subType == "Podcast" {
                            VStack(alignment: .leading, spacing: 4) {
                                Text("Host *")
                                    .font(.system(size: 14, weight: .medium))
                                    .foregroundColor(.white.opacity(0.8))
                                    .padding(.leading, 4)
                                TextField("Host name", text: $host)
                                    .textFieldStyle(RoundedTextFieldStyle())
                            }
                            
                            VStack(alignment: .leading, spacing: 4) {
                                Text("Average Episode Duration (minutes)")
                                    .font(.system(size: 14, weight: .medium))
                                    .foregroundColor(.white.opacity(0.8))
                                    .padding(.leading, 4)
                                TextField("e.g., 45", text: $duration)
                                    .keyboardType(.numberPad)
                                    .textFieldStyle(RoundedTextFieldStyle())
                            }
                            
                            VStack(alignment: .leading, spacing: 4) {
                                Text("Number of Episodes")
                                    .font(.system(size: 14, weight: .medium))
                                    .foregroundColor(.white.opacity(0.8))
                                    .padding(.leading, 4)
                                TextField("e.g., 50", text: $episodeCount)
                                    .keyboardType(.numberPad)
                                    .textFieldStyle(RoundedTextFieldStyle())
                            }
                        }
                        
                        if subType == "AudioBook" {
                            VStack(alignment: .leading, spacing: 4) {
                                Text("Author *")
                                    .font(.system(size: 14, weight: .medium))
                                    .foregroundColor(.white.opacity(0.8))
                                    .padding(.leading, 4)
                                TextField("Author name", text: $author)
                                    .textFieldStyle(RoundedTextFieldStyle())
                            }
                            
                            VStack(alignment: .leading, spacing: 4) {
                                Text("Duration (hours)")
                                    .font(.system(size: 14, weight: .medium))
                                    .foregroundColor(.white.opacity(0.8))
                                    .padding(.leading, 4)
                                TextField("e.g., 8.5", text: $duration)
                                    .keyboardType(.decimalPad)
                                    .textFieldStyle(RoundedTextFieldStyle())
                            }
                            
                            VStack(alignment: .leading, spacing: 4) {
                                Text("Release Year")
                                    .font(.system(size: 14, weight: .medium))
                                    .foregroundColor(.white.opacity(0.8))
                                    .padding(.leading, 4)
                                TextField("e.g., 2024", text: $year)
                                    .keyboardType(.numberPad)
                                    .textFieldStyle(RoundedTextFieldStyle())
                            }
                        }
                        
                        Spacer()
                            .frame(height: 32)
                        
                        // Submit Button
                        Button(action: {
                            let formData: [String: String] = [
                                "type": subType,
                                "title": title,
                                "description": description,
                                "genre": genre,
                                "duration": duration,
                                "year": year,
                                "director": director,
                                "cast": cast,
                                "episodeCount": episodeCount,
                                "host": host,
                                "author": author
                            ]
                            onSubmitClick(formData)
                        }) {
                            Text("Submit")
                                .font(.system(size: 18, weight: .bold))
                                .foregroundColor(.white)
                                .frame(maxWidth: .infinity)
                                .frame(height: 56)
                                .background(
                                    RoundedRectangle(cornerRadius: 12)
                                        .fill(title.isEmpty || description.isEmpty ? 
                                              Color.gray.opacity(0.3) : Color(red: 0.388, green: 0.424, blue: 0.945))
                                )
                        }
                        .disabled(title.isEmpty || description.isEmpty)
                        
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

struct RoundedTextFieldStyle: TextFieldStyle {
    func _body(configuration: TextField<Self._Label>) -> some View {
        configuration
            .padding()
            .background(Color.white)
            .cornerRadius(12)
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(Color.gray.opacity(0.3), lineWidth: 1)
            )
    }
}

#Preview {
    MediaSubmissionView(
        contentType: "Video",
        subType: "Movie",
        onBackClick: { print("Back clicked") },
        onSubmitClick: { data in print("Submit clicked: \(data)") }
    )
}
