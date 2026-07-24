package io.github.nanima1.twilight.domain.timer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TimerSessionReducerTest {
    @Test
    fun `stopping a session records last best and solve count`() {
        val running = TimerSessionReducer.start(TimerSession(), nowMillis = 100L)

        val stopped = TimerSessionReducer.stop(running, nowMillis = 12_445L)

        assertEquals(TimerPhase.READY, stopped.phase)
        assertNull(stopped.startedAtMillis)
        assertEquals(12_345L, stopped.lastSolveMillis)
        assertEquals(12_345L, stopped.bestSolveMillis)
        assertEquals(1, stopped.solveCount)
    }

    @Test
    fun `a slower solve preserves the best result`() {
        val previous = TimerSession(bestSolveMillis = 9_876L)
        val running = TimerSessionReducer.start(previous, nowMillis = 20L)

        val stopped = TimerSessionReducer.stop(running, nowMillis = 12_000L)

        assertEquals(9_876L, stopped.bestSolveMillis)
    }
}
