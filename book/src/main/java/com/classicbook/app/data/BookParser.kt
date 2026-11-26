package com.classicbook.app.data

import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction

data class Chapter(val title: String?, val paragraphs: List<String>)

data class Book(val title: String, val author: String, val chapters: List<Chapter>) {
    val wordCount: Int get() = chapters.sumOf { c -> c.paragraphs.sumOf { p -> p.split(' ').count { it.isNotBlank() } } }
}

/** Turns a plain-text file into a [Book]: chapters, paragraphs and classic typography (curly quotes, dashes). */
object BookParser {

    /** Decodes bytes as UTF-8/UTF-16 (by BOM), falling back to Windows-1252 for old files. */
    fun decode(bytes: ByteArray): String {
        if (bytes.size >= 3 && bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte())
            return String(bytes, 3, bytes.size - 3, Charsets.UTF_8)
        if (bytes.size >= 2 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xFE.toByte())
            return String(bytes, 2, bytes.size - 2, Charsets.UTF_16LE)
        if (bytes.size >= 2 && bytes[0] == 0xFE.toByte() && bytes[1] == 0xFF.toByte())
            return String(bytes, 2, bytes.size - 2, Charsets.UTF_16BE)
        return try {
            Charsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes)).toString()
        } catch (_: CharacterCodingException) {
            String(bytes, Charset.forName("windows-1252"))
        }
    }

    /** A title from a file name: "my_great-story.txt" -> "My Great Story". */
    fun titleFromFileName(name: String?): String {
        val base = name?.substringAfterLast('/')?.substringBeforeLast('.')?.replace(Regex("[_\\-]+"), " ")?.trim()
        if (base.isNullOrEmpty()) return "Untitled"
        return base.split(Regex("\\s+")).joinToString(" ") { w ->
            if (w.any { it.isUpperCase() }) w else w.replaceFirstChar { it.uppercaseChar() }
        }
    }

    private val chapterWords =
        "chapter|chap\\.|chapitre|cap[ií]tulo|capitolo|kapitel|part|partie|book|livre|prologue|epilogue|épilogue|preface|préface|introduction|foreword|afterword|interlude|act|scene"
    private val numberWords =
        "one|two|three|four|five|six|seven|eight|nine|ten|eleven|twelve|thirteen|fourteen|fifteen|sixteen|seventeen|eighteen|nineteen|twenty|thirty|forty|fifty|first|second|third|fourth|fifth|sixth|seventh|eighth|ninth|tenth|last|final"

    private val headingWithWord = Regex(
        "^(?:$chapterWords)(?:\\s+(?:[0-9]+|[ivxlcdm]+|(?:$numberWords)(?:[- ](?:$numberWords))?))?\\s*(?:[.:—–-].*)?$",
        RegexOption.IGNORE_CASE,
    )
    private val romanOrNumberAlone = Regex("^(?:[IVXLCDM]+|[0-9]{1,3})\\.?$")
    private val sceneBreak = Regex("^(?:[*#~•·]\\s*){1,5}$|^-{3,}$|^_{3,}$")

    const val SCENE_BREAK = "\u0000scene"

    fun isChapterHeading(line: String): Boolean {
        val t = line.trim()
        if (t.isEmpty() || t.length > 70) return false
        if (romanOrNumberAlone.matches(t)) return true
        // "CHAPTER I." / "Chapter 3: The Storm" / "Prologue"
        return headingWithWord.matches(t) && !t.endsWith(",")
    }

    fun parse(text: String, fallbackTitle: String): Book {
        val lines = text.replace("\r\n", "\n").replace('\r', '\n').replace('\u000C', '\n')
            .lines().map { it.replace('\t', ' ').trimEnd() }

        // Blocks are runs of non-blank lines.
        val blocks = mutableListOf<List<String>>()
        var cur = mutableListOf<String>()
        for (l in lines) {
            if (l.isBlank()) { if (cur.isNotEmpty()) { blocks += cur; cur = mutableListOf() } } else cur += l
        }
        if (cur.isNotEmpty()) blocks += cur
        val paragraphsRaw = blocks.flatMap { splitBlock(it) }

        var title = fallbackTitle
        var author = ""
        val items = paragraphsRaw.map { joinLines(it) }.toMutableList()

        // A leading "Title" + "by Someone" becomes the title page.
        if (items.size >= 3 && items[0].length <= 80 && !isChapterHeading(items[0])) {
            val by = Regex("^(?:by|par|von|por|di)\\s+(.{2,60})$", RegexOption.IGNORE_CASE).find(items[1].trim())
            if (by != null) {
                title = items[0].trim()
                author = by.groupValues[1].trim()
                items.removeAt(0); items.removeAt(0)
            }
        }

        val chapters = mutableListOf<Chapter>()
        var chTitle: String? = null
        var paras = mutableListOf<String>()
        var i = 0
        while (i < items.size) {
            val p = items[i]
            if (isChapterHeading(p)) {
                if (paras.isNotEmpty() || chTitle != null) chapters += Chapter(chTitle, paras)
                var heading = p.trim()
                // "CHAPTER I" followed by a short subtitle line: "The Beginning"
                val next = items.getOrNull(i + 1)?.trim()
                if (next != null && next.length <= 60 && !next.endsWith('.') && !next.endsWith(',') &&
                    !isChapterHeading(next) && next.split(' ').size <= 8 && !heading.contains(Regex("[:.—–-]\\s*\\S"))
                ) {
                    heading = "$heading\n$next"; i++
                }
                chTitle = typography(heading.trimEnd('.'))
                paras = mutableListOf()
            } else if (sceneBreak.matches(p.trim())) {
                if (paras.isNotEmpty() && paras.last() != SCENE_BREAK) paras += SCENE_BREAK
            } else {
                paras += typography(p)
            }
            i++
        }
        if (paras.isNotEmpty() || chTitle != null) chapters += Chapter(chTitle, paras)
        if (chapters.isEmpty()) chapters += Chapter(null, emptyList())
        return Book(typography(title), typography(author), chapters.map { c -> c.copy(paragraphs = c.paragraphs.dropLastWhile { it == SCENE_BREAK }) })
    }

    /** A block can hold a heading line glued to its first paragraph, or several indented paragraphs. */
    private fun splitBlock(block: List<String>): List<List<String>> {
        // Long lines mean the file has one paragraph per line rather than hard-wrapped text.
        if (block.size > 1 && block.count { it.length > 100 } * 2 >= block.size) return block.map { listOf(it) }
        val out = mutableListOf<List<String>>()
        var cur = mutableListOf<String>()
        for ((idx, l) in block.withIndex()) {
            val startsIndented = idx > 0 && l.startsWith("  ") && !block[idx - 1].startsWith("  ")
            val heading = isChapterHeading(l) || sceneBreak.matches(l.trim())
            if ((heading || startsIndented) && cur.isNotEmpty()) { out += cur; cur = mutableListOf() }
            cur += l
            if (heading) { out += cur; cur = mutableListOf() }
        }
        if (cur.isNotEmpty()) out += cur
        return out
    }

    /** Joins hard-wrapped lines into one paragraph, mending words hyphenated at line ends. */
    private fun joinLines(lines: List<String>): String {
        val sb = StringBuilder()
        for (raw in lines) {
            val l = raw.trim()
            if (sb.isEmpty()) { sb.append(l); continue }
            if (sb.endsWith("-") && sb.length > 1 && sb[sb.length - 2].isLetter() && l.firstOrNull()?.isLowerCase() == true) {
                sb.setLength(sb.length - 1); sb.append(l)
            } else sb.append(' ').append(l)
        }
        return sb.toString().replace(Regex(" {2,}"), " ")
    }

    /** Straight quotes to curly ones, -- to em dashes, ... to ellipses. */
    fun typography(s: String): String {
        var t = s.replace("---", "—").replace("--", "—").replace("...", "…")
        val sb = StringBuilder(t.length)
        for ((i, c) in t.withIndex()) {
            val prev = if (i > 0) t[i - 1] else ' '
            val opening = prev.isWhitespace() || prev in "([{—–“‘-/"
            when (c) {
                '"' -> sb.append(if (opening) '“' else '”')
                '\'' -> sb.append(if (opening && t.getOrNull(i + 1)?.isLetterOrDigit() == true && !isElision(t, i)) '‘' else '’')
                else -> sb.append(c)
            }
        }
        t = sb.toString()
        return t
    }

    // 'tis, 'twas, 'em, '90s keep an apostrophe even at the start of a word
    private fun isElision(t: String, i: Int): Boolean {
        val rest = t.substring(i + 1).take(4).lowercase()
        return rest.startsWith("tis") || rest.startsWith("twas") || rest.startsWith("em ") || rest.startsWith("em.") ||
            rest.startsWith("em,") || (rest.length >= 2 && rest[0].isDigit() && rest[1].isDigit())
    }
}
