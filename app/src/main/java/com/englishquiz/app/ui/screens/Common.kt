package com.englishquiz.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.text.style.TextOverflow
import com.englishquiz.app.ui.AppViewModel
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    vm: AppViewModel,
    title: String,
    onBack: (() -> Unit)? = { vm.back() },
    actions: @Composable RowScope.() -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    val snackbar = remember { SnackbarHostState() }
    val msg = vm.message
    LaunchedEffect(msg) {
        if (msg != null) {
            vm.message = null // clear first, so the next screen doesn't show it again
            snackbar.showSnackbar(msg)
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = actions,
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = floatingActionButton,
        bottomBar = bottomBar,
        content = content,
    )
}

fun formatDate(ts: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(ts))

/** Shows a question picture: a photo stored as a data: URI, or an emoji / short text drawn large. */
@Composable
fun QuestionImage(image: String?, modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier, size: androidx.compose.ui.unit.Dp = 160.dp) {
    if (image.isNullOrBlank()) return
    if (image.startsWith("data:")) {
        val bmp = remember(image) { decodeDataUri(image)?.asImageBitmap() }
        if (bmp != null) {
            androidx.compose.foundation.Image(
                bitmap = bmp, contentDescription = "Question picture",
                contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                modifier = modifier.height(size).fillMaxWidth().clip(RoundedCornerShape(12.dp)),
            )
        }
    } else {
        Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(image, fontSize = (size.value * 0.45f).sp, lineHeight = (size.value * 0.55f).sp, textAlign = TextAlign.Center)
        }
    }
}

fun decodeDataUri(uri: String): android.graphics.Bitmap? = runCatching {
    val bytes = android.util.Base64.decode(uri.substringAfter(","), android.util.Base64.DEFAULT)
    android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}.getOrNull()

/** Downscales a picked photo and encodes it as a JPEG data: URI so it travels inside the units JSON. */
fun encodeImageAsDataUri(ctx: android.content.Context, uri: android.net.Uri, maxSide: Int = 640): String? = runCatching {
    val src = ctx.contentResolver.openInputStream(uri)!!.use { android.graphics.BitmapFactory.decodeStream(it) }
    val scale = minOf(1f, maxSide.toFloat() / maxOf(src.width, src.height))
    val bmp = if (scale < 1f) android.graphics.Bitmap.createScaledBitmap(src, (src.width * scale).toInt(), (src.height * scale).toInt(), true) else src
    val out = java.io.ByteArrayOutputStream()
    bmp.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, out)
    "data:image/jpeg;base64," + android.util.Base64.encodeToString(out.toByteArray(), android.util.Base64.NO_WRAP)
}.getOrNull()

/** Renders lesson text: "# " lines are headings, "• " / "- " lines are bullets, "**x**" is bold. */
@Composable
fun LessonText(content: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        content.lines().forEach { raw ->
            val line = raw.trimEnd()
            when {
                line.isBlank() -> Spacer(Modifier.height(4.dp))
                line.startsWith("# ") -> Text(
                    line.removePrefix("# "), style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp),
                )
                line.startsWith("• ") || line.startsWith("- ") -> Row {
                    Text("•  ", style = MaterialTheme.typography.bodyLarge)
                    Text(boldMarkup(line.drop(2)), style = MaterialTheme.typography.bodyLarge)
                }
                else -> Text(boldMarkup(line), style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

private fun boldMarkup(s: String) = androidx.compose.ui.text.buildAnnotatedString {
    val parts = s.split("**")
    parts.forEachIndexed { i, p ->
        if (i % 2 == 1) withStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold)) { append(p) } else append(p)
    }
}
