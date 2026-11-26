package com.classicbook.app.render

import android.graphics.pdf.PdfDocument
import java.io.OutputStream

/** Writes the typeset pages as a PDF (5.5 × 8.5 in), the same pages the reader shows. */
object PdfExporter {
    fun write(book: TypesetBook, setter: Typesetter, out: OutputStream, onProgress: (Float) -> Unit = {}) {
        val doc = PdfDocument()
        try {
            for ((i, page) in book.pages.withIndex()) {
                val info = PdfDocument.PageInfo.Builder(PageSize.W.toInt(), PageSize.H.toInt(), i + 1).create()
                val p = doc.startPage(info)
                setter.draw(p.canvas, page, book.book)
                doc.finishPage(p)
                onProgress((i + 1f) / book.pages.size)
            }
            doc.writeTo(out)
        } finally {
            doc.close()
        }
    }
}
