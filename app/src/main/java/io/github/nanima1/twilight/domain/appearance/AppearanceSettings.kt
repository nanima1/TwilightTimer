package io.github.nanima1.twilight.domain.appearance

enum class ThemePreset(val id: String) {
    TWILIGHT("twilight"),
    SAKURA_SIGNAL("sakura_signal"),
    MINT_CIRCUIT("mint_circuit");

    companion object {
        fun fromId(id: String?): ThemePreset = entries.firstOrNull { it.id == id } ?: TWILIGHT
    }
}

data class AppearanceSettings(
    val themePreset: ThemePreset = ThemePreset.TWILIGHT,
    val wallpaperUri: String? = null,
    val wallpaperScrim: Float = DEFAULT_WALLPAPER_SCRIM
) {
    companion object {
        const val MIN_WALLPAPER_SCRIM = 0.35f
        const val DEFAULT_WALLPAPER_SCRIM = 0.58f
        const val MAX_WALLPAPER_SCRIM = 0.82f

        fun normalizeWallpaperScrim(value: Float): Float =
            value.coerceIn(MIN_WALLPAPER_SCRIM, MAX_WALLPAPER_SCRIM)

        fun normalizeWallpaperUri(value: String?): String? = value?.trim()?.takeIf(String::isNotEmpty)
    }
}
