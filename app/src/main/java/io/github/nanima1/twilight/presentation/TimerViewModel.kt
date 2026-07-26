package io.github.nanima1.twilight.presentation

import android.content.Context
import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.nanima1.twilight.data.solve.RoomSolveRepository
import io.github.nanima1.twilight.data.solve.TwilightDatabase
import io.github.nanima1.twilight.data.timer.DataStoreTimerSettingsRepository
import io.github.nanima1.twilight.data.timer.TimerSettingsRepository
import io.github.nanima1.twilight.domain.scramble.ScrambleGenerator
import io.github.nanima1.twilight.domain.solve.SolveHistory
import io.github.nanima1.twilight.domain.solve.SolvePenalty
import io.github.nanima1.twilight.domain.solve.SolveRepository
import io.github.nanima1.twilight.domain.timer.TimerPhase
import io.github.nanima1.twilight.domain.timer.InspectionCue
import io.github.nanima1.twilight.domain.timer.TimerSession
import io.github.nanima1.twilight.domain.timer.TimerSessionReducer
import io.github.nanima1.twilight.domain.timer.TimerSettings
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class TimerUiState(
    val session: TimerSession = TimerSession(),
    val scramble: String = "",
    val history: SolveHistory = SolveHistory(),
    val timerSettings: TimerSettings = TimerSettings()
)

class TimerViewModel(
    private val solveRepository: SolveRepository,
    private val timerSettingsRepository: TimerSettingsRepository,
    private val scrambleGenerator: ScrambleGenerator = ScrambleGenerator(),
    private val elapsedRealtimeMillis: () -> Long = SystemClock::elapsedRealtime,
    private val currentTimeMillis: () -> Long = System::currentTimeMillis
) : ViewModel() {
    private val session = MutableStateFlow(TimerSession())
    private val scramble = MutableStateFlow(scrambleGenerator.generate())
    private val mutableInspectionCues = MutableSharedFlow<InspectionCue>(extraBufferCapacity = 2)
    val inspectionCues: SharedFlow<InspectionCue> = mutableInspectionCues.asSharedFlow()
    private val timerSettings = timerSettingsRepository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = TimerSettings()
    )

    val state: StateFlow<TimerUiState> = combine(
        session,
        scramble,
        solveRepository.history,
        timerSettings
    ) { currentSession, currentScramble, history, currentTimerSettings ->
        TimerUiState(
            session = currentSession,
            scramble = currentScramble,
            history = history,
            timerSettings = currentTimerSettings
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = TimerUiState(scramble = scramble.value)
    )

    private var ticker: Job? = null

    fun onTimerPressed() {
        when (session.value.phase) {
            TimerPhase.READY -> if (timerSettings.value.inspectionEnabled) {
                beginInspection()
            } else {
                startDirect()
            }
            TimerPhase.INSPECTING -> start()
            TimerPhase.RUNNING -> stop()
        }
    }

    fun deleteSolve(id: Long) {
        viewModelScope.launch {
            solveRepository.deleteSolve(id)
        }
    }

    fun setSolvePenalty(id: Long, penalty: SolvePenalty) {
        viewModelScope.launch {
            solveRepository.setPenalty(id, penalty)
        }
    }

    fun setInspectionEnabled(enabled: Boolean) {
        viewModelScope.launch {
            timerSettingsRepository.setInspectionEnabled(enabled)
        }
    }

    fun setInspectionHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            timerSettingsRepository.setInspectionHapticsEnabled(enabled)
        }
    }

    private fun beginInspection() {
        session.update { TimerSessionReducer.beginInspection(it, elapsedRealtimeMillis()) }
        restartTicker()
    }

    private fun startDirect() {
        session.update { TimerSessionReducer.startDirect(it, elapsedRealtimeMillis()) }
        restartTicker()
    }

    private fun restartTicker() {
        ticker?.cancel()
        ticker = viewModelScope.launch {
            while (isActive) {
                val previousSession = session.value
                val updatedSession = TimerSessionReducer.tick(
                    previousSession,
                    elapsedRealtimeMillis()
                )
                session.value = updatedSession
                emitInspectionCues(previousSession, updatedSession)
                delay(TICK_INTERVAL_MILLIS)
            }
        }
    }

    private fun emitInspectionCues(previous: TimerSession, current: TimerSession) {
        if (!timerSettings.value.inspectionHapticsEnabled) return
        if (previous.phase != TimerPhase.INSPECTING || current.phase != TimerPhase.INSPECTING) return

        InspectionCue.entries.forEach { cue ->
            if (
                previous.inspectionElapsedMillis < cue.thresholdMillis &&
                current.inspectionElapsedMillis >= cue.thresholdMillis
            ) {
                mutableInspectionCues.tryEmit(cue)
            }
        }
    }

    private fun start() {
        session.update { TimerSessionReducer.start(it, elapsedRealtimeMillis()) }
    }

    private fun stop() {
        ticker?.cancel()
        ticker = null

        val completed = TimerSessionReducer.stop(session.value, elapsedRealtimeMillis())
        val completedScramble = scramble.value
        session.value = completed
        scramble.value = scrambleGenerator.generate()

        viewModelScope.launch {
            solveRepository.addSolve(
                durationMillis = completed.elapsedMillis,
                scramble = completedScramble,
                completedAtEpochMillis = currentTimeMillis(),
                penalty = completed.penalty
            )
        }
    }

    override fun onCleared() {
        ticker?.cancel()
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val database = TwilightDatabase.getInstance(context.applicationContext)
                TimerViewModel(
                    solveRepository = RoomSolveRepository(database.solveDao()),
                    timerSettingsRepository = DataStoreTimerSettingsRepository(
                        context.applicationContext
                    )
                )
            }
        }

        private const val TICK_INTERVAL_MILLIS = 16L
    }
}
