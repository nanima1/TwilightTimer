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
import io.github.nanima1.twilight.domain.scramble.ScrambleGenerator
import io.github.nanima1.twilight.domain.solve.SolveHistory
import io.github.nanima1.twilight.domain.solve.SolveRepository
import io.github.nanima1.twilight.domain.timer.TimerPhase
import io.github.nanima1.twilight.domain.timer.TimerSession
import io.github.nanima1.twilight.domain.timer.TimerSessionReducer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class TimerUiState(
    val session: TimerSession = TimerSession(),
    val scramble: String = "",
    val history: SolveHistory = SolveHistory()
)

class TimerViewModel(
    private val solveRepository: SolveRepository,
    private val scrambleGenerator: ScrambleGenerator = ScrambleGenerator(),
    private val elapsedRealtimeMillis: () -> Long = SystemClock::elapsedRealtime,
    private val currentTimeMillis: () -> Long = System::currentTimeMillis
) : ViewModel() {
    private val session = MutableStateFlow(TimerSession())
    private val scramble = MutableStateFlow(scrambleGenerator.generate())

    val state: StateFlow<TimerUiState> = combine(
        session,
        scramble,
        solveRepository.history
    ) { currentSession, currentScramble, history ->
        TimerUiState(
            session = currentSession,
            scramble = currentScramble,
            history = history
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = TimerUiState(scramble = scramble.value)
    )

    private var ticker: Job? = null

    fun onTimerPressed() {
        if (session.value.phase == TimerPhase.READY) {
            start()
        } else {
            stop()
        }
    }

    fun deleteSolve(id: Long) {
        viewModelScope.launch {
            solveRepository.deleteSolve(id)
        }
    }

    private fun start() {
        session.update { TimerSessionReducer.start(it, elapsedRealtimeMillis()) }
        ticker?.cancel()
        ticker = viewModelScope.launch {
            while (isActive) {
                session.update { TimerSessionReducer.tick(it, elapsedRealtimeMillis()) }
                delay(TICK_INTERVAL_MILLIS)
            }
        }
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
                completedAtEpochMillis = currentTimeMillis()
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
                    solveRepository = RoomSolveRepository(database.solveDao())
                )
            }
        }

        private const val TICK_INTERVAL_MILLIS = 16L
    }
}
