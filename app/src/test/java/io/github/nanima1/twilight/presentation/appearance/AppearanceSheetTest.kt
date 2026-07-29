package io.github.nanima1.twilight.presentation.appearance

import org.junit.Assert.assertEquals
import org.junit.Test

class AppearanceSheetTest {
    @Test
    fun `selected item offset centers it in the viewport`() {
        assertEquals(-350, centeredItemScrollOffset(viewportWidthPx = 1_000, itemWidthPx = 300))
    }

    @Test
    fun `selected item offset does not shift items wider than the viewport`() {
        assertEquals(0, centeredItemScrollOffset(viewportWidthPx = 300, itemWidthPx = 400))
    }
}
