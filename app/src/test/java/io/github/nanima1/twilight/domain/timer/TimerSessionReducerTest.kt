package io.github.nanima1.twilight.domain.timer

import io.github.nanima1.twilight.domain.solve.SolvePenalty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TimerSessionReducerTest {
    @Test
    fun `stopping a session preserves the completed duration and inspection penalty`() {
        val inspecting = TimerSessionReducer.beginInspection(TimerSession(), nowMillis = 100L)
        val running = TimerSessionReducer.start(inspecting, nowMillis = 15_101L)

        val stopped = TimerSessionReducer.stop(running, nowMillis = 27_446L)

        assertEquals(TimerPhase.READY, stopped.phase)
        assertNull(stopped.startedAtMillis)
        assertEquals(12_345L, stopped.elapsedMillis)
        assertEquals(SolvePenalty.PLUS_TWO, stopped.penalty)
    }

    @Test
    fun `beginning inspection clears the previous attempt`() {
        val previous = TimerSession(
            elapsedMillis = 9_876L,
            inspectionElapsedMillis = 17_100L,
            penalty = SolvePenalty.DNF
        )

        val inspecting = TimerSessionReducer.beginInspection(previous, nowMillis = 20L)

        assertEquals(TimerPhase.INSPECTING, inspecting.phase)
        assertEquals(0L, inspecting.elapsedMillis)
        assertEquals(0L, inspecting.inspectionElapsedMillis)
        assertEquals(SolvePenalty.NONE, inspecting.penalty)
    }

    @Test
    fun `inspection tick tracks monotonic elapsed time`() {
        val inspecting = TimerSessionReducer.beginInspection(TimerSession(), nowMillis = 100L)

        val ticked = TimerSessionReducer.tick(inspecting, nowMillis = 8_100L)

        assertEquals(8_000L, ticked.inspectionElapsedMillis)
        assertEquals(0L, ticked.elapsedMillis)
    }

    @Test
    fun `inspection boundaries assign WCA penalties`() {
        val inspecting = TimerSessionReducer.beginInspection(TimerSession(), nowMillis = 100L)

        val atLimit = TimerSessionReducer.start(inspecting, nowMillis = 15_100L)
        val afterLimit = TimerSessionReducer.start(inspecting, nowMillis = 15_101L)
        val atDnfBoundary = TimerSessionReducer.start(inspecting, nowMillis = 17_100L)
        val afterDnfBoundary = TimerSessionReducer.start(inspecting, nowMillis = 17_101L)

        assertEquals(SolvePenalty.NONE, atLimit.penalty)
        assertEquals(SolvePenalty.PLUS_TWO, afterLimit.penalty)
        assertEquals(SolvePenalty.PLUS_TWO, atDnfBoundary.penalty)
        assertEquals(SolvePenalty.DNF, afterDnfBoundary.penalty)
    }

    @Test
    fun `starting a solve clears the previous duration`() {
        val inspecting = TimerSessionReducer.beginInspection(
            TimerSession(elapsedMillis = 9_876L),
            nowMillis = 20L
        )
        val running = TimerSessionReducer.start(inspecting, nowMillis = 20L)

        assertEquals(0L, running.elapsedMillis)
    }
}
