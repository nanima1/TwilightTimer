package io.github.nanima1.twilight.data.appearance

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.github.nanima1.twilight.domain.appearance.AppearanceSettings
import io.github.nanima1.twilight.domain.appearance.ThemePreset
import io.github.nanima1.twilight.domain.appearance.WallpaperPosition
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

interface AppearanceRepository {
    val settings: Flow<AppearanceSettings>

    suspend fun setThemePreset(themePreset: ThemePreset)

    suspend fun setWallpaperUri(themePreset: ThemePreset, uri: String?)

    suspend fun setWallpaperScrim(themePreset: ThemePreset, scrim: Float)

    suspend fun setWallpaperPosition(themePreset: ThemePreset, position: WallpaperPosition)

    suspend fun setWallpaperPanelOpacity(themePreset: ThemePreset, opacity: Float)
}

private val Context.appearanceDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "appearance"
)

class DataStoreAppearanceRepository internal constructor(
    private val dataStore: DataStore<Preferences>
) : AppearanceRepository {
    constructor(context: Context) : this(context.applicationContext.appearanceDataStore)

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
            preferences.migrateLegacyWallpaperProfile()
            preferences[Keys.themePreset] = themePreset.id
        }
    }

    override suspend fun setWallpaperUri(themePreset: ThemePreset, uri: String?) {
        dataStore.edit { preferences ->
            preferences.migrateLegacyWallpaperProfile()
            val normalizedUri = AppearanceSettings.normalizeWallpaperUri(uri)
            if (normalizedUri == null) {
                preferences.remove(Keys.wallpaperUri(themePreset))
            } else {
                preferences[Keys.wallpaperUri(themePreset)] = normalizedUri
            }
        }
    }

    override suspend fun setWallpaperScrim(themePreset: ThemePreset, scrim: Float) {
        dataStore.edit { preferences ->
            preferences.migrateLegacyWallpaperProfile()
            preferences[Keys.wallpaperScrim(themePreset)] =
                AppearanceSettings.normalizeWallpaperScrim(scrim)
        }
    }

    override suspend fun setWallpaperPosition(
        themePreset: ThemePreset,
        position: WallpaperPosition
    ) {
        dataStore.edit { preferences ->
            preferences.migrateLegacyWallpaperProfile()
            preferences[Keys.wallpaperPosition(themePreset)] = position.id
        }
    }

    override suspend fun setWallpaperPanelOpacity(themePreset: ThemePreset, opacity: Float) {
        dataStore.edit { preferences ->
            preferences.migrateLegacyWallpaperProfile()
            preferences[Keys.wallpaperPanelOpacity(themePreset)] =
                AppearanceSettings.normalizeWallpaperPanelOpacity(opacity)
        }
    }

    private fun Preferences.toAppearanceSettings(): AppearanceSettings {
        val themePreset = ThemePreset.fromId(this[Keys.themePreset])
        return AppearanceSettings(
            themePreset = themePreset,
            wallpaperUri = AppearanceSettings.normalizeWallpaperUri(
                this[Keys.wallpaperUri(themePreset)] ?: this[Keys.legacyWallpaperUri]
            ),
            wallpaperScrim = AppearanceSettings.normalizeWallpaperScrim(
                this[Keys.wallpaperScrim(themePreset)]
                    ?: this[Keys.legacyWallpaperScrim]
                    ?: AppearanceSettings.DEFAULT_WALLPAPER_SCRIM
            ),
            wallpaperPosition = WallpaperPosition.fromId(
                this[Keys.wallpaperPosition(themePreset)] ?: this[Keys.legacyWallpaperPosition]
            ),
            wallpaperPanelOpacity = AppearanceSettings.normalizeWallpaperPanelOpacity(
                this[Keys.wallpaperPanelOpacity(themePreset)]
                    ?: AppearanceSettings.DEFAULT_WALLPAPER_PANEL_OPACITY
            )
        )
    }

    private fun MutablePreferences.migrateLegacyWallpaperProfile() {
        val legacyUri = this[Keys.legacyWallpaperUri]
        val legacyScrim = this[Keys.legacyWallpaperScrim]
        val legacyPosition = this[Keys.legacyWallpaperPosition]
        if (legacyUri == null && legacyScrim == null && legacyPosition == null) return

        val currentTheme = ThemePreset.fromId(this[Keys.themePreset])
        legacyUri?.let { uri ->
            if (this[Keys.wallpaperUri(currentTheme)] == null) {
                this[Keys.wallpaperUri(currentTheme)] = uri
            }
        }
        legacyScrim?.let { scrim ->
            if (this[Keys.wallpaperScrim(currentTheme)] == null) {
                this[Keys.wallpaperScrim(currentTheme)] = scrim
            }
        }
        legacyPosition?.let { position ->
            if (this[Keys.wallpaperPosition(currentTheme)] == null) {
                this[Keys.wallpaperPosition(currentTheme)] = position
            }
        }
        remove(Keys.legacyWallpaperUri)
        remove(Keys.legacyWallpaperScrim)
        remove(Keys.legacyWallpaperPosition)
    }

    private object Keys {
        val themePreset = stringPreferencesKey("theme_preset")
        val legacyWallpaperUri = stringPreferencesKey("wallpaper_uri")
        val legacyWallpaperScrim = floatPreferencesKey("wallpaper_scrim")
        val legacyWallpaperPosition = stringPreferencesKey("wallpaper_position")

        fun wallpaperUri(themePreset: ThemePreset) =
            stringPreferencesKey("wallpaper_uri_${themePreset.id}")

        fun wallpaperScrim(themePreset: ThemePreset) =
            floatPreferencesKey("wallpaper_scrim_${themePreset.id}")

        fun wallpaperPosition(themePreset: ThemePreset) =
            stringPreferencesKey("wallpaper_position_${themePreset.id}")

        fun wallpaperPanelOpacity(themePreset: ThemePreset) =
            floatPreferencesKey("wallpaper_panel_opacity_${themePreset.id}")
    }
}
