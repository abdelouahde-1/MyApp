package com.reel.app.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class Progress(val videoId: String, val positionMs: Long, val durationMs: Long) {
    val fraction get() = if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
}

@Serializable
data class LibraryItem(
    val id: String,
    val type: String,
    val name: String,
    val poster: String? = null,
    val background: String? = null,
    val releaseInfo: String? = null,
    val favorite: Boolean = false,
    val watchlist: Boolean = false,
    val watched: Boolean = false,
    /** Names of the user's own lists this title is in. */
    val lists: Set<String> = emptySet(),
    /** Episode ids the user has watched (series). */
    val watchedVideos: Set<String> = emptySet(),
    val progress: Progress? = null,
    val addedAt: Long = 0,
    val lastWatchedAt: Long = 0,
    val watchedAt: Long = 0,
) {
    val isEmpty get() = !favorite && !watchlist && !watched && lists.isEmpty() && watchedVideos.isEmpty() && progress == null

    fun toPreview() = MetaPreview(id, type, name, poster, "poster", background, releaseInfo, null, null, emptyList())

    companion object {
        fun from(p: MetaPreview, now: Long) = LibraryItem(p.id, p.type, p.name, p.poster, p.background, p.releaseInfo, addedAt = now)
    }
}

@Serializable
data class LibraryState(
    val items: Map<String, LibraryItem> = emptyMap(),
    val lists: List<String> = emptyList(),
) {
    operator fun get(id: String) = items[id]

    val continueWatching get() = items.values.filter { it.progress != null }.sortedByDescending { it.lastWatchedAt }
    val watchlist get() = items.values.filter { it.watchlist }
    val favorites get() = items.values.filter { it.favorite }
    val watched get() = items.values.filter { it.watched }
    fun list(name: String) = items.values.filter { name in it.lists }
}

/** When a playback position counts as "finished". */
const val FINISHED_FRACTION = 0.9f

/**
 * Pure state transitions for the library, so the rules are easy to test:
 *  - marking a title watched takes it off the watchlist and out of "Continue watching";
 *  - finishing an episode marks it watched and queues the next one in "Continue watching";
 *  - finishing every episode of a series marks the whole series watched;
 *  - an item that ends up in no list at all is dropped.
 */
object LibraryRules {
    private fun LibraryState.edit(p: MetaPreview, now: Long, f: (LibraryItem) -> LibraryItem): LibraryState {
        val base = items[p.id]?.copy(name = p.name, poster = p.poster ?: items[p.id]?.poster, background = p.background ?: items[p.id]?.background)
            ?: LibraryItem.from(p, now)
        val next = f(base)
        return copy(items = if (next.isEmpty) items - p.id else items + (p.id to next))
    }

    fun toggleFavorite(s: LibraryState, p: MetaPreview, now: Long) = s.edit(p, now) { it.copy(favorite = !it.favorite) }

    fun toggleWatchlist(s: LibraryState, p: MetaPreview, now: Long) = s.edit(p, now) { it.copy(watchlist = !it.watchlist, addedAt = now) }

    fun setWatched(s: LibraryState, p: MetaPreview, watched: Boolean, now: Long, allVideoIds: Collection<String> = emptyList()) =
        s.edit(p, now) {
            if (watched) it.copy(watched = true, watchlist = false, progress = null, watchedAt = now, watchedVideos = it.watchedVideos + allVideoIds)
            else it.copy(watched = false, watchedVideos = it.watchedVideos - allVideoIds.toSet())
        }

    /** Marks one episode (un)watched. [allVideoIds] = every released episode, to detect a completed series. */
    fun setVideoWatched(s: LibraryState, p: MetaPreview, videoId: String, watched: Boolean, allVideoIds: Collection<String>, now: Long) =
        s.edit(p, now) {
            val videos = if (watched) it.watchedVideos + videoId else it.watchedVideos - videoId
            val complete = allVideoIds.isNotEmpty() && videos.containsAll(allVideoIds)
            it.copy(
                watchedVideos = videos,
                watched = complete,
                watchlist = it.watchlist && !complete,
                watchedAt = if (complete) now else it.watchedAt,
                progress = it.progress?.takeUnless { pr -> watched && pr.videoId == videoId },
            )
        }

    /**
     * Records playback. When the video is (nearly) finished it is marked watched and, for a series,
     * [nextVideoId] becomes the "Continue watching" entry.
     */
    fun recordProgress(
        s: LibraryState, p: MetaPreview, videoId: String, positionMs: Long, durationMs: Long, now: Long,
        nextVideoId: String? = null, allVideoIds: Collection<String> = emptyList(),
    ): LibraryState {
        if (durationMs <= 0 || positionMs < 5_000) return s
        val finished = positionMs.toFloat() / durationMs >= FINISHED_FRACTION
        if (!finished) return s.edit(p, now) { it.copy(progress = Progress(videoId, positionMs, durationMs), lastWatchedAt = now) }
        val isEpisode = videoId != p.id
        if (!isEpisode) return setWatched(s, p, true, now)
        val afterEpisode = setVideoWatched(s, p, videoId, true, allVideoIds, now)
        return afterEpisode.edit(p, now) {
            it.copy(progress = if (nextVideoId != null && !it.watched) Progress(nextVideoId, 0, 0) else null, lastWatchedAt = now)
        }
    }

    fun clearProgress(s: LibraryState, p: MetaPreview, now: Long) = s.edit(p, now) { it.copy(progress = null) }

    fun toggleInList(s: LibraryState, p: MetaPreview, list: String, now: Long) =
        s.edit(p, now) { it.copy(lists = if (list in it.lists) it.lists - list else it.lists + list) }

    fun createList(s: LibraryState, name: String): LibraryState {
        val n = name.trim()
        return if (n.isEmpty() || n in s.lists) s else s.copy(lists = s.lists + n)
    }

    fun renameList(s: LibraryState, from: String, to: String): LibraryState {
        val n = to.trim()
        if (n.isEmpty() || n in s.lists || from !in s.lists) return s
        return s.copy(
            lists = s.lists.map { if (it == from) n else it },
            items = s.items.mapValues { (_, i) -> if (from in i.lists) i.copy(lists = i.lists - from + n) else i },
        )
    }

    fun deleteList(s: LibraryState, name: String) = s.copy(
        lists = s.lists - name,
        items = s.items.mapValues { (_, i) -> i.copy(lists = i.lists - name) }.filterValues { !it.isEmpty },
    )
}

/** Library persisted as one JSON file; every change is written in the background. */
class LibraryRepository(private val file: File, private val scope: CoroutineScope) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = false }
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val io = Dispatchers.IO.limitedParallelism(1) // writes run one at a time, in order
    private val _state = MutableStateFlow(load())
    val state: StateFlow<LibraryState> = _state.asStateFlow()

    private fun load(): LibraryState =
        runCatching { json.decodeFromString<LibraryState>(file.readText()) }.getOrDefault(LibraryState())

    fun update(f: (LibraryState, Long) -> LibraryState) {
        _state.update { f(it, System.currentTimeMillis()) }
        val snapshot = _state.value
        scope.launch(io) { runCatching { file.writeText(json.encodeToString(LibraryState.serializer(), snapshot)) } }
    }

    private val pretty = Json { prettyPrint = true }

    fun exportJson(): String = pretty.encodeToString(LibraryState.serializer(), state.value)

    /** Merges an exported library into this one (imported flags win). */
    fun importJson(text: String) {
        val incoming = json.decodeFromString<LibraryState>(text)
        update { s, _ -> LibraryState(s.items + incoming.items, (s.lists + incoming.lists).distinct()) }
    }
}
