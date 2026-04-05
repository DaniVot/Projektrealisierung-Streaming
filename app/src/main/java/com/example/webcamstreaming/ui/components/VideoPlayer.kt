package com.example.webcamstreaming.ui.components

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.PlaybackException
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import androidx.compose.material3.OutlinedButton
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon

@Composable
fun VideoPlayer(
    url: String,
    modifier: Modifier = Modifier,
    onStatusChanged: (StreamStatus, String?) -> Unit = { _, _ -> },
    reloadToken: Int = 0,
    /** When true, ExoPlayer volume is 0f; otherwise 1f. */
    audioMuted: Boolean = false,
    onUrlCopied: () -> Unit = {},
    onError: (String) -> Unit = {},
    onRetry: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val clipboardManager = LocalClipboardManager.current

    var isBuffering by remember(url) { mutableStateOf(true) }
    var hasError by remember(url) { mutableStateOf(false) }
    var errorMessage by remember(url) { mutableStateOf<String?>(null) }
    var streamStatus by remember(url) { mutableStateOf(StreamStatus.BUFFERING) }

    val player = remember(url, reloadToken) {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .apply {
                if (url.contains("skylinewebcams.com")) {
                    setDefaultRequestProperties(
                        mapOf(
                            "Referer" to "https://www.skylinewebcams.com/",
                            "User-Agent" to "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                        )
                    )
                }
            }
        val mediaSourceFactory = DefaultMediaSourceFactory(context).setDataSourceFactory(httpDataSourceFactory)
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
            .apply {
                repeatMode = Player.REPEAT_MODE_ALL
                playWhenReady = true
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_BUFFERING -> {
                                isBuffering = true
                                streamStatus = StreamStatus.BUFFERING
                            }
                            Player.STATE_READY -> {
                                isBuffering = false
                                streamStatus = StreamStatus.STREAMING
                                hasError = false
                                errorMessage = null
                            }
                            Player.STATE_IDLE, Player.STATE_ENDED -> {
                                val err = this@apply.playerError
                                if (err != null) {
                                    hasError = true
                                    streamStatus = StreamStatus.ERROR
                                    errorMessage = mapErrorMessage(context)
                                    onError(errorMessage ?: "Stream nicht verfügbar")
                                }
                            }
                        }
                    }
                    override fun onIsLoadingChanged(isLoading: Boolean) {
                        isBuffering = isLoading
                        if (!isLoading && streamStatus != StreamStatus.ERROR) {
                            // The player might have moved to READY; keep status consistent.
                            streamStatus = StreamStatus.STREAMING
                        } else if (isLoading && !hasError) {
                            streamStatus = StreamStatus.BUFFERING
                        }
                    }
                    override fun onPlayerError(error: PlaybackException) {
                        hasError = true
                        streamStatus = StreamStatus.ERROR
                        errorMessage = mapErrorMessage(context)
                        onError(errorMessage ?: "Stream nicht verfügbar")
                    }
                })
                setMediaItem(MediaItem.fromUri(url))
                volume = if (audioMuted) 0f else 1f
                prepare()
            }
    }

    LaunchedEffect(player, audioMuted) {
        player.volume = if (audioMuted) 0f else 1f
    }

    DisposableEffect(lifecycleOwner, player) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> player.pause()
                Lifecycle.Event.ON_START, Lifecycle.Event.ON_RESUME -> player.play()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    DisposableEffect(player) {
        onDispose {
            player.release()
        }
    }

    LaunchedEffect(url, reloadToken) {
        hasError = false
        errorMessage = null
        streamStatus = StreamStatus.BUFFERING
        isBuffering = true
    }

    LaunchedEffect(isBuffering) {
        if (!isBuffering) return@LaunchedEffect
        delay(20_000L)
        if (isBuffering && !hasError) {
            hasError = true
            streamStatus = StreamStatus.ERROR
            errorMessage = "Verbindung unterbrochen"
            onError(errorMessage ?: "Verbindung unterbrochen")
        }
    }

    LaunchedEffect(streamStatus, errorMessage) {
        onStatusChanged(streamStatus, if (streamStatus == StreamStatus.ERROR) errorMessage else null)
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    this.player = player
                    useController = false
                }
            },
            update = { view -> view.player = player },
            modifier = Modifier.fillMaxSize()
        )
        if (hasError) {
            ErrorOverlay(
                message = errorMessage ?: "Stream nicht verfügbar",
                errorType = (errorMessage ?: "Stream nicht verfügbar"),
                onRetry = {
                    hasError = false
                    errorMessage = null
                    streamStatus = StreamStatus.BUFFERING
                    player.prepare()
                    player.play()
                    onRetry()
                },
                onCopyUrl = {
                    clipboardManager.setText(AnnotatedString(url))
                    onUrlCopied()
                }
            )
        } else if (isBuffering) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun ErrorOverlay(
    message: String,
    errorType: String,
    onRetry: () -> Unit,
    onCopyUrl: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = when (errorType) {
        "Keine Internetverbindung" -> Icons.Filled.WifiOff
        "Verbindung unterbrochen" -> Icons.Filled.Schedule
        else -> Icons.Filled.ErrorOutline
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Error icon",
                tint = MaterialTheme.colorScheme.error,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Button(onClick = onRetry) {
                Text("Retry")
            }
            OutlinedButton(onClick = onCopyUrl) {
                Text("URL kopieren")
            }
        }
    }
}

private fun mapErrorMessage(context: android.content.Context): String {
    val isConnected = isInternetAvailable(context)
    return if (!isConnected) {
        "Keine Internetverbindung"
    } else {
        // For HLS failures we keep it simple and show a user-friendly message.
        "Stream nicht verfügbar"
    }
}

private fun isInternetAvailable(context: android.content.Context): Boolean {
    val cm = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
    @Suppress("DEPRECATION")
    return cm.activeNetworkInfo?.isConnected == true
}
