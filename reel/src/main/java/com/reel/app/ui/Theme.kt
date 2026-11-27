package com.reel.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object ReelColors {
    val Bg = Color(0xFF0E0E11)
    val Surface = Color(0xFF17171C)
    val SurfaceHigh = Color(0xFF212128)
    val Line = Color(0xFF2A2A33)
    val Text = Color(0xFFF2F1EE)
    val Muted = Color(0xFF9B9AA3)
    val Accent = Color(0xFFE8B04B)
    val OnAccent = Color(0xFF1A1405)
    val Good = Color(0xFF6FCF97)
    val Love = Color(0xFFEF6F6C)
}

private val scheme = darkColorScheme(
    primary = ReelColors.Accent,
    onPrimary = ReelColors.OnAccent,
    secondary = ReelColors.Accent,
    onSecondary = ReelColors.OnAccent,
    secondaryContainer = ReelColors.SurfaceHigh,
    onSecondaryContainer = ReelColors.Text,
    background = ReelColors.Bg,
    onBackground = ReelColors.Text,
    surface = ReelColors.Bg,
    onSurface = ReelColors.Text,
    surfaceVariant = ReelColors.Surface,
    onSurfaceVariant = ReelColors.Muted,
    surfaceContainer = ReelColors.Surface,
    surfaceContainerLow = ReelColors.Surface,
    surfaceContainerHigh = ReelColors.SurfaceHigh,
    surfaceContainerHighest = ReelColors.SurfaceHigh,
    outline = ReelColors.Line,
    outlineVariant = ReelColors.Line,
    error = ReelColors.Love,
)

private val base = Typography()
private val typography = Typography(
    headlineLarge = base.headlineLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
    headlineMedium = base.headlineMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
    headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
    titleLarge = base.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.4.sp),
)

@Composable
fun ReelTheme(content: @Composable () -> Unit) = MaterialTheme(colorScheme = scheme, typography = typography, content = content)
