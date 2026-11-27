package com.reel.app.ui.screens

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.reel.app.data.Addon
import com.reel.app.data.LibraryRules
import com.reel.app.ui.ReelColors
import com.reel.app.ui.components.LocalApp
import com.reel.app.ui.components.SectionTitle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val subtitleLanguages = listOf("" to "Off", "eng" to "English", "fre" to "French", "ara" to "Arabic", "spa" to "Spanish", "ger" to "German", "por" to "Portuguese", "ita" to "Italian", "tur" to "Turkish")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(nav: NavController) {
    val app = LocalApp.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val addons by app.addons.addons.collectAsState()
    val settings by app.settings.state.collectAsState()
    val lib by app.library.state.collectAsState()
    var url by remember { mutableStateOf("") }
    var installing by remember { mutableStateOf(false) }
    var confirmRemove by remember { mutableStateOf<Addon?>(null) }
    var confirmClear by remember { mutableStateOf(false) }

    fun toast(msg: String) = Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

    val importer = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val result = runCatching {
                val text = withContext(Dispatchers.IO) { context.contentResolver.openInputStream(uri)!!.bufferedReader().use { it.readText() } }
                app.library.importJson(text)
            }
            toast(if (result.isSuccess) "Library imported" else "That file isn't a Reel library backup")
        }
    }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 40.dp)) {
        item {
            Row(Modifier.fillMaxWidth().statusBarsPadding().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
                Text("Settings", style = MaterialTheme.typography.headlineSmall)
            }
        }

        item { SectionTitle("Add-ons") }
        item {
            Text(
                "Add-ons supply catalogs, details, subtitles and stream sources. Paste an add-on link (ending in manifest.json) to install it. Only install add-ons you trust and that give access to content you are allowed to watch.",
                color = ReelColors.Muted, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 20.dp),
            )
            Spacer(Modifier.height(10.dp))
            Row(Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    url, { url = it }, Modifier.weight(1f), singleLine = true, placeholder = { Text("https://…/manifest.json") },
                    shape = RoundedCornerShape(14.dp),
                    trailingIcon = {
                        IconButton(onClick = {
                            val clip = (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).primaryClip
                            clip?.getItemAt(0)?.coerceToText(context)?.toString()?.let { url = it.trim() }
                        }) { Icon(Icons.Rounded.ContentPaste, "Paste") }
                    },
                )
                Spacer(Modifier.width(10.dp))
                Button(
                    enabled = url.isNotBlank() && !installing,
                    onClick = {
                        installing = true
                        scope.launch {
                            runCatching { app.addons.install(url) }
                                .onSuccess { toast("Installed $it"); url = "" }
                                .onFailure { toast("Couldn't install: ${it.message}") }
                            installing = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ReelColors.Accent, contentColor = ReelColors.OnAccent),
                ) { Text(if (installing) "…" else "Install") }
            }
            Spacer(Modifier.height(8.dp))
        }
        if (addons.isEmpty()) item {
            Column(Modifier.padding(horizontal = 20.dp)) {
                Text("No add-ons installed.", color = ReelColors.Muted)
                TextButton(onClick = { scope.launch { runCatching { app.addons.install(com.reel.app.data.AddonRepository.DEFAULT_ADDONS.first()) }.onFailure { toast("Offline? ${it.message}") } } }) {
                    Text("Restore the default catalog add-on")
                }
            }
        }
        itemsIndexed(addons, key = { _, a -> a.transportUrl }) { i, a ->
            AddonRow(
                a, first = i == 0, last = i == addons.lastIndex,
                onToggle = { app.addons.setEnabled(a.transportUrl, it) },
                onMove = { app.addons.move(a.transportUrl, it) },
                onRemove = { confirmRemove = a },
            )
        }

        item { SectionTitle("Playback") }
        item {
            SwitchRow("Use another video player", "Open streams in VLC, MX Player or similar instead of Reel's player.", settings.externalPlayer) { v -> app.settings.update { it.copy(externalPlayer = v) } }
            SwitchRow("Offer next episode", "When an episode ends, suggest the next one.", settings.autoplayNext) { v -> app.settings.update { it.copy(autoplayNext = v) } }
            SwitchRow("Hide torrent sources", "Torrent sources open in another app (such as Stremio or a torrent client), not in Reel's player.", settings.hideUnsupported) { v -> app.settings.update { it.copy(hideUnsupported = v) } }
            Text("Default quality", modifier = Modifier.padding(start = 20.dp, top = 12.dp))
            Text("The source list opens on this quality when it's available.", style = MaterialTheme.typography.bodySmall, color = ReelColors.Muted, modifier = Modifier.padding(start = 20.dp, bottom = 6.dp))
            FlowRow(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                com.reel.app.data.Quality.entries.filter { it != com.reel.app.data.Quality.Unknown }.forEach { q ->
                    FilterChip(selected = settings.preferredQuality == q.label, onClick = { app.settings.update { it.copy(preferredQuality = q.label) } }, label = { Text(q.label) })
                }
            }
            Text("Subtitles", modifier = Modifier.padding(start = 20.dp, top = 12.dp, bottom = 6.dp))
            FlowRow(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                subtitleLanguages.forEach { (code, label) ->
                    FilterChip(selected = settings.subtitleLang == code, onClick = { app.settings.update { it.copy(subtitleLang = code) } }, label = { Text(label) })
                }
            }
        }

        item { SectionTitle("Library") }
        item {
            Text(
                "${lib.items.size} titles · ${lib.watchlist.size} in watchlist · ${lib.favorites.size} favorites · ${lib.watched.size} watched · ${lib.lists.size} lists",
                color = ReelColors.Muted, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 20.dp),
            )
            Spacer(Modifier.height(10.dp))
            Row(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = { shareBackup(context, app.library.exportJson()) }) { Text("Back up") }
                OutlinedButton(onClick = { importer.launch(arrayOf("application/json", "text/plain", "*/*")) }) { Text("Restore") }
                TextButton(onClick = { confirmClear = true }) { Text("Clear history", color = ReelColors.Love) }
            }
        }
        item {
            Spacer(Modifier.height(24.dp))
            Text("Reel 1.0", color = ReelColors.Muted, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 20.dp))
        }
    }

    confirmRemove?.let { a ->
        AlertDialog(
            onDismissRequest = { confirmRemove = null },
            containerColor = ReelColors.Surface,
            title = { Text("Remove ${a.manifest.name}?") },
            text = { Text("Its catalogs and sources will disappear. Your library is not affected.") },
            confirmButton = { TextButton(onClick = { app.addons.remove(a.transportUrl); confirmRemove = null }) { Text("Remove", color = ReelColors.Love) } },
            dismissButton = { TextButton(onClick = { confirmRemove = null }) { Text("Cancel") } },
        )
    }
    if (confirmClear) AlertDialog(
        onDismissRequest = { confirmClear = false },
        containerColor = ReelColors.Surface,
        title = { Text("Clear watch history?") },
        text = { Text("Removes Continue watching and watched marks. Watchlist, favorites and lists are kept.") },
        confirmButton = {
            TextButton(onClick = {
                app.library.update { s, now ->
                    s.items.values.fold(s) { acc, item ->
                        val p = item.toPreview()
                        val cleared = LibraryRules.clearProgress(acc, p, now)
                        if (item.watched || item.watchedVideos.isNotEmpty()) LibraryRules.setWatched(cleared, p, false, now, item.watchedVideos) else cleared
                    }
                }
                confirmClear = false
            }) { Text("Clear", color = ReelColors.Love) }
        },
        dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("Cancel") } },
    )
}

