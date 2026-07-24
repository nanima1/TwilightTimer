package io.github.nanima1.twilight.domain.scramble

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ScrambleGeneratorTest {
    @Test
    fun `generated scramble has requested number of moves and no repeated axes`() {
        val scramble = ScrambleGenerator(Random(17)).generate(moveCount = 40)
        val moves = scramble.split(" ")

        assertEquals(40, moves.size)
        moves.zipWithNext().forEach { (first, second) ->
            assertFalse(axisOf(first) == axisOf(second))
        }
    }

    private fun axisOf(move: String): Char = when (move.first()) {
        'R', 'L' -> 'X'
        'U', 'D' -> 'Y'
        'F', 'B' -> 'Z'
        else -> error("Unexpected move: $move")
    }
}
