package io.github.nanima1.twilight.presentation.appearance

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import io.github.nanima1.twilight.domain.appearance.AppearanceSettings
import io.github.nanima1.twilight.domain.appearance.WallpaperPosition

@Composable
internal fun WallpaperImage(
    settings: AppearanceSettings,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val wallpaperUri = settings.wallpaperUri ?: return
    AsyncImage(
        model = wallpaperUri,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        alignment = settings.wallpaperPosition.toAlignment(),
        colorFilter = ColorFilter.tint(
            color = Color.Black.copy(alpha = settings.wallpaperScrim),
            blendMode = BlendMode.SrcOver
        ),
        modifier = modifier
    )
}

private fun WallpaperPosition.toAlignment(): Alignment = when (this) {
    WallpaperPosition.TOP -> Alignment.TopCenter
    WallpaperPosition.CENTER -> Alignment.Center
    WallpaperPosition.BOTTOM -> Alignment.BottomCenter
}
