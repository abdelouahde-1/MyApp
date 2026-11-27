package com.reel.app

import com.reel.app.data.LibraryRules
import com.reel.app.data.LibraryState
import com.reel.app.data.MetaPreview
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LibraryRulesTest {
    private val movie = MetaPreview("tt1", "movie", "Movie", "p", "poster", null, "2020", null, null, emptyList())
    private val show = MetaPreview("tt2", "series", "Show", "p", "poster", null, "2021", null, null, emptyList())
    private val episodes = listOf("tt2:1:1", "tt2:1:2")
    private val empty = LibraryState()

    @Test fun togglingTheOnlyFlagOffRemovesTheItem() {
        val s = LibraryRules.toggleWatchlist(empty, movie, 1)
        assertTrue(s["tt1"]!!.watchlist)
        assertTrue(LibraryRules.toggleWatchlist(s, movie, 2).items.isEmpty())
    }

    @Test fun watchingAMovieTakesItOffTheWatchlistButKeepsFavorite() {
        var s = LibraryRules.toggleWatchlist(empty, movie, 1)
        s = LibraryRules.toggleFavorite(s, movie, 1)
        s = LibraryRules.recordProgress(s, movie, "tt1", 60_000, 100_000, 2)
        assertEquals(listOf("tt1"), s.continueWatching.map { it.id })
        s = LibraryRules.recordProgress(s, movie, "tt1", 95_000, 100_000, 3)
        val item = s["tt1"]!!
        assertTrue(item.watched && item.favorite)
        assertFalse(item.watchlist)
        assertNull(item.progress)
        assertTrue(s.continueWatching.isEmpty())
    }

    @Test fun tinyProgressIsIgnored() {
        assertTrue(LibraryRules.recordProgress(empty, movie, "tt1", 2_000, 100_000, 1).items.isEmpty())
    }

    @Test fun finishingAnEpisodeQueuesTheNextAndTheLastCompletesTheSeries() {
        var s = LibraryRules.toggleWatchlist(empty, show, 1)
        s = LibraryRules.recordProgress(s, show, "tt2:1:1", 1_000_000, 1_000_000, 2, nextVideoId = "tt2:1:2", allVideoIds = episodes)
        var item = s["tt2"]!!
        assertEquals(setOf("tt2:1:1"), item.watchedVideos)
        assertEquals("tt2:1:2", item.progress?.videoId)
        assertTrue(item.watchlist)
        s = LibraryRules.recordProgress(s, show, "tt2:1:2", 950_000, 1_000_000, 3, nextVideoId = null, allVideoIds = episodes)
        item = s["tt2"]!!
        assertTrue(item.watched)
        assertFalse(item.watchlist)
        assertNull(item.progress)
    }

    @Test fun unwatchingAnEpisodeReopensTheSeries() {
        var s = LibraryRules.setWatched(empty, show, true, 1, episodes)
        assertEquals(episodes.toSet(), s["tt2"]!!.watchedVideos)
        s = LibraryRules.setVideoWatched(s, show, "tt2:1:2", false, episodes, 2)
        assertFalse(s["tt2"]!!.watched)
    }

    @Test fun customListsCanBeRenamedAndDeleted() {
        var s = LibraryRules.createList(empty, " Date night ")
        s = LibraryRules.createList(s, "Date night")
        assertEquals(listOf("Date night"), s.lists)
        s = LibraryRules.toggleInList(s, movie, "Date night", 1)
        s = LibraryRules.renameList(s, "Date night", "Weekend")
        assertEquals(listOf("tt1"), s.list("Weekend").map { it.id })
        s = LibraryRules.deleteList(s, "Weekend")
        assertTrue(s.lists.isEmpty() && s.items.isEmpty())
    }
}
