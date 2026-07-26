package io.github.nanima1.twilight.data.timer

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import io.github.nanima1.twilight.domain.timer.TimerSettings
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

interface TimerSettingsRepository {
    val settings: Flow<TimerSettings>

    suspend fun setInspectionEnabled(enabled: Boolean)

    suspend fun setInspectionHapticsEnabled(enabled: Boolean)
}

private val Context.timerSettingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "timer_settings"
)

class DataStoreTimerSettingsRepository internal constructor(
    private val dataStore: DataStore<Preferences>
) : TimerSettingsRepository {
    constructor(context: Context) : this(context.applicationContext.timerSettingsDataStore)

    override val settings: Flow<TimerSettings> = dataStore.data
        .catch { error ->
            if (error is IOException) {
                emit(emptyPreferences())
            } else {
                throw error
            }
        }
        .map { preferences ->
            TimerSettings(
                inspectionEnabled = preferences[Keys.inspectionEnabled] ?: true,
                inspectionHapticsEnabled = preferences[Keys.inspectionHapticsEnabled] ?: true
            )
        }
        .distinctUntilChanged()

    override suspend fun setInspectionEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.inspectionEnabled] = enabled
        }
    }

    override suspend fun setInspectionHapticsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.inspectionHapticsEnabled] = enabled
        }
    }

    private object Keys {
        val inspectionEnabled = booleanPreferencesKey("inspection_enabled")
        val inspectionHapticsEnabled = booleanPreferencesKey("inspection_haptics_enabled")
    }
}
