package io.github.nanima1.twilight.presentation

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.nanima1.twilight.domain.scramble.ScrambleGenerator
import io.github.nanima1.twilight.domain.timer.TimerPhase
import io.github.nanima1.twilight.domain.timer.TimerSession
import io.github.nanima1.twilight.domain.timer.TimerSessionReducer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class TimerUiState(
    val session: TimerSession = TimerSession(),
    val scramble: String = ""
)

class TimerViewModel(
    private val scrambleGenerator: ScrambleGenerator = ScrambleGenerator(),
    private val nowMillis: () -> Long = SystemClock::elapsedRealtime
) : ViewModel() {
    private val _state = MutableStateFlow(TimerUiState(scramble = scrambleGenerator.generate()))
    val state: StateFlow<TimerUiState> = _state.asStateFlow()

    private var ticker: Job? = null

    fun onTimerPressed() {
        if (_state.value.session.phase == TimerPhase.READY) {
            start()
        } else {
            stop()
        }
    }

    private fun start() {
        _state.update { it.copy(session = TimerSessionReducer.start(it.session, nowMillis())) }
        ticker?.cancel()
        ticker = viewModelScope.launch {
            while (isActive) {
                _state.update { it.copy(session = TimerSessionReducer.tick(it.session, nowMillis())) }
                delay(TICK_INTERVAL_MILLIS)
            }
        }
    }

    private fun stop() {
        ticker?.cancel()
        ticker = null
        _state.update {
            it.copy(
                session = TimerSessionReducer.stop(it.session, nowMillis()),
                scramble = scrambleGenerator.generate()
            )
        }
    }

    override fun onCleared() {
        ticker?.cancel()
    }

    private companion object {
        const val TICK_INTERVAL_MILLIS = 16L
    }
}
