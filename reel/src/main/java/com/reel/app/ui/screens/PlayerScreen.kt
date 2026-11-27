package com.reel.app.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.View
import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import com.reel.app.data.LibraryRules
import com.reel.app.data.Meta
import com.reel.app.data.MetaPreview
import com.reel.app.data.Subtitle
import com.reel.app.ui.ReelColors
import com.reel.app.ui.components.LocalApp
import kotlinx.coroutines.delay

private fun subtitleMime(url: String): String {
    val path = Uri.parse(url).path.orEmpty().lowercase()
    return when {
        path.endsWith(".vtt") -> MimeTypes.TEXT_VTT
        path.endsWith(".ass") || path.endsWith(".ssa") -> MimeTypes.TEXT_SSA
        path.endsWith(".ttml") || path.endsWith(".xml") -> MimeTypes.APPLICATION_TTML
        else -> MimeTypes.APPLICATION_SUBRIP
    }
}

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(nav: NavController) {
    val app = LocalApp.current
    val pb = remember { app.pendingPlayback }
    if (pb == null) {
        LaunchedEffect(Unit) { nav.popBackStack() }
        return
    }
    val context = LocalContext.current
    val activity = context as Activity
    val settings by app.settings.state.collectAsState()
    val meta by produceState<Meta?>(null) { value = runCatching { app.client.meta(app.addons.enabled, pb.type, pb.itemId) }.getOrNull() }
    var controlsVisible by remember { mutableStateOf(true) }
    var ended by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Fullscreen landscape, screen kept on, while the player is open.
    DisposableEffect(Unit) {
        val window = activity.window
        val previousOrientation = activity.requestedOrientation
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val insets = WindowCompat.getInsetsController(window, window.decorView)
        insets.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insets.hide(WindowInsetsCompat.Type.systemBars())
        onDispose {
            activity.requestedOrientation = previousOrientation
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            insets.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    val player = remember {
        val http = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setDefaultRequestProperties(pb.stream.headers)
        ExoPlayer.Builder(context).setMediaSourceFactory(DefaultMediaSourceFactory(context).setDataSourceFactory(http)).build().apply {
            val subs: List<Subtitle> = (pb.stream.subtitles + app.pendingSubtitles).distinctBy { it.url }
            val preferred = settings.subtitleLang
            val item = MediaItem.Builder()
                .setUri(pb.stream.url)
                .setSubtitleConfigurations(subs.map { s ->
                    MediaItem.SubtitleConfiguration.Builder(Uri.parse(s.url))
                        .setMimeType(subtitleMime(s.url))
                        .setLanguage(s.lang)
                        .setLabel(s.lang.uppercase())
                        .build()
                })
                .build()
            // With no preferred language, subtitles stay off until picked from the subtitle button.
            if (preferred.isNotEmpty()) {
                trackSelectionParameters = trackSelectionParameters.buildUpon().setPreferredTextLanguage(preferred).build()
            }
            setMediaItem(item, pb.startPositionMs)
            prepare()
            playWhenReady = true
        }
    }

    fun preview(): MetaPreview = meta?.preview ?: app.library.state.value[pb.itemId]?.toPreview()
        ?: MetaPreview(pb.itemId, pb.type, pb.title, null, "poster", null, null, null, null, emptyList())

    fun save(position: Long = player.currentPosition) {
        val m = meta
        app.library.update { s, now ->
            LibraryRules.recordProgress(
                s, preview(), pb.videoId, position, player.duration.takeIf { it > 0 } ?: 0, now,
                nextVideoId = m?.nextVideo(pb.videoId)?.id,
                allVideoIds = m?.let(::releasedVideos)?.map { it.id }.orEmpty(),
            )
        }
    }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) { save(player.duration); ended = true }
            }
            override fun onPlayerError(e: PlaybackException) { error = e.errorCodeName.removePrefix("ERROR_CODE_").replace('_', ' ').lowercase() }
        }
        player.addListener(listener)
        val owner = (context as androidx.lifecycle.LifecycleOwner)
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP) { player.pause(); save() } }
        owner.lifecycle.addObserver(observer)
        onDispose {
            owner.lifecycle.removeObserver(observer)
            player.removeListener(listener)
            if (!ended) save()
            player.release()
        }
    }

    // Save progress every 15 s so a crash or kill doesn't lose the position.
    LaunchedEffect(player) {
        while (true) { delay(15_000); if (player.isPlaying) save() }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = player
                    setShowSubtitleButton(true)
                    setShowNextButton(false)
                    setShowPreviousButton(false)
                    setControllerVisibilityListener(PlayerView.ControllerVisibilityListener { v -> controlsVisible = v == View.VISIBLE })
                }
            },
            modifier = Modifier.fillMaxSize(),
        )
        if (controlsVisible || ended || error != null) {
            Row(
                Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent))).padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = Color.White) }
                Column {
                    Text(pb.title, color = Color.White, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    pb.subtitle?.let { Text(it, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall, maxLines = 1) }
                }
            }
        }
        val next = meta?.nextVideo(pb.videoId)
        when {
            error != null -> PlayerCard("Can't play this stream", "Reason: $error. Try another source, or open it in another player.") {
                OutlinedButton(onClick = { nav.popBackStack() }) { Text("Back", color = Color.White) }
                Button(onClick = { openInExternalPlayer(context, pb.stream.url!!, pb.title) }, colors = ButtonDefaults.buttonColors(containerColor = ReelColors.Accent, contentColor = ReelColors.OnAccent)) { Text("Other player") }
            }
            ended && next != null && settings.autoplayNext -> PlayerCard("Up next", "${next.label} · ${next.title}") {
                OutlinedButton(onClick = { nav.popBackStack() }) { Text("Done", color = Color.White) }
                Button(
                    onClick = {
                        nav.previousBackStackEntry?.savedStateHandle?.set("openVideo", next.id)
                        nav.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ReelColors.Accent, contentColor = ReelColors.OnAccent),
                ) { Text("Play next") }
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.BoxScope.PlayerCard(title: String, body: String, buttons: @Composable () -> Unit) {
    Column(
        Modifier.align(Alignment.Center).widthIn(max = 420.dp).padding(24.dp).clip(RoundedCornerShape(18.dp)).background(ReelColors.Surface.copy(alpha = 0.95f)).padding(20.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = Color.White)
        Spacer(Modifier.height(6.dp))
        Text(body, color = ReelColors.Muted)
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)) { buttons() }
    }
}
