package com.classicbook.app.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.classicbook.app.R
import com.classicbook.app.render.Page
import com.classicbook.app.render.PageSize
import com.classicbook.app.render.TextSize
import com.classicbook.app.render.TypesetBook
import com.classicbook.app.render.Typesetter
import kotlinx.coroutines.launch

val Garamond = FontFamily(
    Font(R.font.ebgaramond_regular),
    Font(R.font.ebgaramond_italic, style = FontStyle.Italic),
    Font(R.font.ebgaramond_semibold, FontWeight.SemiBold),
)
val Fell = FontFamily(Font(R.font.imfell_regular))

val Parchment = Color(0xFFF3E7C9)
val ParchmentDark = Color(0xFFE6D3A6)
val Desk = Color(0xFF3B2A1E)
val Ink = Color(0xFF2B2118)
val Burgundy = Color(0xFF7A2E1F)

@Composable
fun ClassicTheme(content: @Composable () -> Unit) {
    val base = Typography()
    fun TextStyle.g() = copy(fontFamily = Garamond)
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Burgundy, onPrimary = Parchment, background = Parchment, onBackground = Ink,
            surface = Parchment, onSurface = Ink, surfaceContainer = Parchment, surfaceContainerHigh = Parchment,
            surfaceContainerHighest = ParchmentDark, surfaceVariant = ParchmentDark, onSurfaceVariant = Color(0xFF5A4A3A),
            secondaryContainer = ParchmentDark, outline = Color(0xFF8C7A5E),
        ),
        typography = Typography(
            displayLarge = base.displayLarge.g(), displayMedium = base.displayMedium.g(), displaySmall = base.displaySmall.g(),
            headlineLarge = base.headlineLarge.g(), headlineMedium = base.headlineMedium.g(), headlineSmall = base.headlineSmall.g(),
            titleLarge = base.titleLarge.g().copy(fontSize = 22.sp), titleMedium = base.titleMedium.g().copy(fontSize = 18.sp),
            titleSmall = base.titleSmall.g(), bodyLarge = base.bodyLarge.g().copy(fontSize = 18.sp),
            bodyMedium = base.bodyMedium.g().copy(fontSize = 16.sp), bodySmall = base.bodySmall.g().copy(fontSize = 14.sp),
            labelLarge = base.labelLarge.g().copy(fontSize = 17.sp), labelMedium = base.labelMedium.g(), labelSmall = base.labelSmall.g(),
        ),
        content = content,
    )
}

@Composable
fun App(vm: BookViewModel) {
    ClassicTheme {
        val snack = remember { SnackbarHostState() }
        LaunchedEffect(vm.message) { vm.message?.let { snack.showSnackbar(it); vm.message = null } }
        Scaffold(snackbarHost = { SnackbarHost(snack) }, containerColor = Parchment) { pad ->
            Box(Modifier.padding(pad).fillMaxSize()) {
                when (val s = vm.screen) {
                    Screen.Home -> HomeScreen(vm)
                    is Screen.Working -> Working(s.message)
                    Screen.Reader -> ReaderScreen(vm)
                }
            }
        }
    }
}

@Composable
private fun Flourish(width: Float = 140f) {
    Canvas(Modifier.size(width.dp, 12.dp)) {
        val cy = size.height / 2; val cx = size.width / 2; val d = 5.dp.toPx()
        val path = Path()
        for (s in listOf(-1f, 1f)) {
            path.moveTo(cx + s * (d + 8.dp.toPx()), cy - 1f)
            path.lineTo(cx + s * (size.width / 2 - 4.dp.toPx()), cy)
            path.lineTo(cx + s * (d + 8.dp.toPx()), cy + 1f)
            path.close()
        }
        path.moveTo(cx, cy - d); path.lineTo(cx + d, cy); path.lineTo(cx, cy + d); path.lineTo(cx - d, cy); path.close()
        drawPath(path, Burgundy)
        drawCircle(Burgundy, 1.8.dp.toPx(), Offset(2.dp.toPx(), cy))
        drawCircle(Burgundy, 1.8.dp.toPx(), Offset(size.width - 2.dp.toPx(), cy))
    }
}

@Composable
fun HomeScreen(vm: BookViewModel) {
    val pick = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> uri?.let(vm::openUri) }
    Column(
        Modifier.fillMaxSize().background(Parchment).padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Flourish()
        Spacer(Modifier.height(18.dp))
        Text("Classic Book", fontFamily = Fell, fontSize = 46.sp, color = Ink, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(
            "Turn any text file into a book,\nprinted the old-fashioned way.",
            style = MaterialTheme.typography.bodyLarge, fontStyle = FontStyle.Italic, color = Color(0xFF5A4A3A),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(18.dp))
        Flourish(70f)
        Spacer(Modifier.height(40.dp))
        Button(
            onClick = { pick.launch(arrayOf("text/plain", "text/*")) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
            shape = RoundedCornerShape(6.dp),
        ) { Text("Open a text file", fontSize = 19.sp) }
        val title = vm.savedTitle
        if (title != null) {
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = vm::continueReading, modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
                shape = RoundedCornerShape(6.dp),
            ) {
                Text("Continue: ", fontSize = 17.sp)
                Text(title, fontSize = 17.sp, fontStyle = FontStyle.Italic, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = vm::openSample) { Text("Read a sample book", fontSize = 17.sp) }
        Spacer(Modifier.height(36.dp))
        Text(
            "Chapters start on a new page when a line reads “Chapter 1”, “CHAPTER IV”, “Prologue”, “Part Two” or just “XII”. " +
                "Leave a blank line between paragraphs. A line of * * * marks a scene break.",
            style = MaterialTheme.typography.bodySmall, color = Color(0xFF6B5A46), textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun Working(msg: String) {
    Column(Modifier.fillMaxSize().background(Parchment), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        CircularProgressIndicator(color = Burgundy)
        Spacer(Modifier.height(16.dp))
        Text(msg, style = MaterialTheme.typography.bodyLarge, fontStyle = FontStyle.Italic)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(vm: BookViewModel) {
    val tb = vm.typeset ?: return
    val setter = vm.setter ?: return
    val pager = rememberPagerState(initialPage = vm.startPage) { tb.pages.size }
    val scope = rememberCoroutineScope()
    LaunchedEffect(pager.currentPage) { vm.pageChanged(pager.currentPage) }
    BackHandler { vm.close() }

    var menu by remember { mutableStateOf(false) }
    var sizeMenu by remember { mutableStateOf(false) }
    var contents by remember { mutableStateOf(false) }
    var details by remember { mutableStateOf(false) }
    val save = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        uri?.let(vm::exportPdf)
    }

    Column(Modifier.fillMaxSize().background(Desk)) {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Desk, titleContentColor = Parchment,
                navigationIconContentColor = Parchment, actionIconContentColor = Parchment),
            navigationIcon = { IconButton(onClick = vm::close) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
            title = { Text(tb.book.title, fontFamily = Fell, fontSize = 21.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            actions = {
                IconButton(onClick = { contents = true }) { Icon(Icons.AutoMirrored.Filled.List, "Contents") }
                Box {
                    TextButton(onClick = { sizeMenu = true }) { Text("Aa", color = Parchment, fontSize = 20.sp) }
                    DropdownMenu(sizeMenu, { sizeMenu = false }, containerColor = Parchment) {
                        TextSize.entries.forEach { s ->
                            DropdownMenuItem(
                                text = { Text((if (s == vm.textSize) "✓  " else "     ") + s.label, fontSize = (s.points * 1.4f).sp) },
                                onClick = { sizeMenu = false; if (s != vm.textSize) vm.changeTextSize(s, pager.currentPage) },
                            )
                        }
                    }
                }
                Box {
                    IconButton(onClick = { menu = true }) { Icon(Icons.Default.MoreVert, "More") }
                    DropdownMenu(menu, { menu = false }, containerColor = Parchment) {
                        DropdownMenuItem(text = { Text("Save as PDF") }, onClick = {
                            menu = false; save.launch(tb.book.title.replace(Regex("[\\\\/:*?\"<>|]"), "") + ".pdf")
                        })
                        DropdownMenuItem(text = { Text("Title & author") }, onClick = { menu = false; details = true })
                    }
                }
            },
        )
        HorizontalPager(pager, Modifier.weight(1f).fillMaxWidth(), beyondViewportPageCount = 1) { i ->
            Box(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp), contentAlignment = Alignment.Center) {
                BookPage(tb.pages[i], tb, setter)
            }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            var drag by remember { mutableStateOf<Float?>(null) }
            Slider(
                value = drag ?: pager.currentPage.toFloat(),
                onValueChange = { drag = it },
                onValueChangeFinished = { drag?.let { scope.launch { pager.scrollToPage(it.toInt()) } }; drag = null },
                valueRange = 0f..maxOf(1, tb.pages.size - 1).toFloat(),
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(thumbColor = Parchment, activeTrackColor = Parchment.copy(alpha = 0.8f),
                    inactiveTrackColor = Parchment.copy(alpha = 0.25f)),
            )
            Spacer(Modifier.width(12.dp))
            Text("${(drag?.toInt() ?: pager.currentPage) + 1} / ${tb.pages.size}", color = Parchment, fontFamily = Garamond, fontSize = 16.sp)
        }
    }

    if (contents) {
        ModalBottomSheet(onDismissRequest = { contents = false }, containerColor = Parchment) {
            Text("Contents", fontFamily = Fell, fontSize = 28.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            LazyColumn(Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                itemsIndexed(tb.chapterStarts) { idx, start ->
                    val ch = tb.book.chapters[tb.pages[start].chapterIndex]
                    val name = ch.title?.replace("\n", " · ") ?: if (idx == 0) "Beginning" else "Section ${idx + 1}"
                    Row(
                        Modifier.fillMaxWidth().clickable { contents = false; scope.launch { pager.scrollToPage(start) } }
                            .padding(horizontal = 28.dp, vertical = 12.dp),
                    ) {
                        Text(name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text("${start + 1}", style = MaterialTheme.typography.bodyLarge, fontStyle = FontStyle.Italic)
                    }
                }
            }
        }
    }

    if (details) {
        var title by remember { mutableStateOf(tb.book.title) }
        var author by remember { mutableStateOf(tb.book.author) }
        AlertDialog(
            onDismissRequest = { details = false },
            containerColor = Parchment,
            title = { Text("Title page", fontFamily = Fell) },
            text = {
                Column {
                    OutlinedTextField(title, { title = it }, label = { Text("Title") }, singleLine = true)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(author, { author = it }, label = { Text("Author") }, singleLine = true)
                }
            },
            confirmButton = { TextButton(onClick = { details = false; vm.changeDetails(title, author, pager.currentPage) }) { Text("Apply") } },
            dismissButton = { TextButton(onClick = { details = false }) { Text("Cancel") } },
        )
    }
}

/** One printed page, drawn by the same code that writes the PDF. */
@Composable
fun BookPage(page: Page, tb: TypesetBook, setter: Typesetter) {
    BoxWithConstraints(contentAlignment = Alignment.Center) {
        val ratio = PageSize.W / PageSize.H
        val w = if (maxWidth / maxHeight < ratio) maxWidth else maxHeight * ratio
        Canvas(Modifier.size(w, w / ratio).shadow(10.dp, RoundedCornerShape(2.dp))) {
            drawIntoCanvas { c ->
                val nc = c.nativeCanvas
                nc.save()
                nc.scale(size.width / PageSize.W, size.height / PageSize.H)
                setter.draw(nc, page, tb.book)
                nc.restore()
            }
        }
    }
}
