package com.reel.app.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.reel.app.ReelApp
import com.reel.app.data.LibraryItem
import com.reel.app.data.LibraryRules
import com.reel.app.data.MetaPreview
import com.reel.app.ui.ReelColors

val LocalApp = staticCompositionLocalOf<ReelApp> { error("no app") }

@Composable
fun ScreenHeader(title: String, modifier: Modifier = Modifier, actions: @Composable () -> Unit = {}) {
    Row(
        modifier.fillMaxWidth().statusBarsPadding().padding(start = 20.dp, end = 8.dp, top = 12.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = androidx.compose.material3.MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
        actions()
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier, trailing: @Composable () -> Unit = {}) {
    Row(modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text, style = androidx.compose.material3.MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        trailing()
    }
}

@Composable
fun EmptyState(icon: ImageVector, title: String, body: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = ReelColors.Muted, modifier = Modifier.size(40.dp))
        Spacer(Modifier.height(12.dp))
        Text(title, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text(body, color = ReelColors.Muted, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

/** Poster with library badges. Tap opens, long-press opens quick actions. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PosterCard(
    preview: MetaPreview,
    modifier: Modifier = Modifier,
    width: Dp? = 116.dp,
    progress: Float? = null,
    caption: String? = null,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val lib by LocalApp.current.library.state.collectAsState()
    val item = lib[preview.id]
    val haptics = LocalHapticFeedback.current
    // The whole card (poster + title) is the touch target.
    Column(
        (if (width != null) modifier.width(width) else modifier).clip(RoundedCornerShape(12.dp))
            .combinedClickable(onClick = onClick, onLongClick = { haptics.performHapticFeedback(HapticFeedbackType.LongPress); onLongClick() }),
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(2f / 3f).clip(RoundedCornerShape(12.dp)).background(ReelColors.Surface)) {
            if (preview.poster != null) {
                AsyncImage(preview.poster, preview.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else {
                Box(Modifier.fillMaxSize().padding(10.dp), contentAlignment = Alignment.Center) {
                    Text(preview.name, color = ReelColors.Muted, maxLines = 4, overflow = TextOverflow.Ellipsis)
                }
            }
            if (item != null) Badges(item, Modifier.align(Alignment.TopEnd).padding(6.dp))
            if (progress != null && progress > 0f) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(3.dp),
                    color = ReelColors.Accent,
                    trackColor = Color.Black.copy(alpha = 0.5f),
                    drawStopIndicator = {},
                    gapSize = 0.dp,
                )
            }
        }
        Text(
            preview.name, maxLines = 1, overflow = TextOverflow.Ellipsis,
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 6.dp, start = 2.dp),
        )
        val sub = caption ?: preview.releaseInfo
        if (sub != null) Text(sub, color = ReelColors.Muted, style = androidx.compose.material3.MaterialTheme.typography.labelSmall, modifier = Modifier.padding(start = 2.dp), maxLines = 1)
    }
}

@Composable
private fun Badges(item: LibraryItem, modifier: Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        @Composable fun dot(icon: ImageVector, tint: Color) = Box(
            Modifier.size(22.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.6f)), contentAlignment = Alignment.Center,
        ) { Icon(icon, null, tint = tint, modifier = Modifier.size(14.dp)) }
        if (item.favorite) dot(Icons.Rounded.Favorite, ReelColors.Love)
        if (item.watchlist) dot(Icons.Rounded.Bookmark, ReelColors.Accent)
        if (item.watched) dot(Icons.Rounded.Check, ReelColors.Good)
    }
}

@Composable
fun PosterRow(
    title: String,
    items: List<MetaPreview>,
    onOpen: (MetaPreview) -> Unit,
    onLongPress: (MetaPreview) -> Unit,
    trailing: @Composable () -> Unit = {},
) {
    SectionTitle(title, trailing = trailing)
    LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(items, key = { it.id }) { p -> PosterCard(p, onClick = { onOpen(p) }, onLongClick = { onLongPress(p) }) }
    }
    Spacer(Modifier.height(12.dp))
}

/** The library actions for one title: watchlist, favorite, watched, and the user's own lists. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuickActionsSheet(preview: MetaPreview, onDismiss: () -> Unit, onOpen: (() -> Unit)? = null) {
    val app = LocalApp.current
    val lib by app.library.state.collectAsState()
    val item = lib[preview.id]
    var newList by remember { mutableStateOf(false) }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = ReelColors.Surface) {
        Column(Modifier.padding(horizontal = 20.dp).navigationBarsPadding().padding(bottom = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(preview.poster, null, contentScale = ContentScale.Crop, modifier = Modifier.width(48.dp).aspectRatio(2f / 3f).clip(RoundedCornerShape(8.dp)).background(ReelColors.SurfaceHigh))
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(preview.name, style = androidx.compose.material3.MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(listOfNotNull(if (preview.type == "series") "Series" else if (preview.type == "movie") "Movie" else preview.type.replaceFirstChar { it.uppercase() }, preview.releaseInfo).joinToString(" · "), color = ReelColors.Muted)
                }
                if (onOpen != null) TextButton(onClick = { onDismiss(); onOpen() }) { Text("Open") }
            }
            Spacer(Modifier.height(16.dp))
            ActionLine(if (item?.watchlist == true) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder, "Watchlist", item?.watchlist == true, ReelColors.Accent) {
                app.library.update { s, now -> LibraryRules.toggleWatchlist(s, preview, now) }
            }
            ActionLine(if (item?.favorite == true) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, "Favorite", item?.favorite == true, ReelColors.Love) {
                app.library.update { s, now -> LibraryRules.toggleFavorite(s, preview, now) }
            }
            ActionLine(if (item?.watched == true) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked, "Watched", item?.watched == true, ReelColors.Good) {
                val watched = item?.watched != true
                app.library.update { s, now -> LibraryRules.setWatched(s, preview, watched, now) }
            }
            if (item?.progress != null) {
                TextButton(onClick = { app.library.update { s, now -> LibraryRules.clearProgress(s, preview, now) } }) { Text("Remove from Continue watching", color = ReelColors.Muted) }
            }
            Spacer(Modifier.height(12.dp))
            Text("My lists", style = androidx.compose.material3.MaterialTheme.typography.labelSmall, color = ReelColors.Muted)
            Spacer(Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                lib.lists.forEach { name ->
                    val inList = item?.lists?.contains(name) == true
                    FilterChip(
                        selected = inList,
                        onClick = { app.library.update { s, now -> LibraryRules.toggleInList(s, preview, name, now) } },
                        label = { Text(name) },
                        leadingIcon = if (inList) ({ Icon(Icons.Rounded.Check, null, Modifier.size(16.dp)) }) else null,
                    )
                }
                FilterChip(selected = false, onClick = { newList = true }, label = { Text("New list") }, leadingIcon = { Icon(Icons.Rounded.Add, null, Modifier.size(16.dp)) })
            }
        }
    }
    if (newList) {
        NameDialog("New list", "", onDismiss = { newList = false }) { name ->
            app.library.update { s, now -> LibraryRules.toggleInList(LibraryRules.createList(s, name), preview, name.trim(), now) }
            newList = false
        }
    }
}

@Composable
private fun ActionLine(icon: ImageVector, label: String, on: Boolean, tint: Color, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = if (on) tint else ReelColors.Muted)
        Spacer(Modifier.width(16.dp))
        Text(label, modifier = Modifier.weight(1f))
        Text(if (on) "On" else "Off", color = if (on) tint else ReelColors.Muted, style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun NameDialog(title: String, initial: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by rememberSaveable { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ReelColors.Surface,
        title = { Text(title) },
        text = { OutlinedTextField(text, { text = it }, singleLine = true, placeholder = { Text("Name") }) },
        confirmButton = { TextButton(onClick = { onConfirm(text) }, enabled = text.isNotBlank()) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

/** Shared state for "which poster is the quick-actions sheet open for". */
@Composable
fun rememberQuickActions(onOpen: (MetaPreview) -> Unit): (MetaPreview) -> Unit {
    var target by remember { mutableStateOf<MetaPreview?>(null) }
    target?.let { p -> QuickActionsSheet(p, onDismiss = { target = null }, onOpen = { onOpen(p) }) }
    return { target = it }
}

@Composable
fun PlaceholderPoster(modifier: Modifier = Modifier) {
    Box(modifier.aspectRatio(2f / 3f).clip(RoundedCornerShape(12.dp)).background(Brush.verticalGradient(listOf(ReelColors.Surface, ReelColors.SurfaceHigh))), contentAlignment = Alignment.Center) {
        Icon(Icons.Rounded.Movie, null, tint = ReelColors.Line)
    }
}
