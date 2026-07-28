package io.github.nanima1.twilight.presentation

import io.github.nanima1.twilight.data.timer.TimerSettingsRepository
import io.github.nanima1.twilight.domain.scramble.ScrambleGenerator
import io.github.nanima1.twilight.domain.solve.SolveHistory
import io.github.nanima1.twilight.domain.solve.SolveHistoryFilter
import io.github.nanima1.twilight.domain.solve.SolveHistoryQuery
import io.github.nanima1.twilight.domain.solve.SolvePenalty
import io.github.nanima1.twilight.domain.solve.SolveRecord
import io.github.nanima1.twilight.domain.solve.SolveRepository
import io.github.nanima1.twilight.domain.solve.SolveStats
import io.github.nanima1.twilight.domain.solve.calculateWcaAverage
import io.github.nanima1.twilight.domain.timer.TimerPhase
import io.github.nanima1.twilight.domain.timer.InspectionCue
import io.github.nanima1.twilight.domain.timer.TimerInputState
import io.github.nanima1.twilight.domain.timer.TimerSettings
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
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
            timerSettingsRepository = FakeTimerSettingsRepository(),
            scrambleGenerator = ScrambleGenerator(Random(17)),
            elapsedRealtimeMillis = { elapsedRealtime },
            currentTimeMillis = { 1_700_000_000_000L }
        )
        val completedScramble = viewModel.state.value.scramble

        viewModel.onTimerPressed()
        viewModel.onTimerPressed()
        elapsedRealtime = 12_445L
        viewModel.onTimerPressed()
        advanceUntilIdle()

        val saved = repository.current.recentSolves.single()
        assertEquals(12_345L, saved.durationMillis)
        assertEquals(completedScramble, saved.scramble)
        assertEquals(1_700_000_000_000L, saved.completedAtEpochMillis)
        assertEquals(SolvePenalty.NONE, saved.penalty)
        assertEquals(1L, viewModel.state.value.history.stats.solveCount)
        assertNotEquals(completedScramble, viewModel.state.value.scramble)
    }

    @Test
    fun `new scramble request changes only the ready scramble`() = runTest {
        val repository = FakeSolveRepository()
        val viewModel = TimerViewModel(
            solveRepository = repository,
            timerSettingsRepository = FakeTimerSettingsRepository(),
            scrambleGenerator = ScrambleGenerator(Random(32))
        )
        val previousScramble = viewModel.state.value.scramble

        viewModel.requestNewScramble()

        assertNotEquals(previousScramble, viewModel.state.value.scramble)
        assertEquals(0L, repository.current.stats.solveCount)
        assertEquals(TimerPhase.READY, viewModel.state.value.session.phase)
    }

    @Test
    fun `new scramble request is ignored while inspection or timer is active`() = runTest {
        val viewModel = TimerViewModel(
            solveRepository = FakeSolveRepository(),
            timerSettingsRepository = FakeTimerSettingsRepository(),
            scrambleGenerator = ScrambleGenerator(Random(33)),
            elapsedRealtimeMillis = { 100L }
        )
        val activeScramble = viewModel.state.value.scramble

        viewModel.onTimerPressStarted()
        viewModel.requestNewScramble()
        assertEquals(activeScramble, viewModel.state.value.scramble)
        viewModel.onTimerPressCancelled()

        viewModel.onTimerPressed()
        viewModel.requestNewScramble()
        assertEquals(activeScramble, viewModel.state.value.scramble)

        viewModel.onTimerPressed()
        viewModel.requestNewScramble()
        assertEquals(activeScramble, viewModel.state.value.scramble)

        viewModel.onTimerPressed()
        advanceUntilIdle()
    }

    @Test
    fun `inspection penalty is saved with the completed solve`() = runTest {
        val repository = FakeSolveRepository()
        var elapsedRealtime = 100L
        val viewModel = TimerViewModel(
            solveRepository = repository,
            timerSettingsRepository = FakeTimerSettingsRepository(),
            scrambleGenerator = ScrambleGenerator(Random(19)),
            elapsedRealtimeMillis = { elapsedRealtime },
            currentTimeMillis = { 1_700_000_000_000L }
        )

        viewModel.onTimerPressed()
        elapsedRealtime = 15_101L
        viewModel.onTimerPressed()
        elapsedRealtime = 18_101L
        viewModel.onTimerPressed()
        advanceUntilIdle()

        val saved = repository.current.recentSolves.single()
        assertEquals(3_000L, saved.durationMillis)
        assertEquals(SolvePenalty.PLUS_TWO, saved.penalty)
        assertEquals(5_000L, repository.current.stats.bestSolveMillis)
    }

    @Test
    fun `deleting a solve updates history statistics`() = runTest {
        val repository = FakeSolveRepository()
        repository.addSolve(8_200L, "R U", 1_000L)
        repository.addSolve(9_400L, "F D", 2_000L)
        val viewModel = TimerViewModel(
            solveRepository = repository,
            timerSettingsRepository = FakeTimerSettingsRepository(),
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
            timerSettingsRepository = FakeTimerSettingsRepository(),
            scrambleGenerator = ScrambleGenerator(Random(25))
        )

        viewModel.setSolvePenalty(id = 1L, penalty = SolvePenalty.DNF)
        viewModel.setSolvePenalty(id = 2L, penalty = SolvePenalty.PLUS_TWO)
        advanceUntilIdle()

        assertEquals(SolvePenalty.PLUS_TWO, viewModel.state.value.history.stats.lastSolvePenalty)
        assertEquals(8_000L, viewModel.state.value.history.stats.bestSolveMillis)
    }

    @Test
    fun `changing a solve note updates history and blank text clears it`() = runTest {
        val repository = FakeSolveRepository()
        repository.addSolve(5_000L, "R U", 1_000L)
        val viewModel = TimerViewModel(
            solveRepository = repository,
            timerSettingsRepository = FakeTimerSettingsRepository(),
            scrambleGenerator = ScrambleGenerator(Random(26))
        )

        viewModel.setSolveNote(id = 1L, note = "  Smooth execution  ")
        advanceUntilIdle()
        assertEquals("Smooth execution", viewModel.state.value.history.recentSolves.single().note)

        viewModel.setSolveNote(id = 1L, note = "  ")
        advanceUntilIdle()
        assertNull(viewModel.state.value.history.recentSolves.single().note)
    }

    @Test
    fun `history filter updates solves and statistics together`() = runTest {
        val now = 1_722_060_000_000L
        val repository = FakeSolveRepository()
        repository.addSolve(8_000L, "previous", now - 86_400_000L)
        repository.addSolve(6_000L, "today", now - 3_600_000L)
        val viewModel = TimerViewModel(
            solveRepository = repository,
            timerSettingsRepository = FakeTimerSettingsRepository(),
            scrambleGenerator = ScrambleGenerator(Random(30)),
            currentTimeMillis = { now }
        )

        viewModel.setHistoryFilter(SolveHistoryFilter.TODAY)
        advanceUntilIdle()

        assertEquals(SolveHistoryFilter.TODAY, viewModel.state.value.historyFilter)
        assertEquals("today", viewModel.state.value.history.recentSolves.single().scramble)
        assertEquals(1L, viewModel.state.value.history.stats.solveCount)
        assertEquals(6_000L, viewModel.state.value.history.stats.bestSolveMillis)
    }

    @Test
    fun `disabled inspection starts the timer immediately`() = runTest {
        val settingsRepository = FakeTimerSettingsRepository(
            TimerSettings(inspectionEnabled = false)
        )
        val viewModel = TimerViewModel(
            solveRepository = FakeSolveRepository(),
            timerSettingsRepository = settingsRepository,
            scrambleGenerator = ScrambleGenerator(Random(27)),
            elapsedRealtimeMillis = { 500L }
        )
        advanceUntilIdle()

        viewModel.onTimerPressed()

        assertEquals(TimerPhase.RUNNING, viewModel.state.value.session.phase)
        assertEquals(500L, viewModel.state.value.session.startedAtMillis)

        viewModel.onTimerPressed()
        advanceUntilIdle()
    }

    @Test
    fun `short hold returns to idle without starting`() = runTest {
        val viewModel = TimerViewModel(
            solveRepository = FakeSolveRepository(),
            timerSettingsRepository = FakeTimerSettingsRepository(),
            scrambleGenerator = ScrambleGenerator(Random(28))
        )

        viewModel.onTimerPressStarted()
        advanceTimeBy(549L)
        runCurrent()

        assertEquals(TimerInputState.HOLDING, viewModel.state.value.inputState)

        viewModel.onTimerReleased()

        assertEquals(TimerInputState.IDLE, viewModel.state.value.inputState)
        assertEquals(TimerPhase.READY, viewModel.state.value.session.phase)
    }

    @Test
    fun `armed hold starts inspection on release`() = runTest {
        val viewModel = TimerViewModel(
            solveRepository = FakeSolveRepository(),
            timerSettingsRepository = FakeTimerSettingsRepository(),
            scrambleGenerator = ScrambleGenerator(Random(29)),
            elapsedRealtimeMillis = { 0L }
        )

        viewModel.onTimerPressStarted()
        advanceTimeBy(550L)
        runCurrent()

        assertEquals(TimerInputState.ARMED, viewModel.state.value.inputState)
        assertEquals(TimerPhase.READY, viewModel.state.value.session.phase)

        viewModel.onTimerReleased()

        assertEquals(TimerInputState.IDLE, viewModel.state.value.inputState)
        assertEquals(TimerPhase.INSPECTING, viewModel.state.value.session.phase)

        viewModel.onTimerPressed()
        viewModel.onTimerPressed()
        advanceUntilIdle()
    }

    @Test
    fun `pressing down while running stops immediately`() = runTest {
        var elapsedRealtime = 100L
        val repository = FakeSolveRepository()
        val viewModel = TimerViewModel(
            solveRepository = repository,
            timerSettingsRepository = FakeTimerSettingsRepository(),
            scrambleGenerator = ScrambleGenerator(Random(30)),
            elapsedRealtimeMillis = { elapsedRealtime },
            currentTimeMillis = { 1_700_000_000_000L }
        )
        viewModel.onTimerPressed()
        viewModel.onTimerPressed()
        elapsedRealtime = 1_100L

        viewModel.onTimerPressStarted()
        advanceUntilIdle()

        assertEquals(TimerPhase.READY, viewModel.state.value.session.phase)
        assertEquals(TimerInputState.IDLE, viewModel.state.value.inputState)
        assertEquals(1_000L, repository.current.recentSolves.single().durationMillis)

        viewModel.onTimerReleased()
        assertEquals(TimerPhase.READY, viewModel.state.value.session.phase)
    }

    @Test
    fun `cancelled armed hold does not start`() = runTest {
        val viewModel = TimerViewModel(
            solveRepository = FakeSolveRepository(),
            timerSettingsRepository = FakeTimerSettingsRepository(),
            scrambleGenerator = ScrambleGenerator(Random(31))
        )

        viewModel.onTimerPressStarted()
        advanceTimeBy(550L)
        runCurrent()
        assertEquals(TimerInputState.ARMED, viewModel.state.value.inputState)

        viewModel.onTimerPressCancelled()

        assertEquals(TimerInputState.IDLE, viewModel.state.value.inputState)
        assertEquals(TimerPhase.READY, viewModel.state.value.session.phase)
    }

    @Test
    fun `inspection emits haptic cues once at eight and twelve seconds`() = runTest {
        var elapsedRealtime = 0L
        val viewModel = TimerViewModel(
            solveRepository = FakeSolveRepository(),
            timerSettingsRepository = FakeTimerSettingsRepository(),
            scrambleGenerator = ScrambleGenerator(Random(29)),
            elapsedRealtimeMillis = { elapsedRealtime }
        )
        val cues = mutableListOf<InspectionCue>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.inspectionCues.take(2).toList(cues)
        }

        viewModel.onTimerPressed()
        elapsedRealtime = 7_999L
        advanceTimeBy(16L)
        runCurrent()
        assertEquals(emptyList<InspectionCue>(), cues)

        elapsedRealtime = 8_000L
        advanceTimeBy(16L)
        elapsedRealtime = 12_000L
        advanceTimeBy(16L)
        runCurrent()

        assertEquals(
            listOf(InspectionCue.EIGHT_SECONDS, InspectionCue.TWELVE_SECONDS),
            cues
        )

        viewModel.onTimerPressed()
        viewModel.onTimerPressed()
        advanceUntilIdle()
    }

    @Test
    fun `disabled inspection haptics emit no cues`() = runTest {
        var elapsedRealtime = 0L
        val viewModel = TimerViewModel(
            solveRepository = FakeSolveRepository(),
            timerSettingsRepository = FakeTimerSettingsRepository(
                TimerSettings(inspectionHapticsEnabled = false)
            ),
            scrambleGenerator = ScrambleGenerator(Random(30)),
            elapsedRealtimeMillis = { elapsedRealtime }
        )
        val cues = mutableListOf<InspectionCue>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.inspectionCues.take(1).toList(cues)
        }

        viewModel.onTimerPressed()
        elapsedRealtime = 12_000L
        advanceTimeBy(16L)
        runCurrent()

        assertEquals(emptyList<InspectionCue>(), cues)

        viewModel.onTimerPressed()
        viewModel.onTimerPressed()
        advanceUntilIdle()
    }

    @Test
    fun `inspection controls update persisted timer settings`() = runTest {
        val viewModel = TimerViewModel(
            solveRepository = FakeSolveRepository(),
            timerSettingsRepository = FakeTimerSettingsRepository(),
            scrambleGenerator = ScrambleGenerator(Random(31))
        )

        viewModel.setInspectionEnabled(false)
        viewModel.setInspectionHapticsEnabled(false)
        advanceUntilIdle()

        assertEquals(false, viewModel.state.value.timerSettings.inspectionEnabled)
        assertEquals(false, viewModel.state.value.timerSettings.inspectionHapticsEnabled)
    }

    private class FakeTimerSettingsRepository(
        initial: TimerSettings = TimerSettings()
    ) : TimerSettingsRepository {
        private val mutableSettings = MutableStateFlow(initial)
        override val settings: Flow<TimerSettings> = mutableSettings

        override suspend fun setInspectionEnabled(enabled: Boolean) {
            mutableSettings.value = mutableSettings.value.copy(inspectionEnabled = enabled)
        }

        override suspend fun setInspectionHapticsEnabled(enabled: Boolean) {
            mutableSettings.value = mutableSettings.value.copy(
                inspectionHapticsEnabled = enabled
            )
        }
    }

    private class FakeSolveRepository : SolveRepository {
        private val solves = MutableStateFlow(emptyList<SolveRecord>())
        val current: SolveHistory get() = solves.value.toHistory()
        private var nextId = 1L

        override fun observeHistory(query: SolveHistoryQuery): Flow<SolveHistory> =
            solves.map { records ->
                records
                    .filter { it.completedAtEpochMillis >= query.sinceEpochMillis }
                    .toHistory()
            }

        override suspend fun addSolve(
            durationMillis: Long,
            scramble: String,
            completedAtEpochMillis: Long,
            penalty: SolvePenalty
        ) {
            val solve = SolveRecord(
                id = nextId++,
                durationMillis = durationMillis,
                scramble = scramble,
                completedAtEpochMillis = completedAtEpochMillis,
                penalty = penalty
            )
            publish(listOf(solve) + solves.value)
        }

        override suspend fun deleteSolve(id: Long) {
            publish(solves.value.filterNot { it.id == id })
        }

        override suspend fun setPenalty(id: Long, penalty: SolvePenalty) {
            publish(
                solves.value.map { solve ->
                    if (solve.id == id) solve.copy(penalty = penalty) else solve
                }
            )
        }

        override suspend fun setNote(id: Long, note: String?) {
            publish(
                solves.value.map { solve ->
                    if (solve.id == id) {
                        solve.copy(note = SolveRecord.normalizeNote(note))
                    } else {
                        solve
                    }
                }
            )
        }

        private fun publish(solves: List<SolveRecord>) {
            this.solves.value = solves
        }

        private fun List<SolveRecord>.toHistory(): SolveHistory =
            SolveHistory(
                recentSolves = this,
                stats = SolveStats(
                    solveCount = size.toLong(),
                    lastSolveMillis = firstOrNull()?.durationMillis,
                    lastSolvePenalty = firstOrNull()?.penalty,
                    bestSolveMillis = mapNotNull(SolveRecord::adjustedDurationMillis).minOrNull(),
                    averageOf5 = calculateWcaAverage(this, 5),
                    averageOf12 = calculateWcaAverage(this, 12)
                )
            )
    }
}
