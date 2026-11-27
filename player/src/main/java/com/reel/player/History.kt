package com.reel.player

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

@Serializable
data class HistoryEntry(
    val uri: String,
    val title: String,
    val positionMs: Long,
    val durationMs: Long,
    val updatedAt: Long,
) {
    val fraction get() = if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
    val finished get() = durationMs > 0 && positionMs >= durationMs * 0.95
}

/** Pure rules for the watch history: newest first, one entry per video, capped. */
object HistoryRules {
    const val MAX = 60

    fun record(list: List<HistoryEntry>, e: HistoryEntry): List<HistoryEntry> =
        (listOf(e) + list.filterNot { it.uri == e.uri }).take(MAX)

    /** Where to start a video: the saved position, unless it was (nearly) finished or barely started. */
    fun resumePosition(list: List<HistoryEntry>, uri: String): Long {
        val e = list.firstOrNull { it.uri == uri } ?: return 0
        return if (e.finished || e.positionMs < 5_000) 0 else e.positionMs
    }
}

/** History persisted in SharedPreferences as JSON. */
class HistoryStore(context: Context) {
    private val prefs = context.getSharedPreferences("history", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }
    private val serializer = ListSerializer(HistoryEntry.serializer())
    private val _items = MutableStateFlow(load())
    val items: StateFlow<List<HistoryEntry>> = _items.asStateFlow()

    private fun load(): List<HistoryEntry> =
        runCatching { json.decodeFromString(serializer, prefs.getString("items", "[]")!!) }.getOrDefault(emptyList())

    private fun save(list: List<HistoryEntry>) {
        _items.value = list
        prefs.edit().putString("items", json.encodeToString(serializer, list)).apply()
    }

    fun record(e: HistoryEntry) = save(HistoryRules.record(items.value, e))
    fun remove(uri: String) = save(items.value.filterNot { it.uri == uri })
    fun clear() = save(emptyList())
    fun resumePosition(uri: String) = HistoryRules.resumePosition(items.value, uri)

    companion object {
        @Volatile private var instance: HistoryStore? = null
        fun get(context: Context) = instance ?: synchronized(this) { instance ?: HistoryStore(context.applicationContext).also { instance = it } }
    }
}

object Format {
    fun time(ms: Long): String {
        val total = (ms.coerceAtLeast(0) / 1000)
        val h = total / 3600; val m = (total % 3600) / 60; val s = total % 60
        return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
    }

    /** "+0:15" / "-1:02" for seek previews. */
    fun delta(ms: Long) = (if (ms >= 0) "+" else "-") + time(kotlin.math.abs(ms))

    /** A readable name for a link or file when the caller gave no title. */
    fun titleFrom(uri: String): String {
        val last = uri.substringBefore('?').trimEnd('/').substringAfterLast('/')
        return java.net.URLDecoder.decode(last.ifBlank { uri }, "UTF-8").substringBeforeLast('.').ifBlank { last }
    }
}

object GestureMath {
    /** A full-width horizontal swipe seeks this far. */
    const val FULL_SWIPE_MS = 120_000L

    fun seekTarget(startMs: Long, dragPx: Float, widthPx: Float, durationMs: Long): Long {
        if (widthPx <= 0) return startMs
        val target = startMs + (dragPx / widthPx * FULL_SWIPE_MS).toLong()
        return if (durationMs > 0) target.coerceIn(0, durationMs) else target.coerceAtLeast(0)
    }

    /** Vertical swipe up raises the level; a full-height swipe covers the whole range. */
    fun level(start: Float, dragUpPx: Float, heightPx: Float): Float =
        if (heightPx <= 0) start else (start + dragUpPx / heightPx).coerceIn(0f, 1f)
}
