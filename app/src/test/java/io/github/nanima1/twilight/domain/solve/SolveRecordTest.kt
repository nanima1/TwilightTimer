package io.github.nanima1.twilight.domain.solve

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SolveRecordTest {
    @Test
    fun `note normalization trims surrounding whitespace`() {
        assertEquals("Smooth execution", SolveRecord.normalizeNote("  Smooth execution  "))
    }

    @Test
    fun `blank note is removed`() {
        assertNull(SolveRecord.normalizeNote("  \n  "))
        assertNull(SolveRecord.normalizeNote(null))
    }

    @Test
    fun `note is limited to the persisted maximum`() {
        val note = "a".repeat(SolveRecord.MAX_NOTE_LENGTH + 20)

        assertEquals(
            "a".repeat(SolveRecord.MAX_NOTE_LENGTH),
            SolveRecord.normalizeNote(note)
        )
    }
}
