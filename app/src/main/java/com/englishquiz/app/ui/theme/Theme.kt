package com.englishquiz.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val CorrectGreen = Color(0xFF2E7D32)
val WrongRed = Color(0xFFC62828)

private val Light = lightColorScheme(
    primary = Color(0xFF1E5AA8),
    secondary = Color(0xFF8A6D00),
    tertiary = Color(0xFF00796B),
)
private val Dark = darkColorScheme(
    primary = Color(0xFFA8C8FF),
    secondary = Color(0xFFEAC248),
    tertiary = Color(0xFF7FD8C8),
)

@Composable
fun EnglishQuizTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val ctx = LocalContext.current
    val scheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> if (dark) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        dark -> Dark
        else -> Light
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
