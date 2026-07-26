package io.github.nanima1.twilight.data.appearance

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.github.nanima1.twilight.domain.appearance.AppearanceSettings
import io.github.nanima1.twilight.domain.appearance.ThemePreset
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

interface AppearanceRepository {
    val settings: Flow<AppearanceSettings>

    suspend fun setThemePreset(themePreset: ThemePreset)

    suspend fun setWallpaperUri(uri: String?)

    suspend fun setWallpaperScrim(scrim: Float)
}

private val Context.appearanceDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "appearance"
)

class DataStoreAppearanceRepository(context: Context) : AppearanceRepository {
    private val dataStore = context.applicationContext.appearanceDataStore

    override val settings: Flow<AppearanceSettings> = dataStore.data
        .catch { error ->
            if (error is IOException) {
                emit(emptyPreferences())
            } else {
                throw error
            }
        }
        .map { preferences -> preferences.toAppearanceSettings() }
        .distinctUntilChanged()

    override suspend fun setThemePreset(themePreset: ThemePreset) {
        dataStore.edit { preferences ->
            preferences[Keys.themePreset] = themePreset.id
        }
    }

    override suspend fun setWallpaperUri(uri: String?) {
        dataStore.edit { preferences ->
            val normalizedUri = AppearanceSettings.normalizeWallpaperUri(uri)
            if (normalizedUri == null) {
                preferences.remove(Keys.wallpaperUri)
            } else {
                preferences[Keys.wallpaperUri] = normalizedUri
            }
        }
    }

    override suspend fun setWallpaperScrim(scrim: Float) {
        dataStore.edit { preferences ->
            preferences[Keys.wallpaperScrim] = AppearanceSettings.normalizeWallpaperScrim(scrim)
        }
    }

    private fun Preferences.toAppearanceSettings(): AppearanceSettings = AppearanceSettings(
        themePreset = ThemePreset.fromId(this[Keys.themePreset]),
        wallpaperUri = AppearanceSettings.normalizeWallpaperUri(this[Keys.wallpaperUri]),
        wallpaperScrim = AppearanceSettings.normalizeWallpaperScrim(
            this[Keys.wallpaperScrim] ?: AppearanceSettings.DEFAULT_WALLPAPER_SCRIM
        )
    )

    private object Keys {
        val themePreset = stringPreferencesKey("theme_preset")
        val wallpaperUri = stringPreferencesKey("wallpaper_uri")
        val wallpaperScrim = floatPreferencesKey("wallpaper_scrim")
    }
}
