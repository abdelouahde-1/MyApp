package com.reel.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.reel.app.data.Addon
import com.reel.app.data.CatalogDef
import com.reel.app.data.MetaPreview
import com.reel.app.ui.ReelColors
import com.reel.app.ui.components.EmptyState
import com.reel.app.ui.components.LocalApp
import com.reel.app.ui.components.PlaceholderPoster
import com.reel.app.ui.components.PosterCard
import com.reel.app.ui.components.PosterRow
import com.reel.app.ui.components.ScreenHeader
import com.reel.app.ui.components.SectionTitle
import com.reel.app.ui.components.rememberQuickActions
import com.reel.app.ui.openDetail
import com.reel.app.ui.openDiscover
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun episodeLabel(videoId: String): String? {
    val parts = videoId.split(':')
    return if (parts.size >= 3) "S${parts[parts.size - 2]} · E${parts.last()}" else null
}

fun typeLabel(type: String) = when (type) {
    "movie" -> "Movies"
    "series" -> "Series"
    "tv" -> "TV"
    "channel" -> "Channels"
    else -> type.replaceFirstChar { it.uppercase() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(nav: NavController) {
    val app = LocalApp.current
    val addons by app.addons.addons.collectAsState()
    val lib by app.library.state.collectAsState()
    val quick = rememberQuickActions { nav.openDetail(it) }
    var refreshKey by remember { mutableIntStateOf(0) }
    var refreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val rows = addons.filter { it.enabled }.flatMap { a -> a.manifest.catalogs.filter { it.browsable }.map { a to it } }

    PullToRefreshBox(
        isRefreshing = refreshing,
        onRefresh = {
            refreshing = true
            app.client.clearCache()
            refreshKey++
            scope.launch { delay(600); refreshing = false }
        },
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
            item {
                ScreenHeader("Reel") {
                    IconButton(onClick = { nav.navigate("settings") }) { Icon(Icons.Rounded.Settings, "Settings", tint = ReelColors.Muted) }
                }
            }
            val continuing = lib.continueWatching
            if (continuing.isNotEmpty()) item("continue") {
                SectionTitle("Continue watching")
                LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(continuing, key = { it.id }) { item ->
                        PosterCard(
                            item.toPreview(),
                            width = 132.dp,
                            progress = item.progress?.fraction?.coerceAtLeast(0.02f),
                            caption = item.progress?.let { pr ->
                                val ep = episodeLabel(pr.videoId)
                                when {
                                    ep == null -> "Resume"
                                    pr.positionMs == 0L -> "Up next · $ep"
                                    else -> ep
                                }
                            },
                            onClick = { nav.openDetail(item.type, item.id) },
                            onLongClick = { quick(item.toPreview()) },
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
            val watchlist = lib.watchlist.sortedByDescending { it.addedAt }
            if (watchlist.isNotEmpty()) item("watchlist") {
                PosterRow("From your watchlist", watchlist.take(20).map { it.toPreview() }, { nav.openDetail(it) }, quick) {
                    TextButton(onClick = { nav.navigate("library") }) { Text("All") }
                }
            }
            if (addons.isEmpty()) item {
                EmptyState(Icons.Rounded.Extension, "No add-ons yet", "Add-ons provide catalogs and stream sources. Pull down to retry, or add one in Settings.")
                Button(onClick = { nav.navigate("settings") }, modifier = Modifier.padding(horizontal = 40.dp)) { Text("Open add-ons") }
            }
            items(rows, key = { (a, c) -> "${a.transportUrl}|${c.type}|${c.id}|$refreshKey" }) { (a, c) ->
                CatalogRow(a, c, onOpen = { nav.openDetail(it) }, onLongPress = quick, onSeeAll = { nav.openDiscover(c.type, a.transportUrl, c.id) })
            }
        }
    }
}

@Composable
private fun CatalogRow(addon: Addon, catalog: CatalogDef, onOpen: (MetaPreview) -> Unit, onLongPress: (MetaPreview) -> Unit, onSeeAll: () -> Unit) {
    val app = LocalApp.current
    val result by produceState<Result<List<MetaPreview>>?>(null, addon.transportUrl, catalog.id) {
        value = runCatching { app.client.catalog(addon, catalog) }
    }
    val title = "${catalog.name} · ${typeLabel(catalog.type)}"
    when {
        result == null -> {
            SectionTitle(title)
            LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), userScrollEnabled = false) {
                items(6) { PlaceholderPoster(Modifier.width(116.dp)) }
            }
            Spacer(Modifier.height(36.dp))
        }
        result!!.getOrNull().isNullOrEmpty() -> Unit // hide failed/empty rows to keep the board clean
        else -> PosterRow(title, result!!.getOrThrow().take(30), onOpen, onLongPress) {
            TextButton(onClick = onSeeAll) { Text("See all") }
        }
    }
}
