package io.github.nanima1.twilight.data.appearance

import org.junit.Assert.assertEquals
import org.junit.Test

class WallpaperImportSizingTest {
    @Test
    fun `keeps images at or below the wallpaper edge at native resolution`() {
        assertEquals(1, WallpaperImportSizing.calculateInSampleSize(2_560, 1_440))
    }

    @Test
    fun `uses power of two sampling for oversized source images`() {
        assertEquals(2, WallpaperImportSizing.calculateInSampleSize(5_120, 2_880))
        assertEquals(4, WallpaperImportSizing.calculateInSampleSize(7_680, 4_320))
    }

    @Test
    fun `scales oversized images to the wallpaper edge while preserving aspect ratio`() {
        assertEquals(
            WallpaperDimensions(width = 2_560, height = 1_920),
            WallpaperImportSizing.scaledDimensions(width = 4_000, height = 3_000)
        )
    }

    @Test
    fun `leaves dimensions already within the wallpaper edge unchanged`() {
        assertEquals(
            WallpaperDimensions(width = 1_080, height = 2_400),
            WallpaperImportSizing.scaledDimensions(width = 1_080, height = 2_400)
        )
    }
}
