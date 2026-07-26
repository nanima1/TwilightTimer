package io.github.nanima1.twilight.presentation.solution

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.nanima1.twilight.solver.CubeSolution
import io.github.nanima1.twilight.solver.CubeSolver
import io.github.nanima1.twilight.solver.InverseScrambleSolver
import io.github.nanima1.twilight.solver.Min2PhaseSolver
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface SolutionUiState {
    val scramble: String
    val immediateSolution: CubeSolution

    data class Immediate(
        override val scramble: String,
        override val immediateSolution: CubeSolution
    ) : SolutionUiState

    data class Optimizing(
        override val scramble: String,
        override val immediateSolution: CubeSolution
    ) : SolutionUiState

    data class Optimized(
        override val scramble: String,
        override val immediateSolution: CubeSolution,
        val optimizedSolution: CubeSolution
    ) : SolutionUiState

    data class OptimizationFailed(
        override val scramble: String,
        override val immediateSolution: CubeSolution,
        val errorMessage: String
    ) : SolutionUiState
}

fun interface SolutionOptimizer {
    suspend fun optimize(scramble: String): CubeSolution
}

class SolutionViewModel(
    initialScramble: String,
    private val immediateSolver: CubeSolver,
    private val optimizer: SolutionOptimizer
) : ViewModel() {
    private val initialSolution = immediateSolver.solve(initialScramble)
    private val mutableState = MutableStateFlow<SolutionUiState>(
        SolutionUiState.Immediate(initialScramble, initialSolution)
    )
    val state: StateFlow<SolutionUiState> = mutableState.asStateFlow()

    private var requestVersion = 0L
    private var optimizationJob: Job? = null

    init {
        scheduleOptimization(initialScramble, initialSolution)
    }

    fun solve(scramble: String) {
        if (scramble == mutableState.value.scramble) return

        optimizationJob?.cancel()
        val immediateSolution = immediateSolver.solve(scramble)
        scheduleOptimization(scramble, immediateSolution)
    }

    private fun scheduleOptimization(
        scramble: String,
        immediateSolution: CubeSolution
    ) {
        optimizationJob?.cancel()
        val currentRequest = ++requestVersion
        mutableState.value = SolutionUiState.Immediate(scramble, immediateSolution)
        optimizationJob = viewModelScope.launch {
            mutableState.value = SolutionUiState.Optimizing(scramble, immediateSolution)
            try {
                val optimizedSolution = optimizer.optimize(scramble)
                if (currentRequest == requestVersion) {
                    mutableState.value = SolutionUiState.Optimized(
                        scramble = scramble,
                        immediateSolution = immediateSolution,
                        optimizedSolution = optimizedSolution
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                if (currentRequest == requestVersion) {
                    mutableState.value = SolutionUiState.OptimizationFailed(
                        scramble = scramble,
                        immediateSolution = immediateSolution,
                        errorMessage = error.message ?: "Unknown solver error"
                    )
                }
            }
        }
    }

    companion object {
        fun factory(initialScramble: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val optimizedSolver = Min2PhaseSolver()
                SolutionViewModel(
                    initialScramble = initialScramble,
                    immediateSolver = InverseScrambleSolver(),
                    optimizer = SolutionOptimizer { scramble ->
                        withContext(Dispatchers.Default) {
                            optimizedSolver.solve(scramble)
                        }
                    }
                )
            }
        }
    }
}
