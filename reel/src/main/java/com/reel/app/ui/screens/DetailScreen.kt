package com.reel.app.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Theaters
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.reel.app.Playback
import com.reel.app.ReelApp
import com.reel.app.data.LibraryRules
import com.reel.app.data.Meta
import com.reel.app.data.MetaPreview
import com.reel.app.data.Stream
import com.reel.app.data.StreamGroup
import com.reel.app.data.Subtitle
import com.reel.app.data.Video
import com.reel.app.ui.ReelColors
import com.reel.app.ui.components.EmptyState
import com.reel.app.ui.components.LocalApp
import com.reel.app.ui.components.QuickActionsSheet
import java.time.LocalDate
import java.time.OffsetDateTime

/** Videos that have been released (unknown dates count as released). */
fun releasedVideos(meta: Meta): List<Video> {
    val today = LocalDate.now()
    return meta.videos.filter { v ->
        (v.season ?: 1) != 0 && v.released?.let { r ->
            runCatching { OffsetDateTime.parse(r).toLocalDate() }.getOrNull()
                ?: runCatching { LocalDate.parse(r.take(10)) }.getOrNull()
        }?.let { !it.isAfter(today) } != false
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailScreen(nav: NavController, type: String, id: String, savedState: SavedStateHandle) {
    val app = LocalApp.current
    val lib by app.library.state.collectAsState()
    val item = lib[id]
    val meta by produceState<Result<Meta?>?>(null, type, id) { value = runCatching { app.client.meta(app.addons.enabled, type, id) } }
    val m = meta?.getOrNull()
    val preview: MetaPreview = m?.preview ?: item?.toPreview() ?: MetaPreview(id, type, "", null, "poster", null, null, null, null, emptyList())
    var streamsFor by remember { mutableStateOf<Video?>(null) }
    var lists by remember { mutableStateOf(false) }

    // When the player finishes an episode it asks us to offer the next one.
    val openVideo by savedState.getStateFlow<String?>("openVideo", null).collectAsState()
    LaunchedEffect(openVideo, m) {
        val vid = openVideo ?: return@LaunchedEffect
        if (m == null) return@LaunchedEffect
        streamsFor = m.videos.firstOrNull { it.id == vid }
        savedState["openVideo"] = null
    }

    val seasons = m?.seasons.orEmpty()
    val resumeVideo = item?.progress?.videoId
    var season by rememberSaveable(m?.id) {
        mutableStateOf(m?.videos?.firstOrNull { it.id == resumeVideo }?.season ?: seasons.firstOrNull { it != 0 } ?: seasons.firstOrNull())
    }
    val isSeries = seasons.isNotEmpty() || (m?.videos?.size ?: 0) > 1
    val released = m?.let(::releasedVideos).orEmpty()

    /** What the big button plays: the in-progress video, else the first unwatched episode, else the movie. */
    val primary: Video? = when {
        m == null -> null
        !isSeries -> Video(m.id, m.name, null, null, null, null, null)
        else -> m.videos.firstOrNull { it.id == resumeVideo }
            ?: m.seasons.filter { it != 0 }.flatMap { m.episodes(it) }.firstOrNull { it.id !in item?.watchedVideos.orEmpty() }
            ?: m.videos.firstOrNull()
    }
    val primaryLabel = when {
        primary == null -> "Play"
        item?.progress?.videoId == primary.id && (item.progress.positionMs) > 0 -> if (isSeries) "Resume ${primary.label}" else "Resume"
        isSeries -> "Play ${primary.label}"
        else -> "Play"
    }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 32.dp)) {
        item {
            Box(Modifier.fillMaxWidth().aspectRatio(16f / 10f)) {
                AsyncImage(preview.background ?: preview.poster, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0f to Color.Black.copy(alpha = 0.35f), 0.4f to Color.Transparent, 1f to ReelColors.Bg)))
                IconButton(
                    onClick = { nav.popBackStack() },
                    modifier = Modifier.statusBarsPadding().padding(8.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.4f)),
                ) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = Color.White) }
            }
        }
        item {
            Column(Modifier.padding(horizontal = 20.dp)) {
                if (m?.logo != null) {
                    AsyncImage(m.logo, m.name, contentScale = ContentScale.Fit, alignment = Alignment.CenterStart, modifier = Modifier.height(64.dp).fillMaxWidth(0.7f))
                    Spacer(Modifier.height(6.dp))
                }
                Text(preview.name, style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val bits = listOfNotNull(preview.releaseInfo, m?.runtime, if (isSeries && seasons.isNotEmpty()) "${seasons.count { it != 0 }} seasons" else null)
                    Text(bits.joinToString("  ·  "), color = ReelColors.Muted)
                    preview.imdbRating?.let {
                        Spacer(Modifier.width(10.dp))
                        Icon(Icons.Rounded.Star, null, tint = ReelColors.Accent, modifier = Modifier.size(16.dp))
                        Text(" $it", color = ReelColors.Muted)
                    }
                }
                if (preview.genres.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        preview.genres.take(5).forEach { g ->
                            Text(g, style = MaterialTheme.typography.labelSmall, modifier = Modifier.clip(RoundedCornerShape(50)).background(ReelColors.Surface).padding(horizontal = 10.dp, vertical = 5.dp))
                        }
                    }
                }
                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = { streamsFor = primary },
                    enabled = primary != null,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ReelColors.Accent, contentColor = ReelColors.OnAccent),
                ) {
                    if (meta == null) CircularProgressIndicator(Modifier.size(20.dp), color = ReelColors.OnAccent, strokeWidth = 2.dp)
                    else { Icon(Icons.Rounded.PlayArrow, null); Spacer(Modifier.width(6.dp)); Text(primaryLabel, style = MaterialTheme.typography.titleMedium) }
                }
                item?.progress?.takeIf { it.videoId == primary?.id && it.positionMs > 0 }?.let { pr ->
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(progress = { pr.fraction }, Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)), color = ReelColors.Accent, trackColor = ReelColors.Surface, drawStopIndicator = {}, gapSize = 0.dp)
                }
                Spacer(Modifier.height(14.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    ToggleAction(if (item?.watchlist == true) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder, "Watchlist", item?.watchlist == true, ReelColors.Accent) {
                        app.library.update { s, now -> LibraryRules.toggleWatchlist(s, preview, now) }
                    }
                    ToggleAction(if (item?.favorite == true) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, "Favorite", item?.favorite == true, ReelColors.Love) {
                        app.library.update { s, now -> LibraryRules.toggleFavorite(s, preview, now) }
                    }
                    ToggleAction(if (item?.watched == true) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked, "Watched", item?.watched == true, ReelColors.Good) {
                        val watched = item?.watched != true
                        app.library.update { s, now -> LibraryRules.setWatched(s, preview, watched, now, released.map { it.id }) }
                    }
                    ToggleAction(Icons.AutoMirrored.Rounded.PlaylistAdd, if (item?.lists.isNullOrEmpty()) "Lists" else "${item!!.lists.size} lists", !item?.lists.isNullOrEmpty(), ReelColors.Accent) { lists = true }
                    val context = LocalContext.current
                    m?.trailerYtIds?.firstOrNull()?.let { yt ->
                        ToggleAction(Icons.Rounded.Theaters, "Trailer", false, ReelColors.Accent) { openYouTube(context, yt) }
                    }
                }
                preview.description?.takeIf { it.isNotBlank() }?.let { Spacer(Modifier.height(16.dp)); ExpandableText(it) }
                m?.cast?.takeIf { it.isNotEmpty() }?.let { Spacer(Modifier.height(12.dp)); Credit("Cast", it.take(6).joinToString(", ")) }
                m?.director?.takeIf { it.isNotEmpty() }?.let { Credit("Director", it.joinToString(", ")) }
                if (meta?.getOrNull() == null && meta != null) {
                    Spacer(Modifier.height(12.dp))
                    Text("Couldn't load details. Check your connection and add-ons.", color = ReelColors.Love)
                }
            }
        }
        if (m != null && isSeries) {
            item {
                Spacer(Modifier.height(20.dp))
                if (seasons.size > 1) LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(seasons) { s -> FilterChip(selected = s == season, onClick = { season = s }, label = { Text(if (s == 0) "Specials" else "Season $s") }) }
                }
                val eps = season?.let { m.episodes(it) } ?: m.videos
                val allWatched = eps.isNotEmpty() && eps.all { it.id in item?.watchedVideos.orEmpty() }
                Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("${eps.size} episodes", color = ReelColors.Muted, modifier = Modifier.weight(1f))
                    TextButton(onClick = {
                        app.library.update { s0, now -> eps.fold(s0) { s, v -> LibraryRules.setVideoWatched(s, preview, v.id, !allWatched, released.map { it.id }, now) } }
                    }) { Text(if (allWatched) "Mark season unwatched" else "Mark season watched") }
                }
            }
            items(season?.let { m.episodes(it) } ?: m.videos, key = { it.id }) { v ->
                EpisodeRow(
                    v,
                    watched = v.id in item?.watchedVideos.orEmpty(),
                    progress = item?.progress?.takeIf { it.videoId == v.id }?.fraction,
                    onClick = { streamsFor = v },
                    onToggleWatched = { w -> app.library.update { s, now -> LibraryRules.setVideoWatched(s, preview, v.id, w, released.map { it.id }, now) } },
                )
            }
        }
    }

    if (lists) QuickActionsSheet(preview, onDismiss = { lists = false })
    streamsFor?.let { v -> if (m != null) StreamsSheet(nav, m, v, onDismiss = { streamsFor = null }) }
}

