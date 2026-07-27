package io.github.nanima1.twilight.domain.solve

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SolveTrendTest {
    @Test
    fun `trend keeps the newest window in chronological order`() {
        val solves = (1L..25L).map { id -> solve(id = id, durationMillis = id * 1_000L) }

        val trend = buildSolveTrend(solves)

        assertEquals((6L..25L).toList(), trend.map(SolveTrendPoint::solveId))
    }

    @Test
    fun `trend applies penalties and preserves dnf gaps`() {
        val solves = listOf(
            solve(id = 1L, durationMillis = 10_000L),
            solve(id = 2L, durationMillis = 11_000L, penalty = SolvePenalty.PLUS_TWO),
            solve(id = 3L, durationMillis = 12_000L, penalty = SolvePenalty.DNF)
        )

        val trend = buildSolveTrend(solves)

        assertEquals(10_000L, trend[0].adjustedDurationMillis)
        assertEquals(13_000L, trend[1].adjustedDurationMillis)
        assertNull(trend[2].adjustedDurationMillis)
    }

    @Test
    fun `trend supports a smaller explicit window`() {
        val solves = (1L..5L).map { id -> solve(id = id, durationMillis = id * 1_000L) }

        val trend = buildSolveTrend(solves, maxPoints = 3)

        assertEquals(listOf(3L, 4L, 5L), trend.map(SolveTrendPoint::solveId))
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
