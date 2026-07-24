package io.github.nanima1.twilight.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TwilightColors = darkColorScheme(
    primary = Color(0xFF27E2E8),
    onPrimary = Color(0xFF081B20),
    secondary = Color(0xFFF8D06A),
    onSecondary = Color(0xFF261C00),
    tertiary = Color(0xFFF15DB3),
    onTertiary = Color(0xFF301020),
    background = Color(0xFF11131B),
    onBackground = Color(0xFFF0F3FA),
    surface = Color(0xFF1A1E29),
    onSurface = Color(0xFFF0F3FA),
    surfaceVariant = Color(0xFF272C38),
    onSurfaceVariant = Color(0xFFC5CBD8),
    outlineVariant = Color(0xFF46505E)
)

@Composable
fun TwilightTimerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TwilightColors,
        typography = TwilightTypography,
        content = content
    )
}
