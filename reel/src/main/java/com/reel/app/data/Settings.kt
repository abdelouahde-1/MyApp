package com.reel.app.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Settings(
    /** Open streams in another installed player (VLC, MX, ...) instead of the built-in one. */
    val externalPlayer: Boolean = false,
    /** Offer the next episode when one finishes. */
    val autoplayNext: Boolean = true,
    /** Preferred subtitle language (ISO 639-2, e.g. "eng"), empty = off. */
    val subtitleLang: String = "",
    /** Hide torrent sources (they open in another app, not in Reel's player). */
    val hideUnsupported: Boolean = false,
)

class SettingsRepository(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    private val _state = MutableStateFlow(
        Settings(
            externalPlayer = prefs.getBoolean("externalPlayer", false),
            autoplayNext = prefs.getBoolean("autoplayNext", true),
            subtitleLang = prefs.getString("subtitleLang", "") ?: "",
            hideUnsupported = prefs.getBoolean("hideTorrents", false),
        )
    )
    val state: StateFlow<Settings> = _state.asStateFlow()

    fun update(f: (Settings) -> Settings) {
        val s = f(_state.value)
        _state.value = s
        prefs.edit()
            .putBoolean("externalPlayer", s.externalPlayer)
            .putBoolean("autoplayNext", s.autoplayNext)
            .putString("subtitleLang", s.subtitleLang)
            .putBoolean("hideTorrents", s.hideUnsupported)
            .apply()
    }
}
