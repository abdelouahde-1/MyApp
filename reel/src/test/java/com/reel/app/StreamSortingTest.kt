package com.reel.app

import com.reel.app.data.Quality
import com.reel.app.data.Stream
import com.reel.app.data.StreamSort
import com.reel.app.data.StreamSorting
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StreamSortingTest {
    private fun stream(name: String, title: String, extra: String = "") = Stream.parse(
        Json.parseToJsonElement("""{"name":${Json.encodeToString(String.serializer(), name)},"title":${Json.encodeToString(String.serializer(), title)},"infoHash":"aa"$extra}""") as JsonObject,
        "Torrentio",
    )

    private val uhd = stream("Torrentio\n4k DV | HDR", "Movie.2023.2160p.WEB-DL.mkv\n👤 120 💾 18.4 GB ⚙️ ThePirateBay")
    private val fhdBig = stream("Torrentio\n1080p", "Movie.2023.1080p.BluRay.x264.mkv\n👤 40 💾 10.2 GB ⚙️ YTS")
    private val fhdSmall = stream("Torrentio\n1080p", "Movie.2023.1080p.WEBRip.x265.mkv\n👤 300 💾 1,8 GB ⚙️ 1337x")
    private val hd = stream("Torrentio\n720p", "Movie.2023.720p.WEBRip.mkv\n👤 12 💾 900 MB ⚙️ EZTV")
    private val cam = stream("Torrentio\nCAM", "Movie.2023.HDCAM.mkv\n👤 3 💾 1.1 GB")
    private val unknown = stream("Some source", "Movie.mkv")

    @Test fun readsQualitySizeAndSeeders() {
        assertEquals(Quality.UHD, StreamSorting.info(uhd).quality)
        assertEquals(Quality.FHD, StreamSorting.info(fhdBig).quality)
        assertEquals(Quality.HD, StreamSorting.info(hd).quality)
        assertEquals(Quality.SD, StreamSorting.info(cam).quality)
        assertEquals(Quality.Unknown, StreamSorting.info(unknown).quality)
        assertEquals(1_800_000_000L, StreamSorting.info(fhdSmall).sizeBytes)
        assertEquals(900_000_000L, StreamSorting.info(hd).sizeBytes)
        assertEquals(300, StreamSorting.info(fhdSmall).seeders)
        assertNull(StreamSorting.info(unknown).sizeBytes)
        // An explicit videoSize hint wins over text.
        assertEquals(5L, StreamSorting.info(stream("x", "💾 2 GB", ""","behaviorHints":{"videoSize":5}""")).sizeBytes)
    }

    @Test fun sortsBySizeAndSeedersWithUnknownLast() {
        val all = listOf(unknown, hd, fhdBig, uhd, fhdSmall)
        assertEquals(listOf(uhd, fhdBig, fhdSmall, hd, unknown), StreamSorting.sort(all, StreamSort.SizeDesc))
        assertEquals(listOf(hd, fhdSmall, fhdBig, uhd, unknown), StreamSorting.sort(all, StreamSort.SizeAsc))
        assertEquals(listOf(fhdSmall, uhd, fhdBig, hd, unknown), StreamSorting.sort(all, StreamSort.Seeders))
        assertEquals(all, StreamSorting.sort(all, StreamSort.Default))
    }

    @Test fun opensOnPreferredQualityOrBestAvailable() {
        assertEquals(Quality.FHD, StreamSorting.initialQuality(setOf(Quality.UHD, Quality.FHD, Quality.HD), Quality.FHD))
        assertEquals(Quality.UHD, StreamSorting.initialQuality(setOf(Quality.UHD, Quality.HD), Quality.FHD))
        assertNull(StreamSorting.initialQuality(emptySet(), Quality.FHD))
    }
}
