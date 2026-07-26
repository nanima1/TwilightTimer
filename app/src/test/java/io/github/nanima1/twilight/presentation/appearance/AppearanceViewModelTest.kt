package io.github.nanima1.twilight.presentation.appearance

import io.github.nanima1.twilight.data.appearance.AppearanceRepository
import io.github.nanima1.twilight.data.appearance.WallpaperStore
import io.github.nanima1.twilight.domain.appearance.AppearanceSettings
import io.github.nanima1.twilight.domain.appearance.ThemePreset
import io.github.nanima1.twilight.domain.appearance.WallpaperPosition
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppearanceViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `imported wallpaper is persisted and previous managed file is removed`() = runTest {
        val repository = FakeAppearanceRepository(
            AppearanceSettings(wallpaperUri = "file:///old")
        )
        val wallpaperStore = FakeWallpaperStore(importedUri = "file:///new")
        val viewModel = AppearanceViewModel(repository, wallpaperStore)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect()
        }

        viewModel.importWallpaper("content://picked/image")
        advanceUntilIdle()

        assertEquals("file:///new", repository.current.wallpaperUri)
        assertEquals(listOf("file:///old"), wallpaperStore.removedUris)
        assertFalse(viewModel.state.value.isWallpaperImporting)
    }

    @Test
    fun `failed import exposes a recoverable ui error`() = runTest {
        val repository = FakeAppearanceRepository(AppearanceSettings())
        val wallpaperStore = FakeWallpaperStore(error = IOException("broken image"))
        val viewModel = AppearanceViewModel(repository, wallpaperStore)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect()
        }

        viewModel.importWallpaper("content://picked/broken")
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.wallpaperImportError)
        assertFalse(viewModel.state.value.isWallpaperImporting)
    }

    @Test
    fun `wallpaper position is persisted`() = runTest {
        val repository = FakeAppearanceRepository(
            AppearanceSettings(wallpaperUri = "file:///wallpaper")
        )
        val viewModel = AppearanceViewModel(repository, FakeWallpaperStore())
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect()
        }

        viewModel.setWallpaperPosition(WallpaperPosition.TOP)
        advanceUntilIdle()

        assertEquals(WallpaperPosition.TOP, repository.current.wallpaperPosition)
    }

    private class FakeAppearanceRepository(initial: AppearanceSettings) : AppearanceRepository {
        private val mutableSettings = MutableStateFlow(initial)
        override val settings: Flow<AppearanceSettings> = mutableSettings
        val current: AppearanceSettings get() = mutableSettings.value

        override suspend fun setThemePreset(themePreset: ThemePreset) {
            mutableSettings.value = current.copy(themePreset = themePreset)
        }

        override suspend fun setWallpaperUri(uri: String?) {
            mutableSettings.value = current.copy(wallpaperUri = uri)
        }

        override suspend fun setWallpaperScrim(scrim: Float) {
            mutableSettings.value = current.copy(wallpaperScrim = scrim)
        }

        override suspend fun setWallpaperPosition(position: WallpaperPosition) {
            mutableSettings.value = current.copy(wallpaperPosition = position)
        }
    }

    private class FakeWallpaperStore(
        private val importedUri: String = "file:///wallpaper",
        private val error: Exception? = null
    ) : WallpaperStore {
        val removedUris = mutableListOf<String?>()

        override suspend fun import(sourceUri: String): String {
            error?.let { throw it }
            return importedUri
        }

        override suspend fun removeManaged(uriValue: String?) {
            removedUris += uriValue
        }
    }
}
