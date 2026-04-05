package com.example.webcamstreaming.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.webcamstreaming.data.Webcam
import com.example.webcamstreaming.ui.components.StreamStatus
import com.example.webcamstreaming.ui.components.VideoPlayer
import java.util.Date
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    webcam: Webcam,
    onBack: () -> Unit,
    onUserMessage: (String) -> Unit = {},
    isAudioMuted: Boolean = false,
    onToggleAudioMute: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var urlExpanded by remember { mutableStateOf(false) }
    var streamStatus by remember { mutableStateOf(StreamStatus.BUFFERING) }
    var reloadToken by remember { mutableStateOf(0) }
    var isFullscreen by remember { mutableStateOf(false) }
    var fullscreenControlsVisible by remember { mutableStateOf(false) }
    val fullscreenTapInteraction = remember { MutableInteractionSource() }

    LaunchedEffect(isFullscreen) {
        if (isFullscreen) {
            fullscreenControlsVisible = false
        }
    }

    LaunchedEffect(isFullscreen, fullscreenControlsVisible, streamStatus) {
        if (!isFullscreen || !fullscreenControlsVisible || streamStatus == StreamStatus.ERROR) {
            return@LaunchedEffect
        }
        delay(5_000L)
        fullscreenControlsVisible = false
    }

    LaunchedEffect(isFullscreen, streamStatus) {
        if (isFullscreen && streamStatus == StreamStatus.ERROR) {
            fullscreenControlsVisible = true
        }
    }

    DisposableEffect(isFullscreen) {
        val activity = context as? Activity
        if (activity != null) {
            val window = activity.window
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            if (isFullscreen) {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                // Align with MainActivity.enableEdgeToEdge() — never use decorFits true here,
                // or returning to the list breaks TopAppBar / status-bar inset layout.
                WindowCompat.setDecorFitsSystemWindows(window, false)
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
        onDispose {
            val act = context as? Activity
            if (act != null) {
                val window = act.window
                val controller = WindowCompat.getInsetsController(window, window.decorView)
                controller.show(WindowInsetsCompat.Type.systemBars())
                WindowCompat.setDecorFitsSystemWindows(window, false)
            }
        }
    }

    BackHandler {
        if (isFullscreen) {
            isFullscreen = false
        } else {
            onBack()
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        if (!isFullscreen) {
            TopAppBar(
                title = { Text(webcam.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                },
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            VideoPlayer(
                url = webcam.streamUrl,
                modifier = Modifier.fillMaxSize(),
                onStatusChanged = { status, _ ->
                    streamStatus = status
                },
                reloadToken = reloadToken,
                audioMuted = isAudioMuted,
                onUrlCopied = { onUserMessage("URL kopiert") }
            )

            if (isFullscreen && streamStatus != StreamStatus.ERROR) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = fullscreenTapInteraction,
                            indication = null
                        ) {
                            fullscreenControlsVisible = !fullscreenControlsVisible
                        }
                )
            }

            val showOverlayControls = !isFullscreen || fullscreenControlsVisible
            if (showOverlayControls) {
                if (isFullscreen) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .statusBarsPadding()
                            .padding(12.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        shadowElevation = 4.dp
                    ) {
                        IconButton(
                            onClick = { isFullscreen = false },
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Icon(
                                Icons.Filled.ArrowBack,
                                contentDescription = "Vollbild verlassen"
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .then(
                            if (isFullscreen) {
                                Modifier
                                    .navigationBarsPadding()
                                    .padding(12.dp)
                            } else {
                                Modifier.padding(12.dp)
                            }
                        )
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(
                        onClick = { reloadToken += 1 },
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Stream neu laden")
                    }
                    IconButton(
                        onClick = onToggleAudioMute,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isAudioMuted) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                            contentDescription = if (isAudioMuted) "Ton einschalten" else "Stummschalten",
                            tint = if (isAudioMuted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { isFullscreen = !isFullscreen },
                        modifier = Modifier.padding(4.dp)
                    ) {
                        val icon = if (isFullscreen) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen
                        Icon(
                            icon,
                            contentDescription = if (isFullscreen) "Vollbild beenden" else "Vollbild"
                        )
                    }
                }
            }
        }

        if (!isFullscreen) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = webcam.streamUrl,
                    maxLines = if (urlExpanded) Int.MAX_VALUE else 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { urlExpanded = !urlExpanded }
                )

                Text(
                    text = "Hinzugefügt: " + addedAtText(webcam.addedAt),
                    modifier = Modifier.padding(top = 6.dp)
                )

                val statusLabel = when (streamStatus) {
                    StreamStatus.STREAMING -> "Streaming"
                    StreamStatus.BUFFERING -> "Buffering"
                    StreamStatus.ERROR -> "Error"
                }
                Text(
                    text = "Status: $statusLabel",
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun addedAtText(addedAt: Long): String {
    val formatted = remember(addedAt) {
        if (addedAt <= 0L) return@remember "Unbekannt"
        val df = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
        df.format(Date(addedAt))
    }
    return formatted
}
