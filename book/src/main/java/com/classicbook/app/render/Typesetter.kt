package com.classicbook.app.render

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.StaticLayout
import android.text.TextPaint
import android.text.style.LeadingMarginSpan
import android.text.style.RelativeSizeSpan
import android.text.style.ScaleXSpan
import com.classicbook.app.data.Book
import com.classicbook.app.data.BookParser
import kotlin.math.ceil

/** The typefaces of the book: EB Garamond for the text, IM Fell English for titles and initials. */
class BookFonts(val regular: Typeface, val italic: Typeface, val semibold: Typeface, val display: Typeface)

/** Page geometry in PostScript points: a 5.5 × 8.5 in trade book page. */
object PageSize {
    const val W = 396f
    const val H = 612f
    const val MARGIN_X = 46f
    const val MARGIN_TOP = 58f
    const val MARGIN_BOTTOM = 62f
    const val TEXT_W = W - 2 * MARGIN_X
}

/** Text size choices for the reader (body size in points). */
enum class TextSize(val label: String, val points: Float) {
    Small("Small", 10.5f), Medium("Medium", 11.75f), Large("Large", 13.5f), Huge("Extra large", 15.5f)
}

sealed class Elem {
    /** Lines [from, to) of [layout], drawn with the layout's origin at ([x], [y]). */
    class Lines(val layout: StaticLayout, val from: Int, val to: Int, val x: Float, val y: Float) : Elem()
    class Glyphs(val text: String, val x: Float, val baseline: Float, val paint: TextPaint) : Elem()
    /** A printer's flourish centred on ([cx], [cy]). */
    class Ornament(val cx: Float, val cy: Float, val width: Float) : Elem()
}

class Page(val number: Int, val elems: List<Elem>, val header: String?, val folio: Boolean, val chapterIndex: Int)

class TypesetBook(val book: Book, val pages: List<Page>, val chapterStarts: List<Int>)

/** Lays out a [Book] into fixed-size pages, the way a classic printed novel looks. */
class Typesetter(private val fonts: BookFonts, size: TextSize) {
    private val pt = size.points
    private val lineHeight = pt * 1.42f
    val ink = 0xFF2B2118.toInt()
    val accent = 0xFF7A2E1F.toInt()

    private fun paint(tf: Typeface, sizePt: Float, color: Int = ink, spacing: Float = 0f) =
        TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG or Paint.LINEAR_TEXT_FLAG).apply {
            typeface = tf; textSize = sizePt; this.color = color; letterSpacing = spacing
            isFakeBoldText = false
        }

    private val body = paint(fonts.regular, pt)
    private val headerPaint = paint(fonts.italic, pt * 0.78f, 0xFF5A4A3A.toInt(), 0.02f)

    private fun layout(
        text: CharSequence, p: TextPaint, width: Int = PageSize.TEXT_W.toInt(),
        align: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL, justify: Boolean = false,
        lineH: Float? = null,
    ): StaticLayout {
        val fm = p.fontMetrics
        val natural = fm.descent - fm.ascent
        val b = StaticLayout.Builder.obtain(text, 0, text.length, p, width)
            .setAlignment(align)
            .setIncludePad(false)
            .setLineSpacing((lineH ?: (natural * 1.12f)) - natural, 1f)
            .setBreakStrategy(Layout.BREAK_STRATEGY_HIGH_QUALITY)
            .setHyphenationFrequency(if (justify) Layout.HYPHENATION_FREQUENCY_NORMAL else Layout.HYPHENATION_FREQUENCY_NONE)
        if (justify) b.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD)
        return b.build()
    }

    private class DropCapMargin(private val lines: Int, private val width: Int) : LeadingMarginSpan.LeadingMarginSpan2 {
        override fun getLeadingMarginLineCount() = lines
        override fun getLeadingMargin(first: Boolean) = if (first) width else 0
        override fun drawLeadingMargin(c: Canvas?, p: Paint?, x: Int, dir: Int, top: Int, baseline: Int, bottom: Int,
                                       text: CharSequence?, start: Int, end: Int, first: Boolean, layout: Layout?) {}
    }

    fun typeset(book: Book): TypesetBook {
        val pages = mutableListOf<Page>()
        val chapterStarts = mutableListOf<Int>()
        val top = PageSize.MARGIN_TOP
        val bottom = PageSize.H - PageSize.MARGIN_BOTTOM
        val left = PageSize.MARGIN_X

        var elems = mutableListOf<Elem>()
        var y = top
        var header: String? = null
        var folio = true
        var chapterIdx = -1
        fun flush() {
            if (elems.isNotEmpty()) pages += Page(pages.size + 1, elems, header, folio, chapterIdx)
            elems = mutableListOf(); y = top
        }

        // ---- Title page
        run {
            folio = false; header = null
            val titleP = paint(fonts.display, if (book.title.length > 30) 26f else 32f, ink, 0.01f)
            val tl = layout(book.title, titleP, align = Layout.Alignment.ALIGN_CENTER, lineH = titleP.textSize * 1.15f)
            var ty = PageSize.H * 0.30f
            elems += Elem.Ornament(PageSize.W / 2, ty - 26f, 120f)
            elems += Elem.Lines(tl, 0, tl.lineCount, left, ty)
            ty += tl.height + 22f
            if (book.author.isNotBlank()) {
                val ap = paint(fonts.italic, 15f, ink)
                val al = layout(book.author, ap, align = Layout.Alignment.ALIGN_CENTER)
                elems += Elem.Lines(al, 0, al.lineCount, left, ty)
                ty += al.height + 18f
            }
            elems += Elem.Ornament(PageSize.W / 2, ty + 8f, 70f)
            flush()
        }

        for ((ci, ch) in book.chapters.withIndex()) {
            chapterIdx = ci
            header = null; folio = true
            chapterStarts += pages.size
            val runningTitle = ch.title?.substringAfter('\n')?.takeIf { it.isNotBlank() } ?: book.title

            // ---- Chapter opening: sunk start, heading, flourish
            if (ch.title != null) {
                y = top + (bottom - top) * 0.16f
                val parts = ch.title.split('\n')
                if (parts.size > 1) {
                    val lp = paint(fonts.regular, pt * 0.95f, ink, 0.22f)
                    val l1 = layout(parts[0].uppercase(), lp, align = Layout.Alignment.ALIGN_CENTER)
                    elems += Elem.Lines(l1, 0, l1.lineCount, left, y); y += l1.height + 10f
                    val sp = paint(fonts.display, pt * 1.75f, ink)
                    val l2 = layout(parts.drop(1).joinToString(" "), sp, align = Layout.Alignment.ALIGN_CENTER, lineH = sp.textSize * 1.2f)
                    elems += Elem.Lines(l2, 0, l2.lineCount, left, y); y += l2.height
                } else {
                    val sp = paint(fonts.display, pt * 1.75f, ink, 0.03f)
                    val l = layout(parts[0], sp, align = Layout.Alignment.ALIGN_CENTER, lineH = sp.textSize * 1.2f)
                    elems += Elem.Lines(l, 0, l.lineCount, left, y); y += l.height
                }
                y += 20f
                elems += Elem.Ornament(PageSize.W / 2, y, 64f)
                y += 30f
            } else if (ci > 0) {
                elems += Elem.Ornament(PageSize.W / 2, y + 8f, 64f); y += 34f
            }

            var afterBreak = true
            var first = true
            for (para in ch.paragraphs) {
                if (para == BookParser.SCENE_BREAK) {
                    if (y + lineHeight * 2 > bottom) { flush(); header = runningTitle } else {
                        elems += Elem.Ornament(PageSize.W / 2, y + lineHeight * 0.75f, 36f)
                        y += lineHeight * 1.5f
                    }
                    afterBreak = true
                    continue
                }
                val dropCap = first && para.firstOrNull { it.isLetterOrDigit() } != null
                val text: CharSequence
                var capElem: ((Float, StaticLayout) -> Elem)? = null
                if (dropCap) {
                    val (sb, place) = buildDropCap(para)
                    text = sb; capElem = place
                } else {
                    text = SpannableStringBuilder(para).apply {
                        if (!afterBreak) setSpan(LeadingMarginSpan.Standard((pt * 1.5f).toInt(), 0), 0, length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                    }
                }
                val lay = layout(text, body, justify = true, lineH = lineHeight)
                val n = lay.lineCount
                var k = 0
                while (k < n) {
                    var fit = 0
                    while (k + fit < n && y + (lay.getLineBottom(k + fit) - lay.getLineTop(k)) <= bottom + 0.5f) fit++
                    val pageEmpty = elems.none { it is Elem.Lines || it is Elem.Glyphs }
                    val keep = if (capElem != null && k == 0) minOf(n, DROP_LINES) else 1
                    if (!pageEmpty && (fit < keep || (k == 0 && fit == 1 && n > 1))) {
                        flush(); header = runningTitle; continue
                    }
                    if (fit == 0) fit = 1 // a line taller than the page: place it anyway
                    // Avoid a single last line alone at the top of the next page
                    if (k + fit == n - 1 && fit >= 3) fit--
                    val oy = y - lay.getLineTop(k)
                    elems += Elem.Lines(lay, k, k + fit, left, oy)
                    if (k == 0 && capElem != null) elems += capElem(oy, lay)
                    y += lay.getLineBottom(k + fit - 1) - lay.getLineTop(k)
                    k += fit
                    if (k < n) { flush(); header = runningTitle }
                }
                first = false
                afterBreak = false
            }
            flush()
            if (chapterStarts.last() == pages.size) chapterStarts.removeAt(chapterStarts.size - 1)
        }
        return TypesetBook(book, pages, chapterStarts)
    }

    /** First letter as a three-line initial; the rest of the first word in small capitals. */
    private fun buildDropCap(para: String): Pair<CharSequence, (Float, StaticLayout) -> Elem> {
        val firstLetter = para.indexOfFirst { it.isLetterOrDigit() }
        val capText = para.substring(0, firstLetter + 1)
        val rest = para.substring(firstLetter + 1)
        // Cap height of the initial spans from the top of line 1's capitals to line 3's baseline.
        val bounds = Rect()
        val probe = paint(fonts.display, 100f)
        probe.getTextBounds("H", 0, 1, bounds)
        val capRatio = bounds.height() / 100f
        body.getTextBounds("H", 0, 1, bounds)
        val bodyCap = bounds.height().toFloat().coerceAtLeast(pt * 0.6f)
        val capPaint = paint(fonts.display, ((DROP_LINES - 1) * lineHeight + bodyCap) / capRatio, accent)
        val margin = ceil(capPaint.measureText(capText) + pt * 0.35f).toInt()

        val sb = SpannableStringBuilder()
        val wordEnd = rest.indexOfFirst { it == ' ' }.let { if (it < 0) rest.length else it }
        // Small capitals for the rest of the first word (and the next one if it is short)
        val secondEnd = rest.indexOf(' ', wordEnd + 1).let { if (it < 0) rest.length else it }
        val scEnd = if (wordEnd < 4 && secondEnd - wordEnd < 12) secondEnd else wordEnd
        sb.append(rest.substring(0, scEnd).uppercase())
        sb.setSpan(RelativeSizeSpan(0.8f), 0, sb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        sb.setSpan(ScaleXSpan(1.04f), 0, sb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        sb.append(rest.substring(scEnd))
        sb.setSpan(DropCapMargin(DROP_LINES, margin), 0, sb.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        return sb to { oy: Float, lay: StaticLayout ->
            val lastLine = minOf(DROP_LINES, lay.lineCount) - 1
            Elem.Glyphs(capText, PageSize.MARGIN_X - 1f, oy + lay.getLineBaseline(lastLine), capPaint)
        }
    }

    // ---- Drawing (screen and PDF share this)

    private val paperTop = 0xFFF5EBCF.toInt()
    private val paperEdge = 0xFFE9D7AA.toInt()

    fun draw(canvas: Canvas, page: Page, book: Book) {
        val bg = Paint(Paint.ANTI_ALIAS_FLAG)
        bg.shader = android.graphics.RadialGradient(
            PageSize.W / 2, PageSize.H * 0.45f, PageSize.H * 0.75f,
            intArrayOf(paperTop, paperTop, paperEdge), floatArrayOf(0f, 0.55f, 1f), android.graphics.Shader.TileMode.CLAMP,
        )
        canvas.drawRect(0f, 0f, PageSize.W, PageSize.H, bg)

        val header = page.header?.let { if (page.number % 2 == 0) book.title else it }
        if (header != null) {
            val t = header.uppercase().let { if (it.length > 48) it.take(46) + "…" else it }
            val w = headerPaint.measureText(t)
            canvas.drawText(t, (PageSize.W - w) / 2, PageSize.MARGIN_TOP - 22f, headerPaint)
        }
        if (page.folio) {
            val t = page.number.toString()
            val fp = paint(fonts.regular, pt * 0.85f, 0xFF5A4A3A.toInt())
            canvas.drawText(t, (PageSize.W - fp.measureText(t)) / 2, PageSize.H - PageSize.MARGIN_BOTTOM + 34f, fp)
        }
        for (e in page.elems) when (e) {
            is Elem.Lines -> {
                canvas.save()
                canvas.translate(e.x, e.y)
                canvas.clipRect(-PageSize.MARGIN_X, e.layout.getLineTop(e.from).toFloat(), e.layout.width + PageSize.MARGIN_X,
                    e.layout.getLineBottom(e.to - 1).toFloat())
                e.layout.draw(canvas)
                canvas.restore()
            }
            is Elem.Glyphs -> canvas.drawText(e.text, e.x, e.baseline, e.paint)
            is Elem.Ornament -> drawOrnament(canvas, e)
        }
    }

    private val ornamentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accent }

    /** A thin rule tapering out on both sides of a small diamond with dots. */
    private fun drawOrnament(c: Canvas, o: Elem.Ornament) {
        val p = ornamentPaint
        val half = o.width / 2
        val d = 4.2f
        if (half > 24f) {
            val path = android.graphics.Path()
            for (s in listOf(-1f, 1f)) {
                path.moveTo(o.cx + s * (d + 6f), o.cy - 0.7f)
                path.lineTo(o.cx + s * half, o.cy)
                path.lineTo(o.cx + s * (d + 6f), o.cy + 0.7f)
                path.close()
            }
            c.drawPath(path, p)
            c.drawCircle(o.cx - half - 3f, o.cy, 1.3f, p)
            c.drawCircle(o.cx + half + 3f, o.cy, 1.3f, p)
        } else {
            c.drawCircle(o.cx - d - 7f, o.cy, 1.5f, p)
            c.drawCircle(o.cx + d + 7f, o.cy, 1.5f, p)
        }
        val dia = android.graphics.Path().apply {
            moveTo(o.cx, o.cy - d); lineTo(o.cx + d, o.cy); lineTo(o.cx, o.cy + d); lineTo(o.cx - d, o.cy); close()
        }
        c.drawPath(dia, p)
    }

    companion object { const val DROP_LINES = 3 }
}
