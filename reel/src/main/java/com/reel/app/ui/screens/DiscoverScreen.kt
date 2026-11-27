package com.reel.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.reel.app.data.MetaPreview
import com.reel.app.ui.ReelColors
import com.reel.app.ui.components.EmptyState
import com.reel.app.ui.components.LocalApp
import com.reel.app.ui.components.PosterCard
import com.reel.app.ui.components.ScreenHeader
import com.reel.app.ui.components.rememberQuickActions
import com.reel.app.ui.openDetail

@Composable
fun ChipRow(options: List<Pair<String, String>>, selected: String?, onSelect: (String) -> Unit) {
    LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(options, key = { it.first }) { (key, label) ->
            FilterChip(selected = key == selected, onClick = { onSelect(key) }, label = { Text(label) })
        }
    }
}

@Composable
fun DiscoverScreen(nav: NavController, initialType: String?, initialAddon: String?, initialCatalog: String?) {
    val app = LocalApp.current
    val addons by app.addons.addons.collectAsState()
    val quick = rememberQuickActions { nav.openDetail(it) }
    val all = addons.filter { it.enabled }.flatMap { a -> a.manifest.catalogs.filter { it.browsable }.map { a to it } }
    val types = all.map { it.second.type }.distinct()

    var type by rememberSaveable { mutableStateOf(initialType) }
    val currentType = type?.takeIf { it in types } ?: types.firstOrNull()
    val catalogs = all.filter { it.second.type == currentType }
    var catalogKey by rememberSaveable { mutableStateOf(initialAddon?.let { "$it|$initialCatalog" }) }
    val current = catalogs.firstOrNull { "${it.first.transportUrl}|${it.second.id}" == catalogKey } ?: catalogs.firstOrNull()
    var genre by rememberSaveable { mutableStateOf<String?>(null) }

    val items = remember { mutableStateListOf<MetaPreview>() }
    var loading by remember { mutableStateOf(false) }
    var endReached by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var page by remember { mutableStateOf(0) }
    val grid = rememberLazyGridState()

    val key = "${current?.first?.transportUrl}|${current?.second?.type}|${current?.second?.id}|$genre"
    LaunchedEffect(key) { items.clear(); endReached = false; error = null; page = 0 }
    LaunchedEffect(key, page) {
        val (a, c) = current ?: return@LaunchedEffect
        loading = true
        val extra = buildMap {
            genre?.let { put("genre", it) }
            if (items.isNotEmpty()) put("skip", items.size.toString())
        }
        runCatching { app.client.catalog(a, c, extra) }
            .onSuccess { new ->
                val fresh = new.filter { n -> items.none { it.id == n.id } }
                items.addAll(fresh)
                if (fresh.isEmpty() || !c.supportsSkip) endReached = true
            }
            .onFailure { error = it.message; endReached = true }
        loading = false
    }
    val nearEnd by remember { derivedStateOf { grid.layoutInfo.visibleItemsInfo.lastOrNull()?.index?.let { it >= grid.layoutInfo.totalItemsCount - 8 } == true } }
    LaunchedEffect(nearEnd, loading, endReached) { if (nearEnd && !loading && !endReached && items.isNotEmpty()) page++ }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Discover")
        if (all.isEmpty()) {
            EmptyState(Icons.Rounded.CloudOff, "Nothing to browse", "Install an add-on with catalogs in Settings.")
            return@Column
        }
        ChipRow(types.map { it to typeLabel(it) }, currentType) { type = it; catalogKey = null; genre = null }
        ChipRow(catalogs.map { (a, c) -> "${a.transportUrl}|${c.id}" to (if (addons.size > 1) "${c.name} · ${a.manifest.name}" else c.name) }, current?.let { "${it.first.transportUrl}|${it.second.id}" }) {
            catalogKey = it; genre = null
        }
        val genres = current?.second?.genres.orEmpty()
        if (genres.isNotEmpty()) ChipRow(listOf("" to "All genres") + genres.map { it to it }, genre ?: "") { genre = it.ifEmpty { null } }
        Box(Modifier.weight(1f).fillMaxWidth().padding(top = 8.dp)) {
            LazyVerticalGrid(
                state = grid,
                columns = GridCells.Adaptive(108.dp),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(items, key = { it.id }) { p -> PosterCard(p, width = null, onClick = { nav.openDetail(p) }, onLongClick = { quick(p) }) }
                if (loading) item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = ReelColors.Accent) }
                }
            }
            if (!loading && items.isEmpty()) EmptyState(Icons.Rounded.CloudOff, "Nothing here", error ?: "This catalog is empty.")
        }
    }
}
