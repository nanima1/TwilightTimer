package io.github.nanima1.twilight.data.appearance

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import io.github.nanima1.twilight.domain.appearance.AppearanceSettings
import io.github.nanima1.twilight.domain.appearance.ThemePreset
import io.github.nanima1.twilight.domain.appearance.WallpaperPosition
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class AppearanceRepositoryTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `wallpaper profiles remain isolated between themes`() = runTest {
        val repository = createRepository("profiles.preferences_pb")
        repository.setWallpaperUri(ThemePreset.TWILIGHT, "file:///twilight")
        repository.setWallpaperScrim(ThemePreset.TWILIGHT, 0.64f)
        repository.setThemePreset(ThemePreset.SAKURA_SIGNAL)

        val sakuraDefaults = repository.settings.first()
        assertEquals(ThemePreset.SAKURA_SIGNAL, sakuraDefaults.themePreset)
        assertNull(sakuraDefaults.wallpaperUri)
        assertEquals(
            AppearanceSettings.DEFAULT_WALLPAPER_SCRIM,
            sakuraDefaults.wallpaperScrim
        )

        repository.setWallpaperUri(ThemePreset.SAKURA_SIGNAL, "file:///sakura")
        repository.setWallpaperPanelOpacity(ThemePreset.SAKURA_SIGNAL, 0.76f)
        repository.setThemePreset(ThemePreset.TWILIGHT)

        val twilight = repository.settings.first()
        assertEquals("file:///twilight", twilight.wallpaperUri)
        assertEquals(0.64f, twilight.wallpaperScrim)
        assertEquals(
            AppearanceSettings.DEFAULT_WALLPAPER_PANEL_OPACITY,
            twilight.wallpaperPanelOpacity
        )

        repository.setThemePreset(ThemePreset.SAKURA_SIGNAL)
        val sakura = repository.settings.first()
        assertEquals("file:///sakura", sakura.wallpaperUri)
        assertEquals(0.76f, sakura.wallpaperPanelOpacity)
    }

    @Test
    fun `legacy wallpaper settings migrate to the previously active theme`() = runTest {
        val dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { File(temporaryFolder.root, "legacy.preferences_pb") }
        )
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("theme_preset")] = ThemePreset.SAKURA_SIGNAL.id
            preferences[stringPreferencesKey("wallpaper_uri")] = "file:///legacy"
            preferences[floatPreferencesKey("wallpaper_scrim")] = 0.7f
            preferences[stringPreferencesKey("wallpaper_position")] = WallpaperPosition.TOP.id
        }
        val repository = DataStoreAppearanceRepository(dataStore)

        val beforeMigration = repository.settings.first()
        assertEquals("file:///legacy", beforeMigration.wallpaperUri)

        repository.setThemePreset(ThemePreset.MINT_CIRCUIT)
        assertNull(repository.settings.first().wallpaperUri)

        repository.setThemePreset(ThemePreset.SAKURA_SIGNAL)
        val migrated = repository.settings.first()
        assertEquals("file:///legacy", migrated.wallpaperUri)
        assertEquals(0.7f, migrated.wallpaperScrim)
        assertEquals(WallpaperPosition.TOP, migrated.wallpaperPosition)
    }

    private fun kotlinx.coroutines.test.TestScope.createRepository(fileName: String): AppearanceRepository {
        val dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { File(temporaryFolder.root, fileName) }
        )
        return DataStoreAppearanceRepository(dataStore)
    }
}
