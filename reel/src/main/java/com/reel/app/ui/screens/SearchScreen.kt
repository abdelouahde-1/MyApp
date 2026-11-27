package com.reel.app.ui.screens

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.reel.app.data.CatalogPage
import com.reel.app.ui.ReelColors
import com.reel.app.ui.components.EmptyState
import com.reel.app.ui.components.LocalApp
import com.reel.app.ui.components.PosterRow
import com.reel.app.ui.components.ScreenHeader
import com.reel.app.ui.components.SectionTitle
import com.reel.app.ui.components.rememberQuickActions
import com.reel.app.ui.openDetail
import kotlinx.coroutines.delay

private class RecentSearches(context: Context) {
    private val prefs = context.getSharedPreferences("search", Context.MODE_PRIVATE)
    fun get(): List<String> = prefs.getString("recent", "")!!.split('\n').filter { it.isNotBlank() }
    fun add(q: String) = save((listOf(q.trim()) + get().filterNot { it.equals(q.trim(), true) }).take(8))
    fun clear() = save(emptyList())
    private fun save(l: List<String>) = prefs.edit().putString("recent", l.joinToString("\n")).apply()
}

@Composable
fun SearchScreen(nav: NavController) {
    val app = LocalApp.current
    val context = LocalContext.current
    val recent = remember { RecentSearches(context) }
    var recentList by remember { mutableStateOf(recent.get()) }
    val quick = rememberQuickActions { nav.openDetail(it) }
    var query by rememberSaveable { mutableStateOf("") }
    var results by remember { mutableStateOf<List<CatalogPage>?>(null) }
    var loading by remember { mutableStateOf(false) }
    val keyboard = LocalSoftwareKeyboardController.current
    val focus = remember { FocusRequester() }

    LaunchedEffect(query) {
        val q = query.trim()
        if (q.length < 2) { results = null; loading = false; return@LaunchedEffect }
        delay(400) // debounce typing
        loading = true
        val found = app.client.search(app.addons.enabled, q)
        results = found
        loading = false
        if (found.isNotEmpty()) { recent.add(q); recentList = recent.get() }
    }
    LaunchedEffect(Unit) { if (query.isEmpty()) focus.requestFocus() }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Search")
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).focusRequester(focus),
            placeholder = { Text("Movies, series…") },
            leadingIcon = { Icon(Icons.Rounded.Search, null) },
            trailingIcon = { if (query.isNotEmpty()) IconButton(onClick = { query = "" }) { Icon(Icons.Rounded.Close, "Clear") } },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
            colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = ReelColors.Surface, focusedContainerColor = ReelColors.Surface, unfocusedBorderColor = ReelColors.Surface),
        )
        Box(Modifier.fillMaxWidth().padding(top = 8.dp)) {
            if (loading) LinearProgressIndicator(Modifier.fillMaxWidth().padding(horizontal = 20.dp), color = ReelColors.Accent)
        }
        val r = results
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 24.dp)) {
            when {
                r == null && recentList.isNotEmpty() -> {
                    item {
                        SectionTitle("Recent") { TextButton(onClick = { recent.clear(); recentList = emptyList() }) { Text("Clear") } }
                    }
                    items(recentList) { q ->
                        Row(Modifier.fillMaxWidth().clickable { query = q }.padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.History, null, tint = ReelColors.Muted)
                            Spacer(Modifier.width(14.dp))
                            Text(q)
                        }
                    }
                }
                r != null && r.isEmpty() && !loading -> item {
                    EmptyState(Icons.Rounded.SearchOff, "No results", "Try another title, or install an add-on that supports search.")
                }
                r != null -> items(r, key = { "${it.addon.transportUrl}|${it.catalog.type}|${it.catalog.id}" }) { page ->
                    val title = if (app.addons.enabled.size > 1) "${typeLabel(page.catalog.type)} · ${page.addon.manifest.name}" else typeLabel(page.catalog.type)
                    PosterRow(title, page.items, { nav.openDetail(it) }, quick)
                }
            }
        }
    }
}
