package io.github.kimbohy.mobilecontroller.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFFE0A030),
    onPrimary = Color(0xFF1A1200),
    background = Color(0xFF0E0F12),
    onBackground = Color(0xFFECECEC),
    surface = Color(0xFF16181D),
    onSurface = Color(0xFFECECEC),
    surfaceVariant = Color(0xFF2A2D33),
    onSurfaceVariant = Color(0xFFC9CCD1),
    outline = Color(0xFF6B7078),
)

/** Thème unique : sombre, fort contraste (pensé pour la scène). */
@Composable
fun MobileControllerTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors, content = content)
}