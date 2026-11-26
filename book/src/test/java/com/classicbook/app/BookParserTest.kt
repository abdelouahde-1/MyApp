package com.classicbook.app

import com.classicbook.app.data.BookParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BookParserTest {
    @Test fun joinsHardWrappedLinesAndFindsChapters() {
        val text = """
            My Story

            by Jane Doe

            CHAPTER I
            The Start

            It was a dark and
            stormy night, and every-
            thing was wet.

            Second paragraph.

            Chapter 2: The End

            "Done," she said -- at last...
        """.trimIndent()
        val b = BookParser.parse(text, "fallback")
        assertEquals("My Story", b.title)
        assertEquals("Jane Doe", b.author)
        assertEquals(2, b.chapters.size)
        assertEquals("CHAPTER I\nThe Start", b.chapters[0].title)
        assertEquals(listOf("It was a dark and stormy night, and everything was wet.", "Second paragraph."), b.chapters[0].paragraphs)
        assertEquals("Chapter 2: The End", b.chapters[1].title)
        assertEquals(listOf("“Done,” she said — at last…"), b.chapters[1].paragraphs)
    }

    @Test fun onePararaphPerLineWithoutBlankLines() {
        val long = "word ".repeat(40).trim()
        val b = BookParser.parse("$long\n$long\n$long", "Notes")
        assertEquals("Notes", b.title)
        assertEquals(1, b.chapters.size)
        assertEquals(3, b.chapters[0].paragraphs.size)
    }

    @Test fun sceneBreaksAndHeadings() {
        val b = BookParser.parse("One.\n\n* * *\n\nTwo.\n\nXII\n\nThree.", "T")
        assertEquals(listOf("One.", BookParser.SCENE_BREAK, "Two."), b.chapters[0].paragraphs)
        assertEquals("XII", b.chapters[1].title)
        assertTrue(BookParser.isChapterHeading("Part Two"))
        assertTrue(BookParser.isChapterHeading("Prologue"))
        assertFalse(BookParser.isChapterHeading("Chapter and verse were quoted to him all day long, he said."))
        assertFalse(BookParser.isChapterHeading("I went home."))
    }

    @Test fun quotesAndEncodings() {
        assertEquals("‘Tis Tom’s ‘book’", BookParser.typography("'Tis Tom's 'book'").replace("’Tis", "‘Tis"))
        assertEquals("café", BookParser.decode(byteArrayOf(0x63, 0x61, 0x66, 0xE9.toByte()))) // Windows-1252
        assertEquals("café", BookParser.decode("café".toByteArray()))
        assertEquals("My Great Story", BookParser.titleFromFileName("my_great-story.txt"))
    }
}
