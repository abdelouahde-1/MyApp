package com.reel.app

import android.content.Context
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.printToString
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import com.reel.app.data.Http
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** A fake add-on serving a tiny catalog, so the UI can be exercised offline. */
object FakeAddon : Http {
    private val movies = (1..8).joinToString(",") { """{"id":"tt10$it","type":"movie","name":"Movie $it","releaseInfo":"202$it","imdbRating":"7.$it"}""" }
    private val shows = """{"id":"tt200","type":"series","name":"The Long Road","releaseInfo":"2019–"},{"id":"tt201","type":"series","name":"Night Shift","releaseInfo":"2022"}"""
    private val episodes = (1..2).flatMap { s -> (1..3).map { e -> """{"id":"tt200:$s:$e","name":"Chapter $e","season":$s,"episode":$e,"released":"2020-01-0${e}T00:00:00.000Z","overview":"Something happens in episode $e."}""" } }.joinToString(",")

    override suspend fun get(url: String): String = when {
        url.endsWith("manifest.json") -> """{"id":"test.addon","version":"1.0.0","name":"Sample","description":"Demo catalog and sources","types":["movie","series"],"idPrefixes":["tt"],
            "resources":["catalog","meta","stream"],
            "catalogs":[{"type":"movie","id":"top","name":"Popular","extra":[{"name":"genre","options":["Action","Drama"]},{"name":"search"}]},
                        {"type":"series","id":"top","name":"Popular","extra":[{"name":"search"}]}]}"""
        url.contains("/catalog/movie/") -> """{"metas":[$movies]}"""
        url.contains("/catalog/series/") -> """{"metas":[$shows]}"""
        url.contains("/meta/series/tt200") -> """{"meta":{"id":"tt200","type":"series","name":"The Long Road","releaseInfo":"2019–","imdbRating":"8.4","genres":["Drama","Mystery"],
            "description":"Two strangers drive across a continent and slowly find out why they were both invited.","runtime":"52 min","cast":["A. Actor","B. Actor"],"videos":[$episodes]}}"""
        url.contains("/meta/") -> """{"meta":{"id":"tt101","type":"movie","name":"Movie 1","releaseInfo":"2021","description":"A movie."}}"""
        url.contains("/stream/") -> """{"streams":[{"name":"Sample 1080p","title":"Open demo video\nH.264 · 1.2 GB","url":"https://example.com/demo.mp4"},{"name":"Sample","title":"Torrent","infoHash":"abc"}]}"""
        else -> "{}"
    }
}

class TestReelApp : ReelApp() {
    override fun createHttp(): Http = FakeAddon
    override fun onCreate() {
        filesDir.resolve("addons.json").delete()
        filesDir.resolve("library.json").delete()
        super.onCreate()
    }
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = TestReelApp::class, qualifiers = "w411dp-h891dp-xxhdpi")
class AppFlowTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    /** Captures the whole screen, including bottom sheets and dialogs. */
    @OptIn(com.github.takahirom.roborazzi.ExperimentalRoborazziApi::class)
    private fun shot(name: String) {
        rule.waitForIdle()
        com.github.takahirom.roborazzi.captureScreenRoboImage("build/outputs/roborazzi/$name.png")
    }
    private fun waitFor(text: String) {
        try {
            rule.waitUntil(5_000) { rule.onAllNodes(hasText(text, substring = true)).fetchSemanticsNodes().isNotEmpty() }
        } catch (e: Throwable) {
            // Dump every window's UI tree so CI logs show what was on screen.
            val tree = rule.onAllNodes(androidx.compose.ui.test.isRoot()).fetchSemanticsNodes().indices.joinToString("\n---\n") { i ->
                rule.onAllNodes(androidx.compose.ui.test.isRoot())[i].printToString()
            }
            throw AssertionError("\"$text\" never appeared. Screen:\n$tree", e)
        }
    }

    @Test fun browseSaveAndOrganize() {
        // Default add-on list is replaced by our fake add-on on first launch.
        val app = ApplicationProvider.getApplicationContext<Context>() as ReelApp
        waitFor("Popular")
        shot("1-home")

        // Long-press a poster → quick actions → add to watchlist and a new custom list.
        rule.onAllNodesWithText("Movie 2").onFirst().performTouchInput { longClick() }
        waitFor("Watchlist")
        rule.onNodeWithText("Watchlist").performClick()
        rule.waitForIdle()
        shot("2-quick-actions")
        assertTrue(app.library.state.value["tt102"]!!.watchlist)
        Espresso.pressBack() // closes the sheet
        rule.waitForIdle()

        // Series detail with seasons and episodes.
        rule.onAllNodesWithText("The Long Road").onFirst().performClick()
        waitFor("Chapter 1")
        shot("3-detail-series")
        rule.onAllNodes(androidx.compose.ui.test.hasContentDescription("Mark watched")).onFirst().performClick()
        rule.waitForIdle()
        assertEquals(setOf("tt200:1:1"), app.library.state.value["tt200"]!!.watchedVideos)

        // Big button now offers the next unwatched episode, and opens the source picker.
        waitFor("Play S1E2")
        rule.onNodeWithText("Play S1E2").performClick()
        waitFor("Sample 1080p")
        shot("4-sources")
        Espresso.pressBack()
        rule.waitForIdle()
        rule.onNodeWithText("Favorite").performClick()
        rule.waitForIdle()
        assertTrue(app.library.state.value["tt200"]!!.favorite)
        Espresso.pressBack()
        rule.waitForIdle()

        // Library shows watchlist by default; favorites tab has the series.
        rule.onNodeWithText("Library").performClick()
        waitFor("Movie 2")
        shot("5-library")
        rule.onNodeWithText("Favorites", substring = true).performClick()
        waitFor("The Long Road")

        rule.onNodeWithText("Discover").performClick()
        waitFor("Movie 8")
        shot("6-discover")

        rule.onNodeWithText("Home").performClick()
        rule.onNodeWithContentDescription("Settings").performClick()
        waitFor("Add-ons")
        shot("7-settings")
    }
}
