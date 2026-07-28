package io.github.nanima1.twilight.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import io.github.nanima1.twilight.domain.appearance.ThemePreset

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

private val SakuraSignalColors = darkColorScheme(
    primary = Color(0xFFFF7EB6),
    onPrimary = Color(0xFF330018),
    secondary = Color(0xFF72E6CF),
    onSecondary = Color(0xFF00201A),
    tertiary = Color(0xFFFFD166),
    onTertiary = Color(0xFF2B2000),
    background = Color(0xFF171218),
    onBackground = Color(0xFFFFF4F8),
    surface = Color(0xFF211A22),
    onSurface = Color(0xFFFFF4F8),
    surfaceVariant = Color(0xFF352A34),
    onSurfaceVariant = Color(0xFFE1C5D2),
    outlineVariant = Color(0xFF66515F)
)

private val MintCircuitColors = darkColorScheme(
    primary = Color(0xFF72F1B8),
    onPrimary = Color(0xFF002116),
    secondary = Color(0xFFFF8A7A),
    onSecondary = Color(0xFF35100B),
    tertiary = Color(0xFF7AA9FF),
    onTertiary = Color(0xFF071B3D),
    background = Color(0xFF101817),
    onBackground = Color(0xFFEAF9F3),
    surface = Color(0xFF182321),
    onSurface = Color(0xFFEAF9F3),
    surfaceVariant = Color(0xFF263633),
    onSurfaceVariant = Color(0xFFBBD4CC),
    outlineVariant = Color(0xFF455F58)
)

private val NeonShrineColors = darkColorScheme(
    primary = Color(0xFFFF6B5F),
    onPrimary = Color(0xFF350703),
    secondary = Color(0xFF59E1D8),
    onSecondary = Color(0xFF00201D),
    tertiary = Color(0xFFC9A7FF),
    onTertiary = Color(0xFF23103B),
    background = Color(0xFF151416),
    onBackground = Color(0xFFFFF3F3),
    surface = Color(0xFF201E21),
    onSurface = Color(0xFFFFF3F3),
    surfaceVariant = Color(0xFF343136),
    onSurfaceVariant = Color(0xFFD7CED6),
    outlineVariant = Color(0xFF5C5660)
)

private val LimePulseColors = darkColorScheme(
    primary = Color(0xFFC7F36B),
    onPrimary = Color(0xFF1A2600),
    secondary = Color(0xFFFF70AE),
    onSecondary = Color(0xFF330016),
    tertiary = Color(0xFF72B7FF),
    onTertiary = Color(0xFF001D36),
    background = Color(0xFF141516),
    onBackground = Color(0xFFF0F8EC),
    surface = Color(0xFF1E2021),
    onSurface = Color(0xFFF0F8EC),
    surfaceVariant = Color(0xFF303335),
    onSurfaceVariant = Color(0xFFCDD4D5),
    outlineVariant = Color(0xFF555C60)
)

data class ThemePreview(
    val name: String,
    val swatches: List<Color>
)

fun ThemePreset.preview(): ThemePreview = when (this) {
    ThemePreset.TWILIGHT -> ThemePreview("Twilight", listOf(Color(0xFF27E2E8), Color(0xFFF15DB3), Color(0xFFF8D06A)))
    ThemePreset.SAKURA_SIGNAL -> ThemePreview("Sakura signal", listOf(Color(0xFFFF7EB6), Color(0xFF72E6CF), Color(0xFFFFD166)))
    ThemePreset.MINT_CIRCUIT -> ThemePreview("Mint circuit", listOf(Color(0xFF72F1B8), Color(0xFFFF8A7A), Color(0xFF7AA9FF)))
    ThemePreset.NEON_SHRINE -> ThemePreview("Neon shrine", listOf(Color(0xFFFF6B5F), Color(0xFF59E1D8), Color(0xFFC9A7FF)))
    ThemePreset.LIME_PULSE -> ThemePreview("Lime pulse", listOf(Color(0xFFC7F36B), Color(0xFFFF70AE), Color(0xFF72B7FF)))
}

private fun ThemePreset.colorScheme(): ColorScheme = when (this) {
    ThemePreset.TWILIGHT -> TwilightColors
    ThemePreset.SAKURA_SIGNAL -> SakuraSignalColors
    ThemePreset.MINT_CIRCUIT -> MintCircuitColors
    ThemePreset.NEON_SHRINE -> NeonShrineColors
    ThemePreset.LIME_PULSE -> LimePulseColors
}

@Composable
fun TwilightTimerTheme(
    themePreset: ThemePreset = ThemePreset.TWILIGHT,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = themePreset.colorScheme(),
        typography = TwilightTypography,
        content = content
    )
}
