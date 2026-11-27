package com.reel.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.reel.app.data.LibraryItem
import com.reel.app.data.LibraryRules
import com.reel.app.data.LibraryState
import com.reel.app.ui.ReelColors
import com.reel.app.ui.components.EmptyState
import com.reel.app.ui.components.LocalApp
import com.reel.app.ui.components.NameDialog
import com.reel.app.ui.components.PosterCard
import com.reel.app.ui.components.ScreenHeader
import com.reel.app.ui.components.rememberQuickActions
import com.reel.app.ui.openDetail

private enum class Shelf(val label: String, val icon: ImageVector) {
    Continue("Continue", Icons.Rounded.PlayCircle),
    Watchlist("Watchlist", Icons.Rounded.Bookmark),
    Favorites("Favorites", Icons.Rounded.Favorite),
    Watched("Watched", Icons.Rounded.CheckCircle),
}

private enum class Sort(val label: String) { Recent("Recently added"), Title("Title A–Z"), Year("Release year") }

private fun LibraryState.shelf(key: String): List<LibraryItem> = when (key) {
    Shelf.Continue.name -> continueWatching
    Shelf.Watchlist.name -> watchlist
    Shelf.Favorites.name -> favorites
    Shelf.Watched.name -> watched
    else -> list(key.removePrefix("list:"))
}

