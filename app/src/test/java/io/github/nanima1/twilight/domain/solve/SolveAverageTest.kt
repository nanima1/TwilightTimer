package io.github.nanima1.twilight.domain.solve

import org.junit.Assert.assertEquals
import org.junit.Test

class SolveAverageTest {
    @Test
    fun `average is unavailable until the sample is complete`() {
        val solves = listOf(solve(1L, 10_000L), solve(2L, 11_000L))

        assertEquals(SolveAverage.Unavailable, calculateWcaAverage(solves, 5))
    }

    @Test
    fun `average of five drops the best and worst results`() {
        val solves = durations(10_000L, 11_000L, 12_000L, 13_000L, 20_000L)

        assertEquals(SolveAverage.Time(12_000L), calculateWcaAverage(solves, 5))
    }

    @Test
    fun `one dnf is dropped as the worst result`() {
        val solves = listOf(
            solve(1L, 10_000L),
            solve(2L, 11_000L),
            solve(3L, 12_000L),
            solve(4L, 13_000L),
            solve(5L, 9_000L, SolvePenalty.DNF)
        )

        assertEquals(SolveAverage.Time(12_000L), calculateWcaAverage(solves, 5))
    }

    @Test
    fun `two dnfs make the average dnf`() {
        val solves = listOf(
            solve(1L, 10_000L),
            solve(2L, 11_000L),
            solve(3L, 12_000L),
            solve(4L, 9_000L, SolvePenalty.DNF),
            solve(5L, 8_000L, SolvePenalty.DNF)
        )

        assertEquals(SolveAverage.Dnf, calculateWcaAverage(solves, 5))
    }

    @Test
    fun `plus two participates with its adjusted duration`() {
        val solves = listOf(
            solve(1L, 9_000L, SolvePenalty.PLUS_TWO),
            solve(2L, 10_000L),
            solve(3L, 12_000L),
            solve(4L, 13_000L),
            solve(5L, 14_000L)
        )

        assertEquals(SolveAverage.Time(12_000L), calculateWcaAverage(solves, 5))
    }

    @Test
    fun `average rounds to the nearest centisecond`() {
        val solves = durations(9_000L, 10_000L, 10_010L, 10_010L, 12_000L)

        assertEquals(SolveAverage.Time(10_010L), calculateWcaAverage(solves, 5))
    }

    @Test
    fun `only the newest sample is used`() {
        val solves = listOf(solve(1L, 1_000L)) +
            durations(10_000L, 11_000L, 12_000L, 13_000L, 20_000L, firstId = 2L)

        assertEquals(SolveAverage.Time(12_000L), calculateWcaAverage(solves, 5))
    }

    @Test
    fun `average of twelve trims one result from each end`() {
        val solves = durations(*(1L..12L).map { it * 1_000L }.toLongArray())

        assertEquals(SolveAverage.Time(6_500L), calculateWcaAverage(solves, 12))
    }

    private fun durations(
        vararg durations: Long,
        firstId: Long = 1L
    ): List<SolveRecord> = durations.mapIndexed { index, duration ->
        solve(firstId + index, duration)
    }

    private fun solve(
        id: Long,
        durationMillis: Long,
        penalty: SolvePenalty = SolvePenalty.NONE
    ): SolveRecord = SolveRecord(
        id = id,
        durationMillis = durationMillis,
        scramble = "R U",
        completedAtEpochMillis = id,
        penalty = penalty
    )
}
