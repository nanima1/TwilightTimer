package io.github.nanima1.twilight.presentation

import org.junit.Assert.assertEquals
import org.junit.Test

class TimerFormattingTest {
    @Test
    fun `inspection readout counts down by whole seconds`() {
        assertEquals("15", formatInspectionReadout(-1L))
        assertEquals("15", formatInspectionReadout(0L))
        assertEquals("15", formatInspectionReadout(1L))
        assertEquals("14", formatInspectionReadout(1_000L))
        assertEquals("1", formatInspectionReadout(14_999L))
        assertEquals("0", formatInspectionReadout(15_000L))
    }

    @Test
    fun `inspection readout shows penalty states after the limit`() {
        assertEquals("+2", formatInspectionReadout(15_001L))
        assertEquals("+2", formatInspectionReadout(17_000L))
        assertEquals("DNF", formatInspectionReadout(17_001L))
    }
}
