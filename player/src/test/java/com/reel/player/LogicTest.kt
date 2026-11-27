package com.reel.player

import org.junit.Assert.assertEquals
import org.junit.Test

class LogicTest {
    private fun e(uri: String, pos: Long, dur: Long = 100_000, at: Long = 0) = HistoryEntry(uri, uri, pos, dur, at)

    @Test fun historyKeepsOneEntryPerVideoNewestFirst() {
        var l = HistoryRules.record(emptyList(), e("a", 10_000))
        l = HistoryRules.record(l, e("b", 20_000))
        l = HistoryRules.record(l, e("a", 30_000))
        assertEquals(listOf("a", "b"), l.map { it.uri })
        assertEquals(30_000, l[0].positionMs)
        val many = (1..100).fold(emptyList<HistoryEntry>()) { acc, i -> HistoryRules.record(acc, e("v$i", 1)) }
        assertEquals(HistoryRules.MAX, many.size)
        assertEquals("v100", many.first().uri)
    }

    @Test fun resumesUnlessFinishedOrBarelyStarted() {
        val l = listOf(e("mid", 40_000), e("done", 99_000), e("start", 2_000))
        assertEquals(40_000, HistoryRules.resumePosition(l, "mid"))
        assertEquals(0, HistoryRules.resumePosition(l, "done"))
        assertEquals(0, HistoryRules.resumePosition(l, "start"))
        assertEquals(0, HistoryRules.resumePosition(l, "unknown"))
    }

    @Test fun formatsTimes() {
        assertEquals("0:05", Format.time(5_400))
        assertEquals("12:03", Format.time(723_000))
        assertEquals("1:02:03", Format.time(3_723_000))
        assertEquals("+0:15", Format.delta(15_000))
        assertEquals("-1:02", Format.delta(-62_000))
        assertEquals("My Movie", Format.titleFrom("https://h/x/My%20Movie.mp4?token=1"))
    }

    @Test fun gestureMathClamps() {
        assertEquals(60_000, GestureMath.seekTarget(0, 500f, 1000f, 600_000))
        assertEquals(0, GestureMath.seekTarget(10_000, -1000f, 1000f, 600_000))
        assertEquals(600_000, GestureMath.seekTarget(590_000, 1000f, 1000f, 600_000))
        assertEquals(0.75f, GestureMath.level(0.5f, 250f, 1000f), 0.001f)
        assertEquals(1f, GestureMath.level(0.9f, 1000f, 1000f), 0.001f)
    }
}