private fun shareBackup(context: Context, json: String) {
    val dir = context.cacheDir.resolve("exports").apply { mkdirs() }
    val file = dir.resolve("reel-library.json").apply { writeText(json) }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
    val send = Intent(Intent.ACTION_SEND).setType("application/json").putExtra(Intent.EXTRA_STREAM, uri).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    context.startActivity(Intent.createChooser(send, "Save library backup"))
}

@Composable
private fun AddonRow(a: Addon, first: Boolean, last: Boolean, onToggle: (Boolean) -> Unit, onMove: (Int) -> Unit, onRemove: () -> Unit) {
    val m = a.manifest
    val provides = m.resources.map { it.name }.distinct().joinToString(" · ") { if (it == "stream") "streams" else if (it == "catalog") "catalogs" else it }
    Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(ReelColors.SurfaceHigh), contentAlignment = Alignment.Center) {
            if (m.logo != null) AsyncImage(m.logo, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            else Icon(Icons.Rounded.Extension, null, tint = ReelColors.Muted)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("${m.name}  ${m.version}", maxLines = 1, overflow = TextOverflow.Ellipsis, color = if (a.enabled) ReelColors.Text else ReelColors.Muted)
            Text(provides, style = MaterialTheme.typography.labelSmall, color = ReelColors.Muted)
            if (m.description.isNotBlank()) Text(m.description, style = MaterialTheme.typography.bodySmall, color = ReelColors.Muted, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        Column {
            IconButton(onClick = { onMove(-1) }, enabled = !first, modifier = Modifier.size(32.dp)) { Icon(Icons.Rounded.KeyboardArrowUp, "Move up") }
            IconButton(onClick = { onMove(1) }, enabled = !last, modifier = Modifier.size(32.dp)) { Icon(Icons.Rounded.KeyboardArrowDown, "Move down") }
        }
        Switch(a.enabled, onToggle, colors = SwitchDefaults.colors(checkedTrackColor = ReelColors.Accent, checkedThumbColor = ReelColors.OnAccent))
        IconButton(onClick = onRemove) { Icon(Icons.Rounded.Delete, "Remove", tint = ReelColors.Muted) }
    }
}

@Composable
private fun SwitchRow(title: String, body: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().clickable { onChange(!checked) }.padding(horizontal = 20.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title)
            Text(body, style = MaterialTheme.typography.bodySmall, color = ReelColors.Muted)
        }
        Spacer(Modifier.width(12.dp))
        Switch(checked, onChange, colors = SwitchDefaults.colors(checkedTrackColor = ReelColors.Accent, checkedThumbColor = ReelColors.OnAccent))
    }
}