@Composable
fun LibraryScreen(nav: NavController) {
    val app = LocalApp.current
    val lib by app.library.state.collectAsState()
    val quick = rememberQuickActions { nav.openDetail(it) }
    var selectedShelf by rememberSaveable { mutableStateOf(Shelf.Watchlist.name) }
    var typeFilter by rememberSaveable { mutableStateOf<String?>(null) }
    var sort by rememberSaveable { mutableStateOf(Sort.Recent) }
    var sortMenu by remember { mutableStateOf(false) }
    var listMenu by remember { mutableStateOf(false) }
    var dialog by remember { mutableStateOf<String?>(null) } // "new", "rename", "delete"

    // A deleted (or renamed elsewhere) list falls back to the watchlist.
    val shelf = selectedShelf.takeUnless { it.startsWith("list:") && it.removePrefix("list:") !in lib.lists } ?: Shelf.Watchlist.name
    val customList = shelf.removePrefix("list:").takeIf { shelf.startsWith("list:") }

    val all = lib.shelf(shelf)
    val types = all.map { it.type }.distinct().sorted()
    val shown = all.filter { typeFilter == null || it.type == typeFilter }.let { l ->
        when {
            shelf == Shelf.Continue.name -> l // already most recent first
            sort == Sort.Title -> l.sortedBy { it.name.lowercase() }
            sort == Sort.Year -> l.sortedByDescending { it.releaseInfo?.take(4)?.toIntOrNull() ?: 0 }
            shelf == Shelf.Watched.name -> l.sortedByDescending { it.watchedAt }
            else -> l.sortedByDescending { it.addedAt }
        }
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Library") {
            Box {
                IconButton(onClick = { sortMenu = true }) { Icon(Icons.AutoMirrored.Rounded.Sort, "Sort", tint = ReelColors.Muted) }
                DropdownMenu(sortMenu, { sortMenu = false }) {
                    Sort.entries.forEach { s ->
                        DropdownMenuItem(text = { Text(s.label, color = if (s == sort) ReelColors.Accent else ReelColors.Text) }, onClick = { sort = s; sortMenu = false })
                    }
                }
            }
            IconButton(onClick = { nav.navigate("settings") }) { Icon(Icons.Rounded.Settings, "Settings", tint = ReelColors.Muted) }
        }
        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(Shelf.entries) { s ->
                ShelfChip(s.label, lib.shelf(s.name).size, s.icon, shelf == s.name) { selectedShelf = s.name }
            }
            items(lib.lists) { name ->
                ShelfChip(name, lib.list(name).size, Icons.AutoMirrored.Rounded.ViewList, shelf == "list:$name") { selectedShelf = "list:$name" }
            }
            item {
                FilterChip(selected = false, onClick = { dialog = "new" }, label = { Text("New list") }, leadingIcon = { Icon(Icons.Rounded.Add, null, Modifier.size(18.dp)) })
            }
        }
        Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            if (types.size > 1) {
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TypeToggle("All", typeFilter == null) { typeFilter = null }
                    types.forEach { t -> TypeToggle(typeLabel(t), typeFilter == t) { typeFilter = t } }
                }
            } else Spacer(Modifier.weight(1f))
            Text("${shown.size} titles", color = ReelColors.Muted, style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
            if (customList != null) Box {
                IconButton(onClick = { listMenu = true }) { Icon(Icons.Rounded.MoreVert, "List options", tint = ReelColors.Muted) }
                DropdownMenu(listMenu, { listMenu = false }) {
                    DropdownMenuItem(text = { Text("Rename list") }, onClick = { listMenu = false; dialog = "rename" })
                    DropdownMenuItem(text = { Text("Delete list") }, onClick = { listMenu = false; dialog = "delete" })
                }
            } else Spacer(Modifier.width(12.dp))
        }
        if (shown.isEmpty()) {
            val (title, body) = when (shelf) {
                Shelf.Continue.name -> "Nothing in progress" to "Titles you start watching show up here so you can pick up where you left off."
                Shelf.Watchlist.name -> "Your watchlist is empty" to "Long-press any poster, or tap the bookmark on a title, to save it for later."
                Shelf.Favorites.name -> "No favorites yet" to "Tap the heart on a title you love."
                Shelf.Watched.name -> "Nothing watched yet" to "Finished titles are marked automatically. You can also mark them yourself."
                else -> "This list is empty" to "Long-press a poster and pick this list to add titles."
            }
            EmptyState(Shelf.entries.firstOrNull { it.name == shelf }?.icon ?: Icons.AutoMirrored.Rounded.ViewList, title, body, Modifier.padding(top = 40.dp))
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(108.dp),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f),
            ) {
                items(shown, key = { it.id }) { item ->
                    val p = item.toPreview()
                    PosterCard(
                        p, width = null,
                        progress = item.progress?.fraction,
                        caption = if (shelf == Shelf.Continue.name) item.progress?.videoId?.let(::episodeLabel) else null,
                        onClick = { nav.openDetail(p) },
                        onLongClick = { quick(p) },
                    )
                }
            }
        }
    }

    when (dialog) {
        "new" -> NameDialog("New list", "", { dialog = null }) { name ->
            app.library.update { s, _ -> LibraryRules.createList(s, name) }
            selectedShelf = "list:${name.trim()}"; dialog = null
        }
        "rename" -> NameDialog("Rename list", customList.orEmpty(), { dialog = null }) { name ->
            app.library.update { s, _ -> LibraryRules.renameList(s, customList!!, name) }
            selectedShelf = "list:${name.trim()}"; dialog = null
        }
        "delete" -> AlertDialog(
            onDismissRequest = { dialog = null },
            containerColor = ReelColors.Surface,
            title = { Text("Delete \"$customList\"?") },
            text = { Text("The titles stay in your watchlist, favorites and history.") },
            confirmButton = {
                TextButton(onClick = { app.library.update { s, _ -> LibraryRules.deleteList(s, customList!!) }; dialog = null }) { Text("Delete", color = ReelColors.Love) }
            },
            dismissButton = { TextButton(onClick = { dialog = null }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun ShelfChip(label: String, count: Int, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(if (count > 0) "$label  $count" else label) },
        leadingIcon = { Icon(icon, null, Modifier.size(18.dp)) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = ReelColors.Accent,
            selectedLabelColor = ReelColors.OnAccent,
            selectedLeadingIconColor = ReelColors.OnAccent,
        ),
    )
}

@Composable
private fun TypeToggle(label: String, selected: Boolean, onClick: () -> Unit) {
    TextButton(onClick = onClick, contentPadding = PaddingValues(horizontal = 8.dp)) {
        Text(label, color = if (selected) ReelColors.Text else ReelColors.Muted, style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
    }
}
