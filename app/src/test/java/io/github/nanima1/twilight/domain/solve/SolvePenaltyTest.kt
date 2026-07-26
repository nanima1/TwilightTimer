package io.github.nanima1.twilight.domain.solve

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SolvePenaltyTest {
    @Test
    fun `plus two adds two seconds to the recorded duration`() {
        assertEquals(14_345L, SolvePenalty.PLUS_TWO.applyTo(12_345L))
    }

    @Test
    fun `dnf has no adjusted duration`() {
        assertNull(SolvePenalty.DNF.applyTo(12_345L))
    }

    @Test
    fun `unknown persisted penalty falls back to none`() {
        assertEquals(SolvePenalty.NONE, SolvePenalty.fromId("unknown"))
        assertEquals(SolvePenalty.NONE, SolvePenalty.fromId(null))
    }

    @Test
    fun `plus two clamps before adding at the long boundary`() {
        assertEquals(Long.MAX_VALUE, SolvePenalty.PLUS_TWO.applyTo(Long.MAX_VALUE))
    }
}
