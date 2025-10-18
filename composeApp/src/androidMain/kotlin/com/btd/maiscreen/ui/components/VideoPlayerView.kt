package com.btd.maiscreen.ui.components

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.datasource.DefaultDataSource
import com.btd.maiscreen.config.AppConfig
import com.btd.maiscreen.config.AppSettings
import com.btd.maiscreen.streaming.AndroidPlaybackApi
import com.btd.maiscreen.streaming.PlaybackConfig
import com.btd.maiscreen.streaming.PlaybackSuccess
import com.btd.maiscreen.ui.screens.ContentItem
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerView(
    contentItem: ContentItem,
    modifier: Modifier = Modifier,
    autoPlay: Boolean = true,
    onPlayerReady: () -> Unit = {},
    onPlayerError: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var manifestUrl by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Create playback API with OkHttp client
    val playbackApi = remember {
        AndroidPlaybackApi(
            PlaybackConfig(
                cloudFrontDomain = AppConfig.cloudFrontDomain
            )
        )
    }
    
    // Load manifest URL
    LaunchedEffect(contentItem.assetId, AppSettings.isLocalPlaybackEnabled) {
        coroutineScope.launch {
            try {
                // Check if this is a test item
                val isTestItem = contentItem.assetId.startsWith("TEST")
                
                if (isTestItem) {
                    Log.d("VideoPlayerView", "🧪 TEST MODE: Testing for asset: ${contentItem.assetId}")
                    
                    // Check if local playback is enabled in settings
                    if (AppSettings.isLocalPlaybackEnabled) {
                        Log.d("VideoPlayerView", "🧪 LOCAL MODE: Using demo video for test")
                        val demoUrl = when (contentItem.id) {
                            "1" -> AppConfig.demoVideo1
                            "2" -> AppConfig.demoVideo2  
                            "3" -> AppConfig.demoVideo3
                            else -> AppConfig.demoVideo1
                        }
                        Log.d("VideoPlayerView", "Using demo video: $demoUrl")
                        manifestUrl = demoUrl
                        isLoading = false
                        return@launch
                    } else {
                        Log.d("VideoPlayerView", "🧪 AWS MODE: Testing AWS API call and returning signed URL")
                    }
                } else {
                    Log.d("VideoPlayerView", "Loading manifest for asset: ${contentItem.assetId}")
                    
                    // Check if local playback is enabled in settings
                    if (AppSettings.isLocalPlaybackEnabled) {
                        Log.d("VideoPlayerView", "📱 LOCAL MODE: Using demo video directly")
                        val demoUrl = when (contentItem.id) {
                            "1" -> AppConfig.demoVideo1
                            "2" -> AppConfig.demoVideo2  
                            "3" -> AppConfig.demoVideo3
                            else -> AppConfig.demoVideo1
                        }
                        Log.d("VideoPlayerView", "Using demo video: $demoUrl")
                        manifestUrl = demoUrl
                        isLoading = false
                        return@launch
                    } else {
                        Log.d("VideoPlayerView", "☁️ AWS MODE: Attempting CloudFront streaming")
                    }
                }
                
                when (val result = playbackApi.getManifest(contentItem.assetId)) {
                    is PlaybackSuccess -> {
                        Log.d("VideoPlayerView", "Successfully got manifest: ${result.data}")
                        
                        if (isTestItem) {
                            // For test items, show the actual signed URL from AWS
                            Log.d("VideoPlayerView", "🧪 TEST COMPLETE: AWS API call successful!")
                            Log.d("VideoPlayerView", "🧪 Signed URL received: ${result.data}")
                            Log.d("VideoPlayerView", "🧪 This proves your AWS backend is working correctly!")
                            
                            // Show the signed URL in the error message (since we don't want to play it)
                            onPlayerError("🧪 AWS Test SUCCESS!\n\n✅ Signed URL received:\n${result.data}\n\n🎉 Your AWS backend is working!\n\nNote: This is a test - no video will play.")
                        } else {
                            // For real content, set manifestUrl to start playback
                            manifestUrl = result.data
                        }
                        isLoading = false
                    }
                    is com.btd.maiscreen.streaming.PlaybackError -> {
                        Log.e("VideoPlayerView", "Failed to load stream: ${result.message}", result.exception)
                        
                        if (isTestItem) {
                            onPlayerError("🧪 AWS Test FAILED!\n\n❌ Error: ${result.message}\n\n🔍 Check your AWS backend configuration:\n• Lambda function deployed?\n• Environment variables set?\n• API Gateway configured?\n• JWT token valid?\n\nCheck logs for details.")
                        } else {
                            // For real content in AWS mode, show error instead of fallback
                            if (!AppSettings.isLocalPlaybackEnabled) {
                                Log.e("VideoPlayerView", "AWS streaming failed - showing error instead of fallback")
                                onPlayerError("❌ AWS Streaming Failed!\n\nError: ${result.message}\n\n🔍 Check your AWS backend configuration:\n• CloudFront distribution working?\n• Lambda function responding?\n• Signed cookies valid?\n\nSwitch to Local mode in Settings to use demo videos.")
                            } else {
                                // Only fallback to demo videos in local mode
                                Log.w("VideoPlayerView", "Local mode fallback to demo video")
                                val demoUrl = when (contentItem.id) {
                                    "1" -> AppConfig.demoVideo1
                                    "2" -> AppConfig.demoVideo2  
                                    "3" -> AppConfig.demoVideo3
                                    else -> AppConfig.demoVideo1
                                }
                                Log.d("VideoPlayerView", "Using demo video: $demoUrl")
                                manifestUrl = demoUrl
                            }
                        }
                        isLoading = false
                    }
                }
            } catch (e: Exception) {
                Log.e("VideoPlayerView", "Exception loading stream", e)
                val isTestItem = contentItem.assetId.startsWith("TEST")
                
                if (isTestItem) {
                    onPlayerError("🧪 AWS Test ERROR!\n\n❌ Exception: ${e.message}\n\n🔍 Check your network connection and AWS backend setup.")
                } else {
                    // For real content in AWS mode, show error instead of fallback
                    if (!AppSettings.isLocalPlaybackEnabled) {
                        Log.e("VideoPlayerView", "AWS streaming exception - showing error instead of fallback")
                        onPlayerError("❌ AWS Streaming Exception!\n\nError: ${e.message}\n\n🔍 Check your network connection and AWS backend setup.\n\nSwitch to Local mode in Settings to use demo videos.")
                    } else {
                        // Only fallback to demo videos in local mode
                        Log.w("VideoPlayerView", "Local mode exception fallback to demo video")
                        val demoUrl = when (contentItem.id) {
                            "1" -> AppConfig.demoVideo1
                            "2" -> AppConfig.demoVideo2  
                            "3" -> AppConfig.demoVideo3
                            else -> AppConfig.demoVideo1
                        }
                        Log.d("VideoPlayerView", "Using demo video: $demoUrl")
                        manifestUrl = demoUrl
                    }
                }
                isLoading = false
            }
        }
    }
    
    val exoPlayer = remember(manifestUrl) {
        manifestUrl?.let { url ->
            Log.d("VideoPlayerView", "Creating ExoPlayer for URL: $url")
            
            // Determine if this is an HLS stream or regular MP4
            val isHlsStream = url.contains(".m3u8") || url.contains("hls")
            val isDemoVideo = url.contains("commondatastorage.googleapis.com")
            
            Log.d("VideoPlayerView", "URL type - HLS: $isHlsStream, Demo: $isDemoVideo")
            
            val player = if (isHlsStream) {
                // Use OkHttp data source for HLS (with cookies)
                val okClient = playbackApi.okHttpClient()
                val dataSourceFactory = OkHttpDataSource.Factory(okClient)
                val mediaSourceFactory = HlsMediaSource.Factory(dataSourceFactory)
                
                Log.d("VideoPlayerView", "Using OkHttp data source with cookies for HLS")
                
                ExoPlayer.Builder(context)
                    .setMediaSourceFactory(mediaSourceFactory)
                    .build()
            } else {
                // Use default data source for MP4 files (demo videos)
                val dataSourceFactory = DefaultDataSource.Factory(context)
                val mediaSourceFactory = ProgressiveMediaSource.Factory(dataSourceFactory)
                
                Log.d("VideoPlayerView", "Using default data source for MP4")
                
                ExoPlayer.Builder(context)
                    .setMediaSourceFactory(mediaSourceFactory)
                    .build()
            }
            
            player.apply {
                setMediaItem(MediaItem.fromUri(url))
                prepare()
                playWhenReady = autoPlay
                
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_READY -> {
                                Log.d("VideoPlayerView", "✅ ExoPlayer ready to play!")
                                onPlayerReady()
                            }
                            Player.STATE_ENDED -> {
                                Log.d("VideoPlayerView", "ExoPlayer finished playing")
                            }
                        }
                    }
                    
                    override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                        Log.e("VideoPlayerView", "❌ ExoPlayer error: ${error.message}", error)
                        Log.e("VideoPlayerView", "Error cause: ${error.cause}")
                        onPlayerError("ExoPlayer error: ${error.message}")
                    }
                })
            }
        }
    }
    
    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer?.release()
        }
    }
    
    if (isLoading || manifestUrl == null) {
        // Show loading state while fetching manifest
        androidx.compose.foundation.layout.Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            androidx.compose.material3.CircularProgressIndicator()
        }
    } else {
        exoPlayer?.let { player ->
            AndroidView(
                factory = { context ->
                    PlayerView(context).apply {
                        this.player = player
                        useController = true  // Re-enable ExoPlayer controls
                        setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)  // Show buffering indicator
                        controllerShowTimeoutMs = 3000  // Show controls for 3 seconds
                        controllerHideOnTouch = true    // Hide controls when touching
                        
                        // Add tap listener for close functionality (optional)
                        setOnClickListener {
                            Log.d("VideoPlayerView", "Video tapped - controls should handle this")
                        }
                    }
                },
                modifier = modifier.fillMaxSize()
            )
        }
    }
}
