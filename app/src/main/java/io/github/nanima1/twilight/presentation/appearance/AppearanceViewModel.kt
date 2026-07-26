package io.github.nanima1.twilight.presentation.appearance

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.nanima1.twilight.data.appearance.AppearanceRepository
import io.github.nanima1.twilight.data.appearance.DataStoreAppearanceRepository
import io.github.nanima1.twilight.domain.appearance.AppearanceSettings
import io.github.nanima1.twilight.domain.appearance.ThemePreset
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppearanceViewModel(
    private val repository: AppearanceRepository
) : ViewModel() {
    val state: StateFlow<AppearanceSettings> = repository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000L),
        initialValue = AppearanceSettings()
    )

    fun setThemePreset(themePreset: ThemePreset) {
        viewModelScope.launch {
            repository.setThemePreset(themePreset)
        }
    }

    fun setWallpaperUri(uri: String?) {
        viewModelScope.launch {
            repository.setWallpaperUri(uri)
        }
    }

    fun setWallpaperScrim(scrim: Float) {
        viewModelScope.launch {
            repository.setWallpaperScrim(scrim)
        }
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                AppearanceViewModel(DataStoreAppearanceRepository(context.applicationContext))
            }
        }
    }
}
