package io.github.nanima1.twilight.data.appearance

import kotlin.math.roundToInt

internal data class WallpaperDimensions(
    val width: Int,
    val height: Int
)

internal object WallpaperImportSizing {
    const val MAX_SOURCE_BYTES = 32L * 1024L * 1024L
    const val MAX_WALLPAPER_EDGE_PX = 2_560

    fun calculateInSampleSize(width: Int, height: Int): Int {
        require(width > 0 && height > 0) { "Wallpaper dimensions must be positive." }

        var sampleSize = 1
        while (width / sampleSize > MAX_WALLPAPER_EDGE_PX ||
            height / sampleSize > MAX_WALLPAPER_EDGE_PX
        ) {
            if (sampleSize > Int.MAX_VALUE / 2) return Int.MAX_VALUE
            sampleSize *= 2
        }
        return sampleSize
    }

    fun scaledDimensions(width: Int, height: Int): WallpaperDimensions {
        require(width > 0 && height > 0) { "Wallpaper dimensions must be positive." }

        val longestEdge = maxOf(width, height)
        if (longestEdge <= MAX_WALLPAPER_EDGE_PX) {
            return WallpaperDimensions(width, height)
        }

        val scale = MAX_WALLPAPER_EDGE_PX.toDouble() / longestEdge
        return WallpaperDimensions(
            width = (width * scale).roundToInt().coerceAtLeast(1),
            height = (height * scale).roundToInt().coerceAtLeast(1)
        )
    }
}
