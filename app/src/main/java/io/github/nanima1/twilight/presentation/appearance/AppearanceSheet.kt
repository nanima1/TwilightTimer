package io.github.nanima1.twilight.presentation.appearance

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.nanima1.twilight.domain.appearance.AppearanceSettings
import io.github.nanima1.twilight.domain.appearance.ThemePreset
import io.github.nanima1.twilight.domain.appearance.WallpaperPosition
import io.github.nanima1.twilight.presentation.theme.preview
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSheet(
    state: AppearanceUiState,
    onDismiss: () -> Unit,
    onThemeSelected: (ThemePreset) -> Unit,
    onWallpaperRequested: () -> Unit,
    onWallpaperRemoved: () -> Unit,
    onWallpaperScrimChanged: (Float) -> Unit,
    onWallpaperPanelOpacityChanged: (Float) -> Unit,
    onWallpaperPositionChanged: (WallpaperPosition) -> Unit,
    onWallpaperImportErrorShown: () -> Unit
) {
    val settings = state.settings
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var pendingScrim by remember(settings.wallpaperScrim) {
        mutableFloatStateOf(settings.wallpaperScrim)
    }
    var pendingPanelOpacity by remember(settings.wallpaperPanelOpacity) {
        mutableFloatStateOf(settings.wallpaperPanelOpacity)
    }
    val themeListState = rememberLazyListState(
        initialFirstVisibleItemIndex = ThemePreset.entries.indexOf(settings.themePreset)
            .coerceAtLeast(0)
    )
    LaunchedEffect(settings.themePreset) {
        themeListState.animateScrollToItem(ThemePreset.entries.indexOf(settings.themePreset))
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Appearance",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close appearance")
                }
            }

            Spacer(Modifier.height(18.dp))
            Text(
                text = "THEME",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                state = themeListState,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ThemePreset.entries, key = ThemePreset::id) { preset ->
                    ThemeOption(
                        preset = preset,
                        selected = settings.themePreset == preset,
                        onClick = { onThemeSelected(preset) },
                        modifier = Modifier
                            .width(132.dp)
                            .height(96.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(20.dp))

            Text(
                text = "WALLPAPER",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = {
                        onWallpaperImportErrorShown()
                        onWallpaperRequested()
                    },
                    enabled = !state.isWallpaperImporting,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    if (state.isWallpaperImporting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Rounded.Wallpaper, contentDescription = null)
                    }
                    Spacer(Modifier.size(8.dp))
                    Text(
                        when {
                            state.isWallpaperImporting -> "Importing image"
                            settings.wallpaperUri == null -> "Choose image"
                            else -> "Replace image"
                        }
                    )
                }
                if (settings.wallpaperUri != null) {
                    IconButton(onClick = onWallpaperRemoved) {
                        Icon(Icons.Rounded.DeleteOutline, contentDescription = "Remove wallpaper")
                    }
                }
            }

            state.wallpaperImportError?.let { error ->
                Spacer(Modifier.height(10.dp))
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (settings.wallpaperUri != null) {
                Spacer(Modifier.height(20.dp))
                Text(
                    text = "PREVIEW",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(10.dp))
                WallpaperPreview(
                    settings = settings.copy(
                        wallpaperScrim = pendingScrim,
                        wallpaperPanelOpacity = pendingPanelOpacity
                    )
                )
            }

            Spacer(Modifier.height(20.dp))
            Text(
                text = "IMAGE POSITION",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                WallpaperPosition.entries.forEachIndexed { index, position ->
                    SegmentedButton(
                        selected = settings.wallpaperPosition == position,
                        onClick = { onWallpaperPositionChanged(position) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = WallpaperPosition.entries.size
                        ),
                        enabled = settings.wallpaperUri != null,
                        label = { Text(position.displayName()) }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Background darkness",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (settings.wallpaperUri == null) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = "${(pendingScrim * 100).roundToInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (settings.wallpaperUri == null) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    fontWeight = FontWeight.Bold
                )
            }
            Slider(
                value = pendingScrim,
                onValueChange = { pendingScrim = it },
                onValueChangeFinished = { onWallpaperScrimChanged(pendingScrim) },
                valueRange = AppearanceSettings.MIN_WALLPAPER_SCRIM..AppearanceSettings.MAX_WALLPAPER_SCRIM,
                enabled = settings.wallpaperUri != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "Background darkness"
                        stateDescription = "${(pendingScrim * 100).roundToInt()} percent"
                    }
            )

            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Panel opacity",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (settings.wallpaperUri == null) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = "${(pendingPanelOpacity * 100).roundToInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (settings.wallpaperUri == null) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    fontWeight = FontWeight.Bold
                )
            }
            Slider(
                value = pendingPanelOpacity,
                onValueChange = { pendingPanelOpacity = it },
                onValueChangeFinished = {
                    onWallpaperPanelOpacityChanged(pendingPanelOpacity)
                },
                valueRange = AppearanceSettings.MIN_WALLPAPER_PANEL_OPACITY..
                    AppearanceSettings.MAX_WALLPAPER_PANEL_OPACITY,
                enabled = settings.wallpaperUri != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "Panel opacity"
                        stateDescription = "${(pendingPanelOpacity * 100).roundToInt()} percent"
                    }
            )
        }
    }
}

@Composable
private fun WallpaperPreview(settings: AppearanceSettings) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(168.dp)
                .aspectRatio(9f / 16f)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.background)
                .clearAndSetSemantics {
                    contentDescription = "Wallpaper preview"
                }
        ) {
            WallpaperImage(
                settings = settings,
                modifier = Modifier.fillMaxSize()
            )
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(10.dp),
                color = MaterialTheme.colorScheme.surface.copy(
                    alpha = settings.wallpaperPanelOpacity
                ),
                contentColor = MaterialTheme.colorScheme.onSurface,
                shape = RoundedCornerShape(4.dp),
                tonalElevation = 0.dp
            ) {
                Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                    Text(
                        text = "3 x 3 SCRAMBLE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "R U R' U' F2 D L2",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Text(
                text = "12.34",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(10.dp)
                    .height(28.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

private fun WallpaperPosition.displayName(): String = when (this) {
    WallpaperPosition.TOP -> "Top"
    WallpaperPosition.CENTER -> "Center"
    WallpaperPosition.BOTTOM -> "Bottom"
}

@Composable
private fun ThemeOption(
    preset: ThemePreset,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val preview = preset.preview()
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.52f))
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .clickable(role = Role.RadioButton, onClick = onClick)
            .semantics { this.selected = selected }
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            preview.swatches.forEach { color ->
                Box(
                    Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
        Spacer(Modifier.height(9.dp))
        Text(
            text = preview.name,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
