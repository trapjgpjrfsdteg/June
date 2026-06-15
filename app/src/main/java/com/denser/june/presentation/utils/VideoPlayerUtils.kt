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
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory

@OptIn(UnstableApi::class)
@Composable
fun rememberManagedExoPlayer(
    uri: Uri,
    context: Context = LocalContext.current,
    repeatMode: Int = Player.REPEAT_MODE_ONE,
    onIsPlayingChanged: ((Boolean) -> Unit)? = null
): ExoPlayer {
    val exoPlayer = remember(uri) {
        // 1. Create a custom HTTP data source factory and set a realistic browser User-Agent string
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
            .setAllowCrossProtocolRedirects(true)

        // 2. Wrap the HTTP source factor inside standard DefaultDataSource factory
        val dataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)
        
        // 3. Attach it into MediaSource setup to handle streaming content types
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