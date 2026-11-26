package com.classicbook.app.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.classicbook.app.R
import com.classicbook.app.data.Book
import com.classicbook.app.data.BookParser
import com.classicbook.app.render.BookFonts
import com.classicbook.app.render.PdfExporter
import com.classicbook.app.render.TextSize
import com.classicbook.app.render.TypesetBook
import com.classicbook.app.render.Typesetter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

sealed class Screen {
    data object Home : Screen()
    data class Working(val message: String) : Screen()
    data object Reader : Screen()
}

class BookViewModel(app: Application) : AndroidViewModel(app) {
    private val ctx: Context get() = getApplication()
    private val prefs = app.getSharedPreferences("book", Context.MODE_PRIVATE)
    private val saved = File(app.filesDir, "current.txt")

    val fonts: BookFonts by lazy {
        fun f(id: Int) = ResourcesCompat.getFont(ctx, id)!!
        BookFonts(f(R.font.ebgaramond_regular), f(R.font.ebgaramond_italic), f(R.font.ebgaramond_semibold), f(R.font.imfell_regular))
    }

    var screen by mutableStateOf<Screen>(Screen.Home); private set
    var typeset by mutableStateOf<TypesetBook?>(null); private set
    var setter by mutableStateOf<Typesetter?>(null); private set
    var textSize by mutableStateOf(runCatching { TextSize.valueOf(prefs.getString("size", null)!!) }.getOrDefault(TextSize.Medium)); private set
    var message by mutableStateOf<String?>(null)
    /** Page the reader should show after a (re)layout. */
    var startPage by mutableStateOf(0); private set

    val savedTitle: String? get() = if (saved.exists()) prefs.getString("title", null) else null
    val savedPage: Int get() = prefs.getInt("page", 0)

    private var text: String = ""
    private var book: Book? = null

    fun openUri(uri: Uri) = load("Opening the file…") {
        val name = ctx.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
            if (c.moveToFirst()) c.getString(0) else null
        } ?: uri.lastPathSegment
        val bytes = ctx.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: error("Cannot read this file")
        newText(BookParser.decode(bytes), BookParser.titleFromFileName(name))
    }

    fun openText(content: String, title: String) = load("Setting the type…") { newText(content, title) }

    fun openSample() = load("Setting the type…") {
        val t = ctx.resources.openRawResource(R.raw.sample).use { it.readBytes() }
        newText(BookParser.decode(t), "The Lighthouse Keeper")
    }

    fun continueReading() = load("Setting the type…") {
        text = saved.readText()
        val b = BookParser.parse(text, prefs.getString("fileTitle", null) ?: "Untitled")
        book = b.copy(title = prefs.getString("title", null) ?: b.title, author = prefs.getString("author", null) ?: b.author)
        startPage = savedPage
        layout()
    }

    private fun newText(content: String, title: String) {
        text = content
        val b = BookParser.parse(content, title)
        if (b.chapters.all { it.paragraphs.isEmpty() }) error("This file has no text in it")
        book = b
        saved.writeText(content)
        prefs.edit().putString("fileTitle", title).putString("title", b.title).putString("author", b.author).putInt("page", 0).apply()
        startPage = 0
        layout()
    }

    private fun layout() {
        val s = Typesetter(fonts, textSize)
        val t = s.typeset(book!!)
        startPage = startPage.coerceIn(0, t.pages.size - 1)
        setter = s; typeset = t
    }

    private fun load(msg: String, block: () -> Unit) {
        screen = Screen.Working(msg)
        viewModelScope.launch {
            val r = withContext(Dispatchers.Default) { runCatching(block) }
            r.onSuccess { screen = Screen.Reader }.onFailure {
                message = it.message ?: "Something went wrong"
                screen = if (typeset != null) Screen.Reader else Screen.Home
            }
        }
    }

    fun pageChanged(p: Int) { prefs.edit().putInt("page", p).apply() }

    fun changeTextSize(size: TextSize, currentPage: Int) {
        val old = typeset ?: return
        val fraction = currentPage.toFloat() / maxOf(1, old.pages.size - 1)
        textSize = size
        prefs.edit().putString("size", size.name).apply()
        load("Setting the type…") {
            layout()
            startPage = (fraction * (typeset!!.pages.size - 1)).toInt()
        }
    }

    fun changeDetails(title: String, author: String, currentPage: Int) {
        val b = book ?: return
        book = b.copy(title = title.trim().ifEmpty { b.title }, author = author.trim())
        prefs.edit().putString("title", book!!.title).putString("author", book!!.author).apply()
        startPage = currentPage
        load("Setting the type…") { layout() }
    }

    fun exportPdf(uri: Uri) {
        val t = typeset ?: return
        val s = setter ?: return
        screen = Screen.Working("Printing the PDF…")
        viewModelScope.launch {
            val r = withContext(Dispatchers.IO) {
                runCatching { ctx.contentResolver.openOutputStream(uri)!!.use { PdfExporter.write(t, s, it) } }
            }
            message = r.fold({ "Saved as PDF (${t.pages.size} pages)" }, { "Could not save the PDF: ${it.message}" })
            screen = Screen.Reader
        }
    }

    fun close() { screen = Screen.Home }
}
