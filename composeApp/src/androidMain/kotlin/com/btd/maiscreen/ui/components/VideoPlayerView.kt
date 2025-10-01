package com.btd.maiscreen.ui.components

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
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
import com.btd.maiscreen.config.AppConfig
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
    LaunchedEffect(contentItem.assetId) {
        coroutineScope.launch {
            try {
                // Check if this is a test item
                val isTestItem = contentItem.assetId.startsWith("TEST")
                
                if (isTestItem) {
                    Log.d("VideoPlayerView", "🧪 TEST MODE: Testing cookie retrieval for asset: ${contentItem.assetId}")
                    Log.d("VideoPlayerView", "🧪 This will NOT start video playback - only test cookies")
                } else {
                    Log.d("VideoPlayerView", "Loading manifest for asset: ${contentItem.assetId}")
                }
                
                when (val result = playbackApi.getManifest(contentItem.assetId)) {
                    is PlaybackSuccess -> {
                        Log.d("VideoPlayerView", "Successfully got manifest: ${result.data}")
                        
                        if (isTestItem) {
                            // For test items, don't set manifestUrl (no video playback)
                            Log.d("VideoPlayerView", "🧪 TEST COMPLETE: Cookie test finished successfully!")
                            Log.d("VideoPlayerView", "🧪 Check logs above for cookie details")
                            onPlayerError("🧪 Cookie Test Complete!\n\nCheck logs for cookie details.\nNo video will play in test mode.")
                        } else {
                            // For real content, set manifestUrl to start playback
                            manifestUrl = result.data
                        }
                        isLoading = false
                    }
                    is com.btd.maiscreen.streaming.PlaybackError -> {
                        Log.e("VideoPlayerView", "Failed to load stream: ${result.message}", result.exception)
                        
                        if (isTestItem) {
                            onPlayerError("🧪 Cookie Test FAILED!\n\n${result.message}\n\nCheck logs for details.")
                        } else {
                            onPlayerError("Failed to load stream: ${result.message}")
                        }
                        isLoading = false
                    }
                }
            } catch (e: Exception) {
                Log.e("VideoPlayerView", "Exception loading stream", e)
                val isTestItem = contentItem.assetId.startsWith("TEST")
                
                if (isTestItem) {
                    onPlayerError("🧪 Cookie Test ERROR!\n\n${e.message}")
                } else {
                    onPlayerError("Error loading stream: ${e.message}")
                }
                isLoading = false
            }
        }
    }
    
    val exoPlayer = remember(manifestUrl) {
        manifestUrl?.let { url ->
            Log.d("VideoPlayerView", "Creating ExoPlayer for URL: $url")
            
            // Use the same OkHttp client that has the cookies
            val okClient = playbackApi.okHttpClient()
            val dataSourceFactory = OkHttpDataSource.Factory(okClient)
            val mediaSourceFactory = HlsMediaSource.Factory(dataSourceFactory)
            
            Log.d("VideoPlayerView", "Using OkHttp data source with cookies for HLS")
            
            ExoPlayer.Builder(context)
                .setMediaSourceFactory(mediaSourceFactory)
                .build()
                .apply {
                    setMediaItem(MediaItem.fromUri(url))
                    prepare()
                    playWhenReady = autoPlay
                    
                    addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(playbackState: Int) {
                            when (playbackState) {
                                Player.STATE_READY -> {
                                    Log.d("VideoPlayerView", "✅ ExoPlayer ready to play with OkHttp cookies!")
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
                        useController = true
                        setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                        // Disable ExoPlayer's own back button to prevent app exit
                        controllerShowTimeoutMs = 3000
                        controllerHideOnTouch = true
                    }
                },
                modifier = modifier.fillMaxSize()
            )
        }
    }
}
