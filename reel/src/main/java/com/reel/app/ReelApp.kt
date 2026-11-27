package com.reel.app

import android.app.Application
import com.reel.app.data.AddonClient
import com.reel.app.data.AddonRepository
import com.reel.app.data.Http
import com.reel.app.data.LibraryRepository
import com.reel.app.data.SettingsRepository
import com.reel.app.data.Stream
import com.reel.app.data.Subtitle
import com.reel.app.data.UrlConnectionHttp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/** What the player should open next (set right before navigating to it). */
data class Playback(
    val itemId: String,
    val type: String,
    val videoId: String,
    val title: String,
    val subtitle: String?,
    val stream: Stream,
    val startPositionMs: Long,
)

open class ReelApp : Application() {
    val scope = CoroutineScope(SupervisorJob())
    /** Network access; tests replace it with canned responses. */
    protected open fun createHttp(): Http = UrlConnectionHttp()
    private val http by lazy { createHttp() }
    val client by lazy { AddonClient(http) }
    lateinit var addons: AddonRepository
    lateinit var library: LibraryRepository
    lateinit var settings: SettingsRepository
    var pendingPlayback: Playback? = null
    var pendingSubtitles: List<Subtitle> = emptyList()

    override fun onCreate() {
        super.onCreate()
        addons = AddonRepository(filesDir.resolve("addons.json"), http, scope)
        library = LibraryRepository(filesDir.resolve("library.json"), scope)
        settings = SettingsRepository(this)
    }
}
