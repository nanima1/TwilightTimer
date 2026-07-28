package io.github.nanima1.twilight.solver

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ScrambleNotationTest {
    @Test
    fun `encoded moves match min2phase move indices`() {
        assertArrayEquals(
            IntArray(18) { it },
            ScrambleNotation.parseEncoded(
                "U U2 U' R R2 R' F F2 F' D D2 D' L L2 L' B B2 B'"
            )
        )
    }

    @Test
    fun `encoded parser preserves validation`() {
        assertThrows(InvalidScrambleException::class.java) {
            ScrambleNotation.parseEncoded("R U Rw")
        }
    }
}
