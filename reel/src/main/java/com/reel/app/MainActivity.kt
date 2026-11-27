package com.reel.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.reel.app.ui.ReelRoot
import com.reel.app.ui.ReelTheme
import com.reel.app.ui.components.LocalApp

class MainActivity : ComponentActivity() {
    private var pendingInstall by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT), navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT))
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) handle(intent)
        setContent {
            CompositionLocalProvider(LocalApp provides application as ReelApp) {
                ReelTheme { ReelRoot(pendingInstall) { pendingInstall = null } }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handle(intent)
    }

    /** stremio://host/manifest.json links install an add-on. */
    private fun handle(intent: Intent?) {
        val data = intent?.data ?: return
        if (data.scheme == "stremio") pendingInstall = data.toString()
    }
}
