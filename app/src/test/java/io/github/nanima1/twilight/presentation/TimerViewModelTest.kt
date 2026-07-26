package io.github.nanima1.twilight.presentation

import io.github.nanima1.twilight.domain.scramble.ScrambleGenerator
import io.github.nanima1.twilight.domain.solve.SolveHistory
import io.github.nanima1.twilight.domain.solve.SolvePenalty
import io.github.nanima1.twilight.domain.solve.SolveRecord
import io.github.nanima1.twilight.domain.solve.SolveRepository
import io.github.nanima1.twilight.domain.solve.SolveStats
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TimerViewModelTest {
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
    fun `stopping timer saves duration and completed scramble`() = runTest {
        val repository = FakeSolveRepository()
        var elapsedRealtime = 100L
        val viewModel = TimerViewModel(
            solveRepository = repository,
            scrambleGenerator = ScrambleGenerator(Random(17)),
            elapsedRealtimeMillis = { elapsedRealtime },
            currentTimeMillis = { 1_700_000_000_000L }
        )
        val completedScramble = viewModel.state.value.scramble

        viewModel.onTimerPressed()
        elapsedRealtime = 12_445L
        viewModel.onTimerPressed()
        advanceUntilIdle()

        val saved = repository.current.recentSolves.single()
        assertEquals(12_345L, saved.durationMillis)
        assertEquals(completedScramble, saved.scramble)
        assertEquals(1_700_000_000_000L, saved.completedAtEpochMillis)
        assertEquals(1L, viewModel.state.value.history.stats.solveCount)
        assertNotEquals(completedScramble, viewModel.state.value.scramble)
    }

    @Test
    fun `deleting a solve updates history statistics`() = runTest {
        val repository = FakeSolveRepository()
        repository.addSolve(8_200L, "R U", 1_000L)
        repository.addSolve(9_400L, "F D", 2_000L)
        val viewModel = TimerViewModel(
            solveRepository = repository,
            scrambleGenerator = ScrambleGenerator(Random(21))
        )
        advanceUntilIdle()

        viewModel.deleteSolve(repository.current.recentSolves.first().id)
        advanceUntilIdle()

        assertEquals(1L, viewModel.state.value.history.stats.solveCount)
        assertEquals(8_200L, viewModel.state.value.history.stats.lastSolveMillis)
        assertEquals(8_200L, viewModel.state.value.history.stats.bestSolveMillis)
    }

    @Test
    fun `changing a solve penalty updates adjusted statistics`() = runTest {
        val repository = FakeSolveRepository()
        repository.addSolve(5_000L, "R U", 1_000L)
        repository.addSolve(6_000L, "F D", 2_000L)
        val viewModel = TimerViewModel(
            solveRepository = repository,
            scrambleGenerator = ScrambleGenerator(Random(25))
        )

        viewModel.setSolvePenalty(id = 1L, penalty = SolvePenalty.DNF)
        viewModel.setSolvePenalty(id = 2L, penalty = SolvePenalty.PLUS_TWO)
        advanceUntilIdle()

        assertEquals(SolvePenalty.PLUS_TWO, viewModel.state.value.history.stats.lastSolvePenalty)
        assertEquals(8_000L, viewModel.state.value.history.stats.bestSolveMillis)
    }

    private class FakeSolveRepository : SolveRepository {
        private val mutableHistory = MutableStateFlow(SolveHistory())
        override val history: Flow<SolveHistory> = mutableHistory
        val current: SolveHistory get() = mutableHistory.value
        private var nextId = 1L

        override suspend fun addSolve(
            durationMillis: Long,
            scramble: String,
            completedAtEpochMillis: Long
        ) {
            val solve = SolveRecord(
                id = nextId++,
                durationMillis = durationMillis,
                scramble = scramble,
                completedAtEpochMillis = completedAtEpochMillis
            )
            publish(listOf(solve) + current.recentSolves)
        }

        override suspend fun deleteSolve(id: Long) {
            publish(current.recentSolves.filterNot { it.id == id })
        }

        override suspend fun setPenalty(id: Long, penalty: SolvePenalty) {
            publish(
                current.recentSolves.map { solve ->
                    if (solve.id == id) solve.copy(penalty = penalty) else solve
                }
            )
        }

        private fun publish(solves: List<SolveRecord>) {
            mutableHistory.value = SolveHistory(
                recentSolves = solves,
                stats = SolveStats(
                    solveCount = solves.size.toLong(),
                    lastSolveMillis = solves.firstOrNull()?.durationMillis,
                    lastSolvePenalty = solves.firstOrNull()?.penalty,
                    bestSolveMillis = solves.mapNotNull(SolveRecord::adjustedDurationMillis).minOrNull()
                )
            )
        }
    }
}
