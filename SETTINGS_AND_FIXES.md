# Settings and Video Player Fixes

## Changes Made

### 1. Fixed Video Player Close Button ✅
**Issue:** The close button in the video player wasn't working properly.

**Solution:** 
- Changed from using a clickable `Box` to using `IconButton` in `VideoPlayerScreen.kt`
- This provides better click handling and follows Material Design patterns
- Added debug logging to track button clicks

**Files Modified:**
- `composeApp/src/commonMain/kotlin/com/btd/maiscreen/ui/screens/VideoPlayerScreen.kt`

### 2. Added Playback Mode Toggle ✅
**Feature:** Users can now toggle between local video playback and AWS CloudFront streaming.

**Implementation:**

#### New Files Created:
1. **`AppSettings.kt`** - Global settings state manager
   - `isLocalPlaybackEnabled`: Boolean state for playback mode
   - `togglePlaybackMode()`: Toggle between local and AWS modes
   - `setPlaybackMode(isLocal: Boolean)`: Set specific playback mode

2. **`SettingsScreen.kt`** - Settings UI screen
   - Clean, modern Material 3 design
   - Playback mode toggle with visual feedback
   - Info box explaining each mode
   - Placeholder sections for future settings (Account, About)

#### Files Modified:

1. **`App.kt`**
   - Added `AppScreen.Settings` enum value
   - Added `SettingsScreen` case in navigation
   - Integrated `AppSettings` for state management

2. **`MainScreen.kt`**
   - Added `onSettingsClick` callback parameter
   - Added settings button (⚙️) to top navigation bar
   - Button placed between search and profile icons

3. **`VideoPlayerView.kt` (Android)**
   - Integrated `AppSettings.isLocalPlaybackEnabled` check
   - When **Local Mode** is enabled:
     - Plays demo videos directly from device
     - Skips CloudFront API calls
     - Logs: "📱 LOCAL MODE: Using demo video directly"
   - When **AWS Mode** is enabled:
     - Attempts CloudFront streaming with JWT authentication
     - Falls back to demo videos if AWS fails
     - Logs: "☁️ AWS MODE: Attempting CloudFront streaming"
   - `LaunchedEffect` now observes `AppSettings.isLocalPlaybackEnabled` for reactive updates

## How to Use

### Accessing Settings
1. Launch the app
2. On the main screen, tap the **⚙️ Settings** icon in the top navigation bar
3. The settings screen will open

### Toggling Playback Mode
1. In the settings screen, find the "Video Playback" section
2. Use the switch to toggle between:
   - **OFF (AWS Streaming)**: Streams videos from AWS CloudFront with authentication
   - **ON (Local Videos)**: Plays demo videos from device storage
3. The change takes effect immediately
4. Return to the main screen and play any video to test

### Testing the Modes

#### Local Mode (Toggle ON):
- Videos play immediately without API calls
- Uses demo videos (BigBuckBunny, ElephantsDream, etc.)
- No authentication required
- Perfect for testing UI without backend

#### AWS Mode (Toggle OFF):
- Requires authentication (JWT token)
- Makes API calls to CloudFront
- Retrieves signed cookies
- Streams content from AWS
- Falls back to demo videos if AWS fails

## Technical Details

### State Management
- Uses Compose `mutableStateOf` for reactive state
- Settings persist only during app session (not saved to disk yet)
- State changes trigger video player reload via `LaunchedEffect` key

### Video Player Logic
```kotlin
if (AppSettings.isLocalPlaybackEnabled) {
    // Use local demo videos
    val demoUrl = when (contentItem.id) {
        "1" -> AppConfig.demoVideo1
        "2" -> AppConfig.demoVideo2  
        "3" -> AppConfig.demoVideo3
        else -> AppConfig.demoVideo1
    }
    manifestUrl = demoUrl
} else {
    // Attempt AWS CloudFront streaming
    playbackApi.getManifest(contentItem.assetId)
}
```

### Navigation Flow
```
MainScreen (⚙️ button) → SettingsScreen
  ↓
Settings changed (toggle switch)
  ↓
AppSettings.isLocalPlaybackEnabled updated
  ↓
Video player reacts to state change
```

## Future Enhancements
- [ ] Persist settings to local storage (SharedPreferences/UserDefaults)
- [ ] Add more settings options (quality, autoplay, etc.)
- [ ] Add account management settings
- [ ] Add network status indicator
- [ ] Add video quality selector
- [ ] Add download settings for offline playback

## Demo Videos Used
When in Local Mode, the following public demo videos are used:
1. Big Buck Bunny - https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4
2. Elephants Dream - https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4
3. For Bigger Blazes - https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4

## Notes
- The close button fix improves user experience across all video playback modes
- Settings screen uses Material 3 design language for consistency
- Playback mode toggle provides instant feedback with no app restart needed
- AWS mode includes proper error handling and fallback mechanisms

