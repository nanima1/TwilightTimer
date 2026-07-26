package io.github.nanima1.twilight.presentation.appearance

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.nanima1.twilight.data.appearance.AppearanceRepository
import io.github.nanima1.twilight.data.appearance.DataStoreAppearanceRepository
import io.github.nanima1.twilight.data.appearance.FileWallpaperStore
import io.github.nanima1.twilight.data.appearance.WallpaperStore
import io.github.nanima1.twilight.domain.appearance.AppearanceSettings
import io.github.nanima1.twilight.domain.appearance.ThemePreset
import io.github.nanima1.twilight.domain.appearance.WallpaperPosition
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppearanceUiState(
    val settings: AppearanceSettings = AppearanceSettings(),
    val isWallpaperImporting: Boolean = false,
    val wallpaperImportError: String? = null
)

class AppearanceViewModel(
    private val repository: AppearanceRepository,
    private val wallpaperStore: WallpaperStore
) : ViewModel() {
    private val operationState = MutableStateFlow(WallpaperOperationState())

    val state: StateFlow<AppearanceUiState> = combine(
        repository.settings,
        operationState
    ) { settings, operation ->
        AppearanceUiState(
            settings = settings,
            isWallpaperImporting = operation.isImporting,
            wallpaperImportError = operation.error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000L),
        initialValue = AppearanceUiState()
    )

    fun setThemePreset(themePreset: ThemePreset) {
        viewModelScope.launch {
            repository.setThemePreset(themePreset)
        }
    }

    fun importWallpaper(sourceUri: String) {
        viewModelScope.launch {
            operationState.value = WallpaperOperationState(isImporting = true)
            val targetSettings = state.value.settings
            val targetTheme = targetSettings.themePreset
            val previousUri = targetSettings.wallpaperUri
            try {
                val importedUri = wallpaperStore.import(sourceUri, targetTheme)
                repository.setWallpaperUri(targetTheme, importedUri)
                if (previousUri != importedUri) {
                    wallpaperStore.removeManaged(previousUri)
                }
                operationState.value = WallpaperOperationState()
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                operationState.value = WallpaperOperationState(
                    error = "Couldn't import this image. Choose a PNG, JPG, or WebP under 32 MB."
                )
            }
        }
    }

    fun removeWallpaper() {
        viewModelScope.launch {
            val targetSettings = state.value.settings
            val wallpaperUri = targetSettings.wallpaperUri
            repository.setWallpaperUri(targetSettings.themePreset, null)
            wallpaperStore.removeManaged(wallpaperUri)
            operationState.value = WallpaperOperationState()
        }
    }

    fun setWallpaperScrim(scrim: Float) {
        viewModelScope.launch {
            repository.setWallpaperScrim(state.value.settings.themePreset, scrim)
        }
    }

    fun setWallpaperPosition(position: WallpaperPosition) {
        viewModelScope.launch {
            repository.setWallpaperPosition(state.value.settings.themePreset, position)
        }
    }

    fun setWallpaperPanelOpacity(opacity: Float) {
        viewModelScope.launch {
            repository.setWallpaperPanelOpacity(state.value.settings.themePreset, opacity)
        }
    }

    fun clearWallpaperImportError() {
        operationState.update { it.copy(error = null) }
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                AppearanceViewModel(
                    repository = DataStoreAppearanceRepository(context.applicationContext),
                    wallpaperStore = FileWallpaperStore(context.applicationContext)
                )
            }
        }
    }

    private data class WallpaperOperationState(
        val isImporting: Boolean = false,
        val error: String? = null
    )
}
