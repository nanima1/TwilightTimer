package io.github.nanima1.twilight.presentation.solution

import io.github.nanima1.twilight.solver.CubeSolution
import io.github.nanima1.twilight.solver.CubeSolver
import io.github.nanima1.twilight.solver.SolutionMethod
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SolutionViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `immediate solution remains visible while optimization runs`() = runTest {
        val releaseOptimization = CompletableDeferred<Unit>()
        val viewModel = SolutionViewModel(
            initialScramble = FIRST_SCRAMBLE,
            immediateSolver = immediateSolver,
            optimizer = SolutionOptimizer {
                releaseOptimization.await()
                optimizedSolution(it)
            }
        )

        val immediate = viewModel.state.value
        assertTrue(immediate is SolutionUiState.Immediate)
        assertEquals(FIRST_SCRAMBLE, immediate.scramble)

        runCurrent()
        val optimizing = viewModel.state.value
        assertTrue(optimizing is SolutionUiState.Optimizing)
        assertEquals(immediateSolution(FIRST_SCRAMBLE), optimizing.immediateSolution)

        releaseOptimization.complete(Unit)
        advanceUntilIdle()

        val optimized = viewModel.state.value as SolutionUiState.Optimized
        assertEquals(FIRST_SCRAMBLE, optimized.scramble)
        assertEquals(optimizedSolution(FIRST_SCRAMBLE), optimized.optimizedSolution)
    }

    @Test
    fun `new scramble cancels the previous optimization`() = runTest {
        val firstStarted = CompletableDeferred<Unit>()
        var firstCancelled = false
        val viewModel = SolutionViewModel(
            initialScramble = FIRST_SCRAMBLE,
            immediateSolver = immediateSolver,
            optimizer = SolutionOptimizer { scramble ->
                if (scramble == FIRST_SCRAMBLE) {
                    firstStarted.complete(Unit)
                    try {
                        awaitCancellation()
                    } finally {
                        firstCancelled = true
                    }
                }
                optimizedSolution(scramble)
            }
        )
        runCurrent()
        assertTrue(firstStarted.isCompleted)

        viewModel.solve(SECOND_SCRAMBLE)
        runCurrent()

        assertTrue(firstCancelled)
        val optimized = viewModel.state.value as SolutionUiState.Optimized
        assertEquals(SECOND_SCRAMBLE, optimized.scramble)
    }

    @Test
    fun `late non cooperative result cannot replace the current solution`() = runTest {
        val releaseFirst = CompletableDeferred<Unit>()
        val viewModel = SolutionViewModel(
            initialScramble = FIRST_SCRAMBLE,
            immediateSolver = immediateSolver,
            optimizer = SolutionOptimizer { scramble ->
                if (scramble == FIRST_SCRAMBLE) {
                    withContext(NonCancellable) {
                        releaseFirst.await()
                    }
                }
                optimizedSolution(scramble)
            }
        )
        runCurrent()

        viewModel.solve(SECOND_SCRAMBLE)
        runCurrent()
        val current = viewModel.state.value
        assertTrue(current is SolutionUiState.Optimized)
        assertEquals(SECOND_SCRAMBLE, current.scramble)

        releaseFirst.complete(Unit)
        advanceUntilIdle()

        assertSame(current, viewModel.state.value)
    }

    @Test
    fun `optimization failure preserves the immediate fallback`() = runTest {
        val viewModel = SolutionViewModel(
            initialScramble = FIRST_SCRAMBLE,
            immediateSolver = immediateSolver,
            optimizer = SolutionOptimizer { error("Search limit reached") }
        )

        advanceUntilIdle()

        val failed = viewModel.state.value as SolutionUiState.OptimizationFailed
        assertEquals(FIRST_SCRAMBLE, failed.scramble)
        assertEquals(immediateSolution(FIRST_SCRAMBLE), failed.immediateSolution)
        assertEquals("Search limit reached", failed.errorMessage)
    }

    private companion object {
        const val FIRST_SCRAMBLE = "R U F"
        const val SECOND_SCRAMBLE = "L D B"

        val immediateSolver = CubeSolver(::immediateSolution)

        fun immediateSolution(scramble: String) = CubeSolution(
            algorithm = "inverse $scramble",
            moveCount = 3,
            method = SolutionMethod.INVERSE_SCRAMBLE
        )

        fun optimizedSolution(scramble: String) = CubeSolution(
            algorithm = "optimized $scramble",
            moveCount = 2,
            method = SolutionMethod.TWO_PHASE
        )
    }
}
