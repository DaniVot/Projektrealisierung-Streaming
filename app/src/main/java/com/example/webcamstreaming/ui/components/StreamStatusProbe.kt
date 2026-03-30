package com.example.webcamstreaming.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import kotlinx.coroutines.delay

/**
 * Probes a stream in the background (no video UI) to derive a quick status for a camera card.
 *
 * - BUFFERING while loading
 * - STREAMING when the manifest is ready
 * - ERROR on failures
 */
@Composable
fun StreamStatusProbe(
    url: String,
    timeoutMs: Long = 20_000L,
    onStatusChanged: (StreamStatus) -> Unit,
) {
    val context = LocalContext.current

    var status by remember(url) { mutableStateOf(StreamStatus.BUFFERING) }

    val player = remember(url) {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory().apply {
            // Skyline streams often require a specific Referer/User-Agent.
            if (url.contains("skylinewebcams.com")) {
                setDefaultRequestProperties(
                    mapOf(
                        "Referer" to "https://www.skylinewebcams.com/",
                        "User-Agent" to "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36",
                    )
                )
            }
        }

        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(httpDataSourceFactory)

        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
            .apply {
                // We only want "is it reachable/parsable" status, not continuous playback.
                playWhenReady = false
                repeatMode = Player.REPEAT_MODE_OFF
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_BUFFERING -> status = StreamStatus.BUFFERING
                            Player.STATE_READY -> status = StreamStatus.STREAMING
                            Player.STATE_IDLE -> {
                                val err = this@apply.playerError
                                if (err != null) status = StreamStatus.ERROR
                            }
                        }
                    }

                    override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                        status = StreamStatus.ERROR
                    }
                })
                setMediaItem(MediaItem.fromUri(url))
                prepare()
            }
    }

    // Timeout: if we still only buffer after a while, mark as error.
    LaunchedEffect(url) {
        status = StreamStatus.BUFFERING
        delay(timeoutMs)
        if (status == StreamStatus.BUFFERING) {
            status = StreamStatus.ERROR
        }
    }

    LaunchedEffect(status) {
        onStatusChanged(status)
    }

    DisposableEffect(player) {
        onDispose { player.release() }
    }
}

