package com.reel.player

import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Rational
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.Audiotrack
import androidx.compose.material.icons.rounded.Brightness6
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Forward10
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PictureInPictureAlt
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Replay10
import androidx.compose.material.icons.rounded.ScreenRotation
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Subtitles
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.media3.ui.SubtitleView
import kotlinx.coroutines.delay
import kotlin.math.abs

/** What to play, read from the launching intent (MX Player-compatible extras are understood). */
data class PlayRequest(
    val uri: Uri,
    val title: String,
    val startMs: Long?,
    val subtitles: List<Pair<Uri, String?>>,
    val headers: Map<String, String>,
)

private fun subtitleMime(name: String): String {
    val n = name.lowercase().substringBefore('?')
    return when {
        n.endsWith(".vtt") -> MimeTypes.TEXT_VTT
        n.endsWith(".ass") || n.endsWith(".ssa") -> MimeTypes.TEXT_SSA
        n.endsWith(".ttml") || n.endsWith(".xml") || n.endsWith(".dfxp") -> MimeTypes.APPLICATION_TTML
        else -> MimeTypes.APPLICATION_SUBRIP
    }
}

fun Context.displayName(uri: Uri): String? =
    if (uri.scheme == "content") runCatching {
        contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
            if (c.moveToFirst()) c.getString(0) else null
        }
    }.getOrNull() else null

private enum class Fit(val mode: Int, val label: String) {
    Fit(AspectRatioFrameLayout.RESIZE_MODE_FIT, "Fit"),
    Zoom(AspectRatioFrameLayout.RESIZE_MODE_ZOOM, "Zoom"),
    Stretch(AspectRatioFrameLayout.RESIZE_MODE_FILL, "Stretch"),
}

@OptIn(UnstableApi::class)
class PlayerActivity : ComponentActivity() {
    private lateinit var history: HistoryStore
    private var player by mutableStateOf<ExoPlayer?>(null)
    private var request by mutableStateOf<PlayRequest?>(null)
    private var extraSubs = mutableListOf<Pair<Uri, String?>>()
    private var inPip by mutableStateOf(false)
    private var ended = false
    private var manualOrientation = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        history = HistoryStore.get(this)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        immersive()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        val req = parse(intent)
        if (req == null) { finish(); return }
        start(req)
        setContent { PlayerScreen() }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        parse(intent)?.let { saveProgress(); start(it) }
    }

    private fun immersive() {
        val c = WindowCompat.getInsetsController(window, window.decorView)
        c.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        c.hide(WindowInsetsCompat.Type.systemBars())
    }

    @Suppress("DEPRECATION")
    private fun parse(i: Intent): PlayRequest? {
        val uri = i.data ?: (i.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)) ?: return null
        val title = i.getStringExtra("title") ?: i.getStringExtra(Intent.EXTRA_TITLE)
            ?: displayName(uri)?.substringBeforeLast('.') ?: Format.titleFrom(uri.toString())
        val start = (i.extras?.get("position") as? Number)?.toLong()?.takeIf { it > 0 }
        val subUris = i.getParcelableArrayExtra("subs")?.mapNotNull { it as? Uri }.orEmpty()
        val subNames = i.getStringArrayExtra("subs.name")
        val headerArr = i.getStringArrayExtra("headers")
        val headers = headerArr?.toList()?.chunked(2)?.filter { it.size == 2 }?.associate { it[0] to it[1] }.orEmpty()
        return PlayRequest(uri, title, start, subUris.mapIndexed { n, u -> u to subNames?.getOrNull(n) }, headers)
    }

    private fun buildItem(req: PlayRequest): MediaItem {
        val subs = (req.subtitles + extraSubs).distinctBy { it.first }
        return MediaItem.Builder()
            .setUri(req.uri)
            .setSubtitleConfigurations(subs.map { (u, name) ->
                val label = name ?: displayName(u) ?: u.lastPathSegment ?: "Subtitles"
                MediaItem.SubtitleConfiguration.Builder(u)
                    .setMimeType(subtitleMime(displayName(u) ?: u.toString()))
                    .setLabel(label)
                    .setSelectionFlags(if (u in extraSubs.map { it.first }) C.SELECTION_FLAG_DEFAULT else 0)
                    .build()
            })
            .build()
    }

    private fun start(req: PlayRequest) {
        player?.release()
        extraSubs = mutableListOf()
        ended = false
        request = req
        val http = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setUserAgent("ReelPlayer/1.0 (Android)")
            .setDefaultRequestProperties(req.headers)
        val p = ExoPlayer.Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(DefaultDataSource.Factory(this, http)))
            .setSeekBackIncrementMs(10_000)
            .setSeekForwardIncrementMs(10_000)
            .build()
        p.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                else window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) { ended = true; saveProgress() }
            }
            override fun onVideoSizeChanged(size: VideoSize) {
                if (!manualOrientation && size.width > 0 && size.height > 0) {
                    requestedOrientation = if (size.height > size.width) ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                    else ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                }
                if (Build.VERSION.SDK_INT >= 26) runCatching { setPictureInPictureParams(pipParams()) }
            }
        })
        p.setMediaItem(buildItem(req), req.startMs ?: history.resumePosition(req.uri.toString()))
        p.prepare()
        p.playWhenReady = true
        player = p
    }

    /** Adds a subtitle file and keeps playing from the same spot. */
    fun addSubtitle(uri: Uri) {
        val p = player ?: return
        val req = request ?: return
        extraSubs.add(uri to displayName(uri))
        val pos = p.currentPosition
        val playing = p.playWhenReady
        p.setMediaItem(buildItem(req), pos)
        p.prepare()
        p.playWhenReady = playing
    }

    fun toggleOrientation() {
        manualOrientation = true
        requestedOrientation = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT else ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
    }

    fun saveProgress() {
        val p = player ?: return
        val req = request ?: return
        val dur = p.duration.takeIf { it > 0 } ?: 0
        val pos = if (ended) dur else p.currentPosition
        if (pos <= 0 && dur <= 0) return
        history.record(HistoryEntry(req.uri.toString(), req.title, pos, dur, System.currentTimeMillis()))
    }

    private fun pipParams(): PictureInPictureParams {
        val size = player?.videoSize
        val ratio = if (size != null && size.width > 0 && size.height > 0) {
            val r = size.width.toFloat() / size.height
            when { r > 2.39f -> Rational(239, 100); r < 1 / 2.39f -> Rational(100, 239); else -> Rational(size.width, size.height) }
        } else Rational(16, 9)
        return PictureInPictureParams.Builder().setAspectRatio(ratio).build()
    }

    fun enterPip() {
        if (packageManager.hasSystemFeature("android.software.picture_in_picture")) runCatching { enterPictureInPictureMode(pipParams()) }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (player?.isPlaying == true) enterPip()
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        inPip = isInPictureInPictureMode
    }

    override fun onStop() {
        super.onStop()
        saveProgress()
        player?.pause() // also covers closing the picture-in-picture window
    }

    override fun onDestroy() {
        player?.release()
        player = null
        super.onDestroy()
    }

    /** Tells the app that launched us where playback stopped (same keys as MX Player). */
    override fun finish() {
        player?.let { p ->
            setResult(RESULT_OK, Intent()
                .putExtra("position", (if (ended) p.duration else p.currentPosition).toInt())
                .putExtra("duration", p.duration.coerceAtLeast(0).toInt())
                .putExtra("end_by", if (ended) "playback_completion" else "user"))
        }
        super.finish()
    }

    fun brightness(): Float {
        val b = window.attributes.screenBrightness
        if (b >= 0) return b
        return runCatching { Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS) / 255f }.getOrDefault(0.5f)
    }

    fun setBrightness(v: Float) {
        window.attributes = window.attributes.apply { screenBrightness = v.coerceIn(0.01f, 1f) }
    }

    private val audio by lazy { getSystemService(AUDIO_SERVICE) as AudioManager }
    fun volume(): Float = audio.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    fun setVolume(v: Float) {
        val max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        audio.setStreamVolume(AudioManager.STREAM_MUSIC, (v * max).toInt().coerceIn(0, max), 0)
    }

    @Composable
    private fun PlayerScreen() {
        PlayerTheme {
            val p = player
            val req = request
            if (p != null && req != null) PlayerUi(this, p, req, inPip)
        }
    }
}

private val Accent = Color(0xFFE8B04B)

@OptIn(UnstableApi::class)
@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerUi(activity: PlayerActivity, player: ExoPlayer, req: PlayRequest, inPip: Boolean) {
    var isPlaying by remember { mutableStateOf(player.isPlaying) }
    var buffering by remember { mutableStateOf(true) }
    var position by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var ended by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var controls by remember { mutableStateOf(true) }
    var locked by remember { mutableStateOf(false) }
    var fit by remember { mutableStateOf(Fit.Fit) }
    var speed by remember { mutableFloatStateOf(1f) }
    var speedMenu by remember { mutableStateOf(false) }
    var trackSheet by remember { mutableIntStateOf(-1) } // C.TRACK_TYPE_AUDIO / TEXT, -1 = closed
    var subScale by remember { mutableFloatStateOf(1f) }
    var indicator by remember { mutableStateOf<Pair<ImageVector?, String>?>(null) }
    var seekPreview by remember { mutableStateOf<Long?>(null) }
    var dragging by remember { mutableStateOf<Float?>(null) } // slider value while dragging
    var touchTick by remember { mutableIntStateOf(0) }
    var playerView by remember { mutableStateOf<PlayerView?>(null) }

    // Poll the player for the UI (cheap, and simpler than wiring every callback).
    LaunchedEffect(player) {
        var saveTick = 0
        while (true) {
            isPlaying = player.isPlaying
            buffering = player.playbackState == Player.STATE_BUFFERING
            ended = player.playbackState == Player.STATE_ENDED
            position = player.currentPosition
            duration = player.duration.coerceAtLeast(0)
            error = player.playerError?.let { e -> e.errorCodeName.removePrefix("ERROR_CODE_").replace('_', ' ').lowercase() }
            if (++saveTick % 20 == 0 && isPlaying) activity.saveProgress()
            delay(250)
        }
    }
    // Auto-hide the controls while playing.
    LaunchedEffect(controls, isPlaying, touchTick) {
        if (controls && isPlaying) { delay(3500); controls = false }
    }
    LaunchedEffect(indicator) { if (indicator != null) { delay(900); indicator = null } }
    LaunchedEffect(ended) { if (ended) controls = true }

    val subPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) activity.addSubtitle(uri)
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false
                    this.player = player
                    setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
                    playerView = this
                }
            },
            update = { v ->
                v.player = player
                v.resizeMode = fit.mode
                v.subtitleView?.setFractionalTextSize(SubtitleView.DEFAULT_TEXT_SIZE_FRACTION * subScale)
            },
            modifier = Modifier.fillMaxSize(),
        )
        if (inPip) return@Box

        // Gesture layer: tap toggles controls, double-tap seeks/pauses, swipes adjust brightness/volume/position.
        Box(
            Modifier.fillMaxSize()
                .pointerInput(locked, player) {
                    detectTapGestures(
                        onTap = { controls = !controls; touchTick++ },
                        onDoubleTap = { o ->
                            if (locked) return@detectTapGestures
                            when {
                                o.x < size.width / 3f -> { player.seekBack(); indicator = Icons.Rounded.Replay10 to "-10s" }
                                o.x > size.width * 2 / 3f -> { player.seekForward(); indicator = Icons.Rounded.Forward10 to "+10s" }
                                else -> if (player.isPlaying) player.pause() else player.play()
                            }
                        },
                    )
                }
                .pointerInput(locked, player) {
                    if (locked) return@pointerInput
                    var mode = 0 // 1 = seek, 2 = brightness, 3 = volume
                    var startX = 0f
                    var total = androidx.compose.ui.geometry.Offset.Zero
                    var startLevel = 0f
                    var startPos = 0L
                    detectDragGestures(
                        onDragStart = { o -> mode = 0; startX = o.x; total = androidx.compose.ui.geometry.Offset.Zero },
                        onDragEnd = {
                            seekPreview?.let { player.seekTo(it) }
                            seekPreview = null
                            mode = 0
                        },
                        onDragCancel = { seekPreview = null; mode = 0 },
                    ) { change, drag ->
                        change.consume()
                        total += drag
                        if (mode == 0) {
                            mode = if (abs(total.x) > abs(total.y)) 1 else if (startX < size.width / 2f) 2 else 3
                            startLevel = when (mode) { 2 -> activity.brightness(); 3 -> activity.volume(); else -> 0f }
                            startPos = player.currentPosition
                        }
                        when (mode) {
                            1 -> {
                                val target = GestureMath.seekTarget(startPos, total.x, size.width.toFloat(), player.duration.coerceAtLeast(0))
                                seekPreview = target
                                indicator = null to "${Format.time(target)}  (${Format.delta(target - startPos)})"
                            }
                            2 -> {
                                val v = GestureMath.level(startLevel, -total.y, size.height.toFloat())
                                activity.setBrightness(v)
                                indicator = Icons.Rounded.Brightness6 to "${(v * 100).toInt()}%"
                            }
                            3 -> {
                                val v = GestureMath.level(startLevel, -total.y, size.height.toFloat())
                                activity.setVolume(v)
                                indicator = Icons.AutoMirrored.Rounded.VolumeUp to "${(v * 100).toInt()}%"
                            }
                        }
                    }
                },
        )

        if (buffering && error == null) CircularProgressIndicator(Modifier.align(Alignment.Center), color = Accent)

        indicator?.let { (icon, text) ->
            Row(
                Modifier.align(Alignment.Center).clip(RoundedCornerShape(14.dp)).background(Color.Black.copy(alpha = 0.6f)).padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (icon != null) { Icon(icon, null, tint = Color.White); Spacer(Modifier.width(8.dp)) }
                Text(text, color = Color.White, style = MaterialTheme.typography.titleMedium)
            }
        }

        if (controls && locked) {
            IconButton(
                onClick = { locked = false; touchTick++ },
                modifier = Modifier.align(Alignment.CenterStart).padding(24.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.5f)),
            ) { Icon(Icons.Rounded.Lock, "Unlock", tint = Color.White) }
        }

        if (controls && !locked) {
            // Top bar
            Row(
                Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.75f), Color.Transparent))).padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { activity.finish() }) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = Color.White) }
                Text(req.title, color = Color.White, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                IconButton(onClick = { activity.enterPip() }) { Icon(Icons.Rounded.PictureInPictureAlt, "Picture-in-picture", tint = Color.White) }
            }

            // Centre
            if (!buffering || ended) Row(Modifier.align(Alignment.Center), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(36.dp)) {
                RoundButton(Icons.Rounded.Replay10, "Back 10 seconds", 52) { player.seekBack(); touchTick++ }
                RoundButton(if (ended) Icons.Rounded.Replay else if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, if (isPlaying) "Pause" else "Play", 72) {
                    if (ended) { player.seekTo(0); player.play() } else if (player.isPlaying) player.pause() else player.play()
                    touchTick++
                }
                RoundButton(Icons.Rounded.Forward10, "Forward 10 seconds", 52) { player.seekForward(); touchTick++ }
            }

            // Bottom bar
            Column(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))))
                    .navigationBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(Format.time(dragging?.toLong() ?: position), color = Color.White, style = MaterialTheme.typography.labelMedium)
                    Slider(
                        value = (dragging ?: position.toFloat()).coerceIn(0f, duration.toFloat().coerceAtLeast(1f)),
                        onValueChange = { dragging = it; touchTick++ },
                        onValueChangeFinished = { dragging?.let { player.seekTo(it.toLong()) }; dragging = null },
                        valueRange = 0f..duration.toFloat().coerceAtLeast(1f),
                        enabled = duration > 0,
                        colors = SliderDefaults.colors(thumbColor = Accent, activeTrackColor = Accent, inactiveTrackColor = Color.White.copy(alpha = 0.3f)),
                        modifier = Modifier.weight(1f).padding(horizontal = 10.dp),
                    )
                    Text(if (duration > 0) Format.time(duration) else "Live", color = Color.White, style = MaterialTheme.typography.labelMedium)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BarButton(Icons.Rounded.LockOpen, "Lock") { locked = true; touchTick++ }
                    BarButton(Icons.Rounded.AspectRatio, fit.label) { fit = Fit.entries[(fit.ordinal + 1) % Fit.entries.size]; indicator = Icons.Rounded.AspectRatio to fit.label; touchTick++ }
                    BarButton(Icons.Rounded.ScreenRotation, "Rotate") { activity.toggleOrientation(); touchTick++ }
                    Spacer(Modifier.weight(1f))
                    Box {
                        BarButton(Icons.Rounded.Speed, if (speed == 1f) "Speed" else "${speed}x") { speedMenu = true; touchTick++ }
                        DropdownMenu(speedMenu, { speedMenu = false }) {
                            listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 1.75f, 2f).forEach { s ->
                                DropdownMenuItem(text = { Text("${s}x", color = if (s == speed) Accent else Color.Unspecified) }, onClick = {
                                    speed = s; player.playbackParameters = PlaybackParameters(s); speedMenu = false
                                })
                            }
                        }
                    }
                    BarButton(Icons.Rounded.Audiotrack, "Audio") { trackSheet = C.TRACK_TYPE_AUDIO }
                    BarButton(Icons.Rounded.Subtitles, "Subtitles") { trackSheet = C.TRACK_TYPE_TEXT }
                }
            }
        }

        error?.let { reason ->
            Column(
                Modifier.align(Alignment.Center).widthIn(max = 420.dp).padding(24.dp).clip(RoundedCornerShape(18.dp))
                    .background(Color(0xF017171C)).padding(20.dp),
            ) {
                Text("Can't play this video", color = Color.White, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(6.dp))
                Text("Reason: $reason", color = Color(0xFF9B9AA3))
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)) {
                    OutlinedButton(onClick = { activity.finish() }) { Text("Close", color = Color.White) }
                    Button(onClick = { player.prepare(); player.play() }, colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Color(0xFF1A1405))) { Text("Retry") }
                }
            }
        }
    }

    if (trackSheet >= 0) {
        TrackSheet(
            player, trackSheet, subScale,
            onScale = { subScale = it },
            onLoadFile = { subPicker.launch(arrayOf("*/*")) },
            onDismiss = { trackSheet = -1 },
        )
    }
}

@Composable
private fun RoundButton(icon: ImageVector, label: String, sizeDp: Int, onClick: () -> Unit) {
    Box(
        Modifier.size(sizeDp.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.45f)).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, label, tint = Color.White, modifier = Modifier.size((sizeDp * 0.55f).dp)) }
}

@Composable
private fun BarButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        Modifier.clip(RoundedCornerShape(10.dp)).clickable(onClick = onClick).padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, label, tint = Color.White, modifier = Modifier.size(22.dp))
        Text(label, color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall)
    }
}

private data class TrackOption(val label: String, val group: androidx.media3.common.Tracks.Group, val index: Int, val selected: Boolean)

@OptIn(UnstableApi::class)
@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrackSheet(player: ExoPlayer, type: Int, subScale: Float, onScale: (Float) -> Unit, onLoadFile: () -> Unit, onDismiss: () -> Unit) {
    val options = remember(player.currentTracks, type) {
        player.currentTracks.groups.filter { it.type == type }.flatMap { g ->
            (0 until g.length).filter { g.isTrackSupported(it) }.map { i ->
                val f = g.getTrackFormat(i)
                val lang = f.language?.let { java.util.Locale(it).displayLanguage.ifBlank { it } }
                val extra = if (type == C.TRACK_TYPE_AUDIO && f.channelCount > 0) when (f.channelCount) { 1 -> "Mono"; 2 -> "Stereo"; 6 -> "5.1"; 8 -> "7.1"; else -> "${f.channelCount} ch" } else null
                TrackOption(listOfNotNull(f.label ?: lang ?: "Track ${i + 1}", extra).joinToString(" · "), g, i, g.isTrackSelected(i))
            }
        }
    }
    val textOff = type == C.TRACK_TYPE_TEXT && (player.trackSelectionParameters.disabledTrackTypes.contains(C.TRACK_TYPE_TEXT) || options.none { it.selected })

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color(0xFF17171C)) {
        Column(Modifier.padding(horizontal = 20.dp).navigationBarsPadding().padding(bottom = 16.dp)) {
            Text(if (type == C.TRACK_TYPE_AUDIO) "Audio" else "Subtitles", style = MaterialTheme.typography.titleLarge, color = Color.White)
            Spacer(Modifier.height(8.dp))
            LazyColumn(Modifier.height(if (options.size > 5) 280.dp else ((options.size + 1) * 52).dp)) {
                if (type == C.TRACK_TYPE_TEXT) item {
                    TrackRow("Off", textOff) {
                        player.trackSelectionParameters = player.trackSelectionParameters.buildUpon().setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true).build()
                        onDismiss()
                    }
                }
                items(options) { o ->
                    TrackRow(o.label, o.selected && !(type == C.TRACK_TYPE_TEXT && textOff)) {
                        player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
                            .setTrackTypeDisabled(type, false)
                            .setOverrideForType(TrackSelectionOverride(o.group.mediaTrackGroup, o.index))
                            .build()
                        onDismiss()
                    }
                }
                if (options.isEmpty() && type == C.TRACK_TYPE_AUDIO) item { Text("No audio tracks to choose from.", color = Color(0xFF9B9AA3)) }
            }
            if (type == C.TRACK_TYPE_TEXT) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { onDismiss(); onLoadFile() }) { Text("Load subtitle file…", color = Color.White) }
                Spacer(Modifier.height(12.dp))
                Text("Size", color = Color(0xFF9B9AA3), style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0.75f to "Small", 1f to "Normal", 1.35f to "Large", 1.7f to "Huge").forEach { (s, l) ->
                        FilterChip(selected = subScale == s, onClick = { onScale(s) }, label = { Text(l) })
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable(onClick = onClick).padding(vertical = 14.dp, horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = if (selected) Accent else Color.White, modifier = Modifier.weight(1f))
        if (selected) Icon(Icons.Rounded.Check, null, tint = Accent)
    }
}
