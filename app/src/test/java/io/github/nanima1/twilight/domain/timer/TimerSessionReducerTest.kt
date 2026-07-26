package io.github.nanima1.twilight.domain.timer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TimerSessionReducerTest {
    @Test
    fun `stopping a session preserves the completed duration`() {
        val running = TimerSessionReducer.start(TimerSession(), nowMillis = 100L)

        val stopped = TimerSessionReducer.stop(running, nowMillis = 12_445L)

        assertEquals(TimerPhase.READY, stopped.phase)
        assertNull(stopped.startedAtMillis)
        assertEquals(12_345L, stopped.elapsedMillis)
    }

    @Test
    fun `starting another solve clears the previous duration`() {
        val running = TimerSessionReducer.start(
            TimerSession(elapsedMillis = 9_876L),
            nowMillis = 20L
        )

        assertEquals(0L, running.elapsedMillis)
    }
}
