package io.github.nanima1.twilight.solver

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class InverseScrambleSolverTest {
    private val solver: CubeSolver = InverseScrambleSolver()

    @Test
    fun `solution reverses move order and direction`() {
        val solution = solver.solve("R U2 F' L D")

        assertEquals("D' L' F U2 R'", solution.algorithm)
        assertEquals(5, solution.moveCount)
        assertEquals(SolutionMethod.INVERSE_SCRAMBLE, solution.method)
    }

    @Test
    fun `solution normalizes whitespace`() {
        val solution = solver.solve("  R\tU'\nB2  ")

        assertEquals("B2 U R'", solution.algorithm)
        assertEquals(3, solution.moveCount)
    }

    @Test
    fun `empty scramble is rejected`() {
        assertThrows(InvalidScrambleException::class.java) {
            solver.solve("  \n ")
        }
    }

    @Test
    fun `unsupported notation is rejected`() {
        val error = assertThrows(InvalidScrambleException::class.java) {
            solver.solve("R U Rw")
        }

        assertEquals("Unsupported move: Rw", error.message)
    }

    @Test
    fun `moves without separating whitespace are rejected`() {
        val error = assertThrows(InvalidScrambleException::class.java) {
            solver.solve("R UF")
        }

        assertEquals("Unsupported move: UF", error.message)
    }

    @Test
    fun `moves with compound suffixes are rejected`() {
        val error = assertThrows(InvalidScrambleException::class.java) {
            solver.solve("R U2' F")
        }

        assertEquals("Unsupported move: U2'", error.message)
    }
}
