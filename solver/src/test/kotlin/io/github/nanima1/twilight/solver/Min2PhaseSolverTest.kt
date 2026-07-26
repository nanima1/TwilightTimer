package io.github.nanima1.twilight.solver

import cs.min2phase.Tools
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class Min2PhaseSolverTest {
    private val solver = Min2PhaseSolver()

    @Test
    fun `solution replays to a solved cube`() {
        val solution = solver.solve(SCRAMBLE)

        assertEquals(SolutionMethod.TWO_PHASE, solution.method)
        assertTrue(solution.moveCount <= 21)
        assertEquals(SOLVED, Tools.fromScramble("$SCRAMBLE ${solution.algorithm}"))
    }

    @Test
    fun `simple scramble produces a valid solution`() {
        val solution = solver.solve("R U R' U'")

        assertEquals(SOLVED, Tools.fromScramble("R U R' U' ${solution.algorithm}"))
    }

    @Test
    fun `cancelled scramble produces an empty solution`() {
        val solution = solver.solve("R R'")

        assertEquals("", solution.algorithm)
        assertEquals(0, solution.moveCount)
    }

    @Test
    fun `unsupported maximum depth is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            Min2PhaseSolver(maxDepth = 31)
        }
    }

    private companion object {
        const val SCRAMBLE = "R U2 F' L D2 B U' R2 F D' L2 U B' R D F2 L' U2 B R'"
        const val SOLVED = "UUUUUUUUURRRRRRRRRFFFFFFFFFDDDDDDDDDLLLLLLLLLBBBBBBBBB"
    }
}
