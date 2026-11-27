package com.reel.app.ui

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CollectionsBookmark
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.reel.app.data.MetaPreview
import com.reel.app.ui.components.LocalApp
import com.reel.app.ui.screens.DetailScreen
import com.reel.app.ui.screens.DiscoverScreen
import com.reel.app.ui.screens.HomeScreen
import com.reel.app.ui.screens.LibraryScreen
import com.reel.app.ui.screens.PlayerScreen
import com.reel.app.ui.screens.SearchScreen
import com.reel.app.ui.screens.SettingsScreen
import kotlinx.coroutines.launch

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab("home", "Home", Icons.Rounded.Home),
    Tab("discover", "Discover", Icons.Rounded.Explore),
    Tab("search", "Search", Icons.Rounded.Search),
    Tab("library", "Library", Icons.Rounded.CollectionsBookmark),
)

fun NavController.openDetail(p: MetaPreview) = navigate("detail/${Uri.encode(p.type)}/${Uri.encode(p.id)}")
fun NavController.openDetail(type: String, id: String) = navigate("detail/${Uri.encode(type)}/${Uri.encode(id)}")
fun NavController.openDiscover(type: String, addonUrl: String, catalogId: String) =
    navigate("discover?type=${Uri.encode(type)}&addon=${Uri.encode(addonUrl)}&catalog=${Uri.encode(catalogId)}") { launchSingleTop = true }

@Composable
fun ReelRoot(pendingInstall: String?, onInstallHandled: () -> Unit) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route.orEmpty().substringBefore('?')
    val showBar = route in tabs.map { it.route }

    Scaffold(
        containerColor = ReelColors.Bg,
        bottomBar = {
            if (showBar) NavigationBar(containerColor = ReelColors.Bg, tonalElevation = 0.dp) {
                tabs.forEach { t ->
                    NavigationBarItem(
                        selected = route == t.route,
                        onClick = {
                            nav.navigate(t.route) {
                                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(t.icon, null) },
                        label = { Text(t.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ReelColors.OnAccent,
                            indicatorColor = ReelColors.Accent,
                            selectedTextColor = ReelColors.Text,
                            unselectedIconColor = ReelColors.Muted,
                            unselectedTextColor = ReelColors.Muted,
                        ),
                    )
                }
            }
        },
    ) { pad ->
        Box(Modifier.fillMaxSize().padding(bottom = if (showBar) pad.calculateBottomPadding() else 0.dp)) {
            NavHost(nav, startDestination = "home") {
                composable("home") { HomeScreen(nav) }
                composable(
                    "discover?type={type}&addon={addon}&catalog={catalog}",
                    arguments = listOf("type", "addon", "catalog").map { navArgument(it) { type = NavType.StringType; nullable = true; defaultValue = null } },
                ) { e ->
                    DiscoverScreen(nav, e.arguments?.getString("type"), e.arguments?.getString("addon"), e.arguments?.getString("catalog"))
                }
                composable("search") { SearchScreen(nav) }
                composable("library") { LibraryScreen(nav) }
                composable("settings") { SettingsScreen(nav) }
                composable("player") { PlayerScreen(nav) }
                composable(
                    "detail/{type}/{id}",
                    arguments = listOf(navArgument("type") { type = NavType.StringType }, navArgument("id") { type = NavType.StringType }),
                ) { e -> DetailScreen(nav, e.arguments!!.getString("type")!!, e.arguments!!.getString("id")!!, e.savedStateHandle) }
            }
        }
    }

    if (pendingInstall != null) InstallDialog(pendingInstall, onInstallHandled)
}

@Composable
private fun InstallDialog(url: String, onDone: () -> Unit) {
    val app = LocalApp.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var busy by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDone,
        containerColor = ReelColors.Surface,
        title = { Text("Install add-on?") },
        text = { Text(url) },
        confirmButton = {
            TextButton(enabled = !busy, onClick = {
                busy = true
                scope.launch {
                    val msg = runCatching { "Installed ${app.addons.install(url)}" }.getOrElse { "Could not install: ${it.message}" }
                    android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                    onDone()
                }
            }) { Text(if (busy) "Installing…" else "Install") }
        },
        dismissButton = { TextButton(onClick = onDone) { Text("Cancel") } },
    )
}
