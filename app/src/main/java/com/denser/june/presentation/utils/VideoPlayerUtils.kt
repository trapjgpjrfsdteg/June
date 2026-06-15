package com.denser.june.presentation.utils

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.denser.june.core.utils.Constants

@OptIn(UnstableApi::class)
fun createHttpDataSourceFactory(): HttpDataSource.Factory {
    return DefaultHttpDataSource.Factory()
        .setUserAgent(Constants.USER_AGENT)
        .setAllowCrossProtocolRedirects(true)
        .setDefaultRequestProperties(
            mapOf(
                "Origin" to Constants.YOUTUBE_ORIGIN,
                "Referer" to Constants.YOUTUBE_REFERER
            )
        )
}

@OptIn(UnstableApi::class)
@Composable
fun rememberManagedExoPlayer(
    uri: Uri,
    context: Context = LocalContext.current,
    repeatMode: Int = Player.REPEAT_MODE_ONE,
    onIsPlayingChanged: ((Boolean) -> Unit)? = null
): ExoPlayer {
    val exoPlayer = remember(uri) {
        val httpDataSourceFactory = createHttpDataSourceFactory()

        val dataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)

        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(dataSourceFactory)

        // 4. Instantiate the player using our custom network configurations
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build().apply {
                setMediaItem(MediaItem.fromUri(uri))
                prepare()
                this.repeatMode = repeatMode
            }
    }

    LaunchedEffect(repeatMode) {
        exoPlayer.repeatMode = repeatMode
    }

    DisposableEffect(exoPlayer, onIsPlayingChanged) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                onIsPlayingChanged?.invoke(isPlaying)
            }
        }
        exoPlayer.addListener(listener)
        onDispose { exoPlayer.removeListener(listener) }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                exoPlayer.pause()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    return exoPlayer
}