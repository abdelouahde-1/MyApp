package com.reel.player

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private object C {
    val Bg = Color(0xFF0E0E11)
    val Surface = Color(0xFF17171C)
    val SurfaceHigh = Color(0xFF212128)
    val Text = Color(0xFFF2F1EE)
    val Muted = Color(0xFF9B9AA3)
    val Accent = Color(0xFFE8B04B)
    val OnAccent = Color(0xFF1A1405)
}

@Composable
fun PlayerTheme(content: @Composable () -> Unit) = MaterialTheme(
    colorScheme = darkColorScheme(
        primary = C.Accent, onPrimary = C.OnAccent, background = C.Bg, onBackground = C.Text,
        surface = C.Bg, onSurface = C.Text, surfaceVariant = C.Surface, onSurfaceVariant = C.Muted,
        surfaceContainer = C.Surface, surfaceContainerHigh = C.SurfaceHigh, secondaryContainer = C.SurfaceHigh,
    ),
    content = content,
)

data class DeviceVideo(val uri: Uri, val name: String, val durationMs: Long, val sizeBytes: Long)

private fun Context.queryVideos(): List<DeviceVideo> {
    val cols = arrayOf(MediaStore.Video.Media._ID, MediaStore.Video.Media.DISPLAY_NAME, MediaStore.Video.Media.DURATION, MediaStore.Video.Media.SIZE)
    val out = mutableListOf<DeviceVideo>()
    contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, cols, null, null, "${MediaStore.Video.Media.DATE_ADDED} DESC")?.use { c ->
        while (c.moveToNext()) {
            val id = c.getLong(0)
            out += DeviceVideo(ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id), c.getString(1) ?: "Video", c.getLong(2), c.getLong(3))
        }
    }
    return out
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT), navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT))
        super.onCreate(savedInstanceState)
        setContent { PlayerTheme { Home() } }
    }
}

fun Context.play(uri: Uri, title: String? = null) {
    startActivity(Intent(this, PlayerActivity::class.java).setData(uri).apply { title?.let { putExtra("title", it) } })
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Home() {
    val context = LocalContext.current
    val history = remember { HistoryStore.get(context) }
    val recent by history.items.collectAsState()
    var linkDialog by remember { mutableStateOf(false) }
    val videoPermission = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_VIDEO else Manifest.permission.READ_EXTERNAL_STORAGE
    var granted by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, videoPermission) == PackageManager.PERMISSION_GRANTED) }
    var videos by remember { mutableStateOf<List<DeviceVideo>>(emptyList()) }
    val thumbs = remember { ImageLoader.Builder(context).components { add(VideoFrameDecoder.Factory()) }.build() }

    val permissionAsk = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it }
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            // Keep access so the file can be reopened from "Continue watching".
            runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            context.play(uri)
        }
    }
    LaunchedEffect(granted) { if (granted) videos = withContext(Dispatchers.IO) { runCatching { context.queryVideos() }.getOrDefault(emptyList()) } }

    LazyColumn(Modifier.fillMaxSize().background(C.Bg), contentPadding = PaddingValues(bottom = 32.dp)) {
        item {
            Text("Reel Player", style = MaterialTheme.typography.headlineMedium, color = C.Text, modifier = Modifier.statusBarsPadding().padding(start = 20.dp, top = 16.dp, bottom = 16.dp))
            Row(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BigAction(Icons.Rounded.Folder, "Open file", Modifier.weight(1f)) { filePicker.launch(arrayOf("video/*", "audio/*")) }
                BigAction(Icons.Rounded.Link, "Open link", Modifier.weight(1f)) { linkDialog = true }
            }
        }
        val continuing = recent.filter { !it.finished }
        if (continuing.isNotEmpty()) {
            item {
                Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 24.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Continue watching", style = MaterialTheme.typography.titleMedium, color = C.Text, modifier = Modifier.weight(1f))
                    TextButton(onClick = { history.clear() }) { Text("Clear") }
                }
            }
            items(continuing, key = { "h-" + it.uri }) { e ->
                Row(Modifier.fillMaxWidth().clickable { context.play(Uri.parse(e.uri), e.title) }.padding(start = 20.dp, end = 4.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Thumb(Uri.parse(e.uri), thumbs)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(e.title, color = C.Text, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(if (e.durationMs > 0) "${Format.time(e.positionMs)} / ${Format.time(e.durationMs)}" else Format.time(e.positionMs), color = C.Muted, style = MaterialTheme.typography.labelSmall)
                        Spacer(Modifier.height(6.dp))
                        LinearProgressIndicator(progress = { e.fraction }, Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)), color = C.Accent, trackColor = C.SurfaceHigh, drawStopIndicator = {}, gapSize = 0.dp)
                    }
                    IconButton(onClick = { history.remove(e.uri) }) { Icon(Icons.Rounded.Close, "Remove", tint = C.Muted) }
                }
            }
        }
        item {
            Text("On this device", style = MaterialTheme.typography.titleMedium, color = C.Text, modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 8.dp))
            if (!granted) Column(Modifier.padding(horizontal = 20.dp)) {
                Text("Allow access to show the videos saved on your phone.", color = C.Muted)
                Spacer(Modifier.height(8.dp))
                Button(onClick = { permissionAsk.launch(videoPermission) }, colors = ButtonDefaults.buttonColors(containerColor = C.Accent, contentColor = C.OnAccent)) { Text("Allow access") }
            } else if (videos.isEmpty()) {
                Text("No videos found.", color = C.Muted, modifier = Modifier.padding(horizontal = 20.dp))
            }
        }
        if (granted) items(videos, key = { "d-" + it.uri }) { v ->
            Row(Modifier.fillMaxWidth().clickable { context.play(v.uri, v.name.substringBeforeLast('.')) }.padding(horizontal = 20.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Thumb(v.uri, thumbs)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(v.name, color = C.Text, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(listOf(Format.time(v.durationMs), "%.1f GB".format(v.sizeBytes / 1e9).takeIf { v.sizeBytes >= 1e9 } ?: "%.0f MB".format(v.sizeBytes / 1e6)).joinToString("  ·  "), color = C.Muted, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }

    if (linkDialog) LinkDialog(onDismiss = { linkDialog = false }) { url -> linkDialog = false; context.play(Uri.parse(url)) }
}

@Composable
private fun BigAction(icon: ImageVector, label: String, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier.clip(RoundedCornerShape(16.dp)).background(C.Surface).clickable(onClick = onClick).padding(vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, null, tint = C.Accent, modifier = Modifier.size(28.dp))
        Spacer(Modifier.height(8.dp))
        Text(label, color = C.Text)
    }
}

@Composable
private fun Thumb(uri: Uri, loader: ImageLoader) {
    Box(Modifier.width(112.dp).aspectRatio(16f / 9f).clip(RoundedCornerShape(10.dp)).background(C.SurfaceHigh), contentAlignment = Alignment.Center) {
        Icon(if (uri.scheme == "content" || uri.scheme == "file") Icons.Rounded.VideoLibrary else Icons.Rounded.Movie, null, tint = C.Muted)
        if (uri.scheme == "content" || uri.scheme == "file") AsyncImage(uri, null, imageLoader = loader, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun LinkDialog(onDismiss: () -> Unit, onOpen: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    val valid = text.trim().let { it.startsWith("http://") || it.startsWith("https://") || it.startsWith("rtsp://") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = C.Surface,
        title = { Text("Open link") },
        text = {
            OutlinedTextField(text, { text = it }, singleLine = true, placeholder = { Text("https://…/video.mp4 or .m3u8") }, isError = text.isNotBlank() && !valid)
        },
        confirmButton = { TextButton(enabled = valid, onClick = { onOpen(text.trim()) }) { Text("Play") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
