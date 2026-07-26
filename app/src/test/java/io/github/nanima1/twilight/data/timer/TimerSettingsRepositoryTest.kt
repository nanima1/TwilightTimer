package io.github.nanima1.twilight.data.timer

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class TimerSettingsRepositoryTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `new installs enable inspection and haptic cues`() = runTest {
        val dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { File(temporaryFolder.root, "timer-settings.preferences_pb") }
        )
        val repository = DataStoreTimerSettingsRepository(dataStore)

        val settings = repository.settings.first()

        assertTrue(settings.inspectionEnabled)
        assertTrue(settings.inspectionHapticsEnabled)
    }

    @Test
    fun `inspection preferences persist together`() = runTest {
        val dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { File(temporaryFolder.root, "persisted-settings.preferences_pb") }
        )
        val repository = DataStoreTimerSettingsRepository(dataStore)

        repository.setInspectionEnabled(false)
        repository.setInspectionHapticsEnabled(false)

        val settings = repository.settings.first()
        assertFalse(settings.inspectionEnabled)
        assertFalse(settings.inspectionHapticsEnabled)
    }
}
