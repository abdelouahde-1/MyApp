package com.reel.app

import com.reel.app.data.Addon
import com.reel.app.data.AddonClient
import com.reel.app.data.AddonUrls
import com.reel.app.data.Http
import com.reel.app.data.Manifest
import com.reel.app.data.Meta
import com.reel.app.data.Stream
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProtocolTest {
    private fun obj(s: String) = Json.parseToJsonElement(s) as JsonObject

    private val manifestJson = """
        {"id":"org.test","version":"1.2.0","name":"Test","types":["movie","series"],
         "idPrefixes":["tt"],
         "resources":["catalog",{"name":"meta","types":["series"]},{"name":"stream","types":["movie","series"],"idPrefixes":["tt","kitsu"]}],
         "catalogs":[
           {"type":"movie","id":"top","name":"Popular","extra":[{"name":"genre","options":["Action","Drama"]},{"name":"skip"},{"name":"search"}]},
           {"type":"series","id":"find","extra":[{"name":"search","isRequired":true}]},
           {"type":"movie","id":"old","extraSupported":["skip"],"extraRequired":[]}
         ]}
    """

    @Test fun normalizesAddonUrls() {
        assertEquals("https://a.example/x/manifest.json", AddonUrls.normalize("stremio://a.example/x/manifest.json"))
        assertEquals("https://a.example/manifest.json", AddonUrls.normalize("a.example/"))
        assertEquals("http://a.example/manifest.json", AddonUrls.normalize(" http://a.example/manifest.json "))
    }

    @Test fun buildsResourceUrlsWithEncodedExtras() {
        assertEquals("https://h/catalog/movie/top.json", AddonUrls.resource("https://h", "catalog", "movie", "top"))
        assertEquals(
            "https://h/catalog/movie/top/genre=Sci-Fi%20%26%20Fantasy&skip=100.json",
            AddonUrls.resource("https://h", "catalog", "movie", "top", linkedMapOf("genre" to "Sci-Fi & Fantasy", "skip" to "100", "search" to "")),
        )
        assertEquals("https://h/stream/series/tt1:2:3.json", AddonUrls.resource("https://h", "stream", "series", "tt1:2:3"))
    }

    @Test fun parsesManifestResourcesAndCatalogs() {
        val m = Manifest.parse(obj(manifestJson))
        assertEquals(3, m.catalogs.size)
        assertEquals(listOf("Action", "Drama"), m.catalogs[0].genres)
        assertTrue(m.catalogs[0].supportsSearch && m.catalogs[0].browsable)
        assertFalse(m.catalogs[1].browsable)
        assertEquals("find", m.catalogs[1].name)
        assertTrue(m.catalogs[2].supportsSkip)
        assertTrue(m.supports("meta", "series", "tt1"))
        assertFalse(m.supports("meta", "movie", "tt1"))
        assertTrue(m.supports("stream", "series", "kitsu:5"))
        assertFalse(m.supports("subtitles", "movie", "tt1"))
        assertFalse(m.supports("catalog", "tv", "tt1"))
    }

    @Test fun parsesSeriesMetaAndFindsNextEpisode() {
        val meta = Meta.parse(obj("""
            {"id":"tt9","type":"series","name":"Show","imdbRating":"8.1","genres":["Drama"],
             "trailers":[{"source":"abc","type":"Trailer"}],
             "links":[{"name":"Ann","category":"Cast"}],
             "videos":[
               {"id":"tt9:2:1","name":"Two-one","season":2,"episode":1},
               {"id":"tt9:1:2","name":"One-two","season":"1","episode":2},
               {"id":"tt9:0:1","name":"Special","season":0,"episode":1},
               {"id":"tt9:1:1","name":"One-one","season":1,"episode":1}
             ]}
        """), "series")!!
        assertEquals(listOf(1, 2, 0), meta.seasons)
        assertEquals(listOf("tt9:1:1", "tt9:1:2"), meta.episodes(1).map { it.id })
        assertEquals("tt9:2:1", meta.nextVideo("tt9:1:2")?.id)
        assertEquals("tt9:0:1", meta.nextVideo("tt9:2:1")?.id)
        assertNull(meta.nextVideo("tt9:0:1"))
        assertEquals(listOf("Ann"), meta.cast)
        assertEquals(listOf("abc"), meta.trailerYtIds)
        assertEquals("S1E2", meta.episodes(1)[1].label)
    }

    @Test fun classifiesStreams() {
        assertEquals(Stream.Kind.Direct, Stream.parse(obj("""{"url":"https://x/a.mp4","name":"HD"}"""), "A").kind)
        assertEquals(Stream.Kind.YouTube, Stream.parse(obj("""{"ytId":"q"}"""), "A").kind)
        assertEquals(Stream.Kind.Unsupported, Stream.parse(obj("""{"name":"nothing"}"""), "A").kind)
        // Torrent-only sources are not playable in Reel and are not offered.
        assertEquals(Stream.Kind.Unsupported, Stream.parse(obj("""{"infoHash":"ff00","name":"T 1080p"}"""), "A").kind)
        val s = Stream.parse(obj("""{"url":"u","behaviorHints":{"proxyHeaders":{"request":{"Referer":"r"}}},"subtitles":[{"url":"s.srt","lang":"eng"}]}"""), "A")
        assertEquals(mapOf("Referer" to "r"), s.headers)
        assertEquals("eng", s.subtitles.single().lang)
        assertEquals("A", s.headline)
    }

    @Test fun clientQueriesOnlySupportingAddonsAndSurvivesFailures() = runTest {
        val requested = mutableListOf<String>()
        val http = object : Http {
            override suspend fun get(url: String): String {
                requested += url
                return when {
                    url.startsWith("https://bad") -> error("down")
                    url.contains("/stream/") -> """{"streams":[{"url":"https://v/1.mp4","name":"1080p"}]}"""
                    url.contains("/catalog/") -> """{"metas":[{"id":"tt1","name":"One"},{"id":"tt1","name":"Dup"},{"name":"no id"}]}"""
                    else -> "{}"
                }
            }
        }
        val m = Manifest.parse(obj(manifestJson))
        val good = Addon("https://good/manifest.json", m)
        val bad = Addon("https://bad/manifest.json", m.copy(name = "Bad"))
        val off = Addon("https://off/manifest.json", m, enabled = false)
        val client = AddonClient(http)

        val groups = client.streams(listOf(good, bad, off), "movie", "tt1")
        assertEquals(listOf("Test", "Bad"), groups.map { it.addonName })
        assertEquals("1080p", groups[0].streams.single().headline)
        assertTrue(groups[1].error != null)
        assertTrue(requested.none { it.startsWith("https://off") })

        val items = client.catalog(good, m.catalogs[0])
        assertEquals(listOf("One"), items.map { it.name })
        assertEquals("movie", items[0].type)
    }
}
