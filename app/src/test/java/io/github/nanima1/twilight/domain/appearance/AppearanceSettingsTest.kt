package io.github.nanima1.twilight.domain.appearance

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppearanceSettingsTest {
    @Test
    fun `unknown theme id falls back to twilight`() {
        assertEquals(ThemePreset.TWILIGHT, ThemePreset.fromId("missing"))
    }

    @Test
    fun `unknown wallpaper position falls back to center`() {
        assertEquals(WallpaperPosition.CENTER, WallpaperPosition.fromId("missing"))
    }

    @Test
    fun `wallpaper scrim is clamped to readable bounds`() {
        assertEquals(
            AppearanceSettings.MIN_WALLPAPER_SCRIM,
            AppearanceSettings.normalizeWallpaperScrim(0.1f)
        )
        assertEquals(
            AppearanceSettings.MAX_WALLPAPER_SCRIM,
            AppearanceSettings.normalizeWallpaperScrim(1f)
        )
    }

    @Test
    fun `blank wallpaper uri is removed`() {
        assertNull(AppearanceSettings.normalizeWallpaperUri("   "))
        assertEquals("content://wallpaper/1", AppearanceSettings.normalizeWallpaperUri(" content://wallpaper/1 "))
    }
}
