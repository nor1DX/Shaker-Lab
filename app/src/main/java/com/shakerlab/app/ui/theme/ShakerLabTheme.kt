package com.shakerlab.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Gold = Color(0xFFF5C842)
private val Black = Color(0xFF000000)
private val BgDark = Color(0xFF0D0D0D)
private val Surface = Color(0xFF1A1A1A)
private val SurfaceVariant = Color(0xFF252525)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF999999)
private val Outline = Color(0xFF2E2E2E)
private val PrimaryContainer = Color(0xFF3A2E00)
private val SecondaryContainer = Color(0xFF2A2218)

private val ShakerLabColorScheme = darkColorScheme(
    primary = Gold,
    onPrimary = Black,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = Gold,
    secondary = Gold,
    onSecondary = Black,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = Gold,
    tertiary = Gold,
    onTertiary = Black,
    background = BgDark,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = Outline,
    outlineVariant = Outline,
)

@Composable
fun ShakerLabTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ShakerLabColorScheme,
        content = content
    )
}