@Composable
private fun ToggleAction(icon: ImageVector, label: String, on: Boolean, tint: Color, onClick: () -> Unit) {
    Column(
        Modifier.clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, label, tint = if (on) tint else ReelColors.Text)
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = if (on) tint else ReelColors.Muted)
    }
}

@Composable
private fun ExpandableText(text: String) {
    var expanded by remember { mutableStateOf(false) }
    Text(
        text, color = ReelColors.Muted, maxLines = if (expanded) Int.MAX_VALUE else 3, overflow = TextOverflow.Ellipsis,
        modifier = Modifier.animateContentSize().clickable { expanded = !expanded },
    )
}

@Composable
private fun Credit(label: String, value: String) {
    Row(Modifier.padding(vertical = 2.dp)) {
        Text("$label  ", style = MaterialTheme.typography.bodySmall)
        Text(value, style = MaterialTheme.typography.bodySmall, color = ReelColors.Muted, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun EpisodeRow(v: Video, watched: Boolean, progress: Float?, onClick: () -> Unit, onToggleWatched: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 20.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(128.dp).aspectRatio(16f / 9f).clip(RoundedCornerShape(10.dp)).background(ReelColors.Surface)) {
            AsyncImage(v.thumbnail, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            if (progress != null && progress > 0f) LinearProgressIndicator(
                progress = { progress }, Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(3.dp),
                color = ReelColors.Accent, trackColor = Color.Black.copy(alpha = 0.5f), drawStopIndicator = {}, gapSize = 0.dp,
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text("${v.episode?.let { "$it. " } ?: ""}${v.title}", maxLines = 2, overflow = TextOverflow.Ellipsis, color = if (watched) ReelColors.Muted else ReelColors.Text)
            v.released?.take(10)?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = ReelColors.Muted) }
            v.overview?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = ReelColors.Muted, maxLines = 2, overflow = TextOverflow.Ellipsis) }
        }
        IconButton(onClick = { onToggleWatched(!watched) }) {
            Icon(
                if (watched) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                if (watched) "Mark unwatched" else "Mark watched",
                tint = if (watched) ReelColors.Good else ReelColors.Line,
            )
        }
    }
}

fun openYouTube(context: Context, ytId: String) = openUrl(context, "https://www.youtube.com/watch?v=$ytId")

fun openUrl(context: Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No app can open this link", Toast.LENGTH_SHORT).show()
    }
}

fun openInExternalPlayer(context: Context, url: String, title: String) {
    val intent = Intent(Intent.ACTION_VIEW).setDataAndType(Uri.parse(url), "video/*").putExtra("title", title)
    try {
        context.startActivity(Intent.createChooser(intent, "Open with"))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No video player installed", Toast.LENGTH_SHORT).show()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StreamsSheet(nav: NavController, meta: Meta, video: Video, onDismiss: () -> Unit) {
    val app = LocalApp.current
    val context = LocalContext.current
    val settings by app.settings.state.collectAsState()
    val lib by app.library.state.collectAsState()
    val groups by produceState<List<StreamGroup>?>(null, video.id) { value = app.client.streams(app.addons.enabled, meta.type, video.id) }
    val subs by produceState<List<Subtitle>>(emptyList(), video.id) { value = app.client.subtitles(app.addons.enabled, meta.type, video.id) }
    val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val title = if (video.id == meta.id) meta.name else "${meta.name} · ${video.label}"

    fun play(stream: Stream, external: Boolean) {
        when (stream.kind) {
            Stream.Kind.YouTube -> openYouTube(context, stream.ytId!!)
            Stream.Kind.External -> openUrl(context, stream.externalUrl!!)
            Stream.Kind.Unsupported -> Toast.makeText(context, "This kind of stream isn't supported", Toast.LENGTH_SHORT).show()
            Stream.Kind.Direct -> if (external) {
                openInExternalPlayer(context, stream.url!!, title)
            } else {
                val progress = lib[meta.id]?.progress?.takeIf { it.videoId == video.id }
                (context.applicationContext as ReelApp).let { a ->
                    a.pendingPlayback = Playback(meta.id, meta.type, video.id, meta.name, if (video.id == meta.id) null else "${video.label} · ${video.title}", stream, progress?.positionMs ?: 0)
                    a.pendingSubtitles = subs
                }
                onDismiss()
                nav.navigate("player")
            }
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheet, containerColor = ReelColors.Surface) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding()) {
            Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 20.dp), maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text("Choose a source", color = ReelColors.Muted, modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))
            val g = groups
            when {
                g == null -> Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = ReelColors.Accent) }
                g.isEmpty() -> {
                    EmptyState(Icons.Rounded.Extension, "No stream sources", "Your add-ons only provide catalogs. Install an add-on that provides streams in Settings → Add-ons.")
                    TextButton(onClick = { onDismiss(); nav.navigate("settings") }, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Open add-ons") }
                }
                else -> LazyColumn(Modifier.fillMaxWidth().height(520.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
                    g.forEach { group ->
                        val streams = group.streams.filter { !settings.hideUnsupported || it.kind != Stream.Kind.Unsupported }
                        item(key = "h-${group.addonName}") {
                            Text(
                                group.addonName + (group.error?.let { " · unavailable" } ?: if (streams.isEmpty()) " · nothing found" else ""),
                                style = MaterialTheme.typography.labelSmall, color = ReelColors.Muted,
                                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 4.dp),
                            )
                        }
                        items(streams.size, key = { "${group.addonName}-$it" }) { i ->
                            StreamRow(streams[i], settings.externalPlayer, onPlay = { ext -> play(streams[i], ext) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StreamRow(s: Stream, preferExternal: Boolean, onPlay: (external: Boolean) -> Unit) {
    val supported = s.kind != Stream.Kind.Unsupported
    Row(
        Modifier.fillMaxWidth().clickable(enabled = supported) { onPlay(preferExternal) }.padding(start = 20.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(38.dp).clip(CircleShape).background(ReelColors.SurfaceHigh), contentAlignment = Alignment.Center) {
            Icon(
                when (s.kind) { Stream.Kind.Unsupported -> Icons.Rounded.Block; Stream.Kind.Direct -> Icons.Rounded.PlayArrow; else -> Icons.AutoMirrored.Rounded.OpenInNew },
                null, tint = if (supported) ReelColors.Accent else ReelColors.Muted, modifier = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(s.headline, maxLines = 1, overflow = TextOverflow.Ellipsis, color = if (supported) ReelColors.Text else ReelColors.Muted)
            if (s.details.isNotEmpty()) Text(s.details, style = MaterialTheme.typography.bodySmall, color = ReelColors.Muted, maxLines = 3, overflow = TextOverflow.Ellipsis)
            if (!supported) Text("Not playable in Reel", style = MaterialTheme.typography.labelSmall, color = ReelColors.Muted)
        }
        if (s.kind == Stream.Kind.Direct) IconButton(onClick = { onPlay(!preferExternal) }) {
            Icon(Icons.AutoMirrored.Rounded.OpenInNew, if (preferExternal) "Play in Reel" else "Open in another player", tint = ReelColors.Muted)
        }
    }
}
