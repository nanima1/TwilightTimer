package io.github.nanima1.twilight.solver

import cs.min2phase.PrecomputedTables
import cs.min2phase.Tools
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.IOException
import java.security.MessageDigest
import java.util.concurrent.Executors
import org.junit.Assert.assertArrayEquals
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
    fun `solver accepts mixed whitespace between moves`() {
        val solution = solver.solve("  R\tU'\nB2  ")

        assertEquals(SOLVED, Tools.fromScramble("R U' B2 ${solution.algorithm}"))
    }

    @Test
    fun `cancelled scramble produces an empty solution`() {
        val solution = solver.solve("R R'")

        assertEquals("", solution.algorithm)
        assertEquals(0, solution.moveCount)
    }

    @Test
    fun `active search observes cooperative cancellation and leaves solver reusable`() {
        var cancellationChecks = 0

        assertThrows(TestCancellation::class.java) {
            solver.solve(SLOW_TAIL_SCRAMBLE) {
                cancellationChecks++
                if (cancellationChecks >= 2) {
                    throw TestCancellation()
                }
            }
        }

        assertTrue(cancellationChecks >= 2)
        val recovered = solver.solve(SCRAMBLE)
        assertEquals(SOLVED, Tools.fromScramble("$SCRAMBLE ${recovered.algorithm}"))
    }

    @Test
    fun `shared solver handles concurrent requests`() {
        val executor = Executors.newFixedThreadPool(2)
        try {
            val scrambles = listOf(SCRAMBLE, SLOW_TAIL_SCRAMBLE)
            val solutions = scrambles.map { scramble ->
                executor.submit<CubeSolution> { solver.solve(scramble) }
            }.map { future -> future.get() }

            scrambles.zip(solutions).forEach { (scramble, solution) ->
                assertEquals(SOLVED, Tools.fromScramble("$scramble ${solution.algorithm}"))
            }
        } finally {
            executor.shutdownNow()
        }
    }

    @Test
    fun `unsupported maximum depth is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            Min2PhaseSolver(maxDepth = 31)
        }
    }

    @Test
    fun `precomputed tables have the expected content`() {
        val tables = requireNotNull(
            Min2PhaseSolver::class.java.getResourceAsStream(TABLE_RESOURCE)
        ).use { it.readBytes() }
        val generatedTables = ByteArrayOutputStream().use { output ->
            DataOutputStream(output).use(Tools::saveTo)
            output.toByteArray()
        }

        assertEquals(PrecomputedTables.TABLE_SIZE_BYTES, tables.size)
        assertEquals(TABLE_SHA_256, tables.sha256())
        assertArrayEquals(generatedTables, tables)
    }

    @Test
    fun `precomputed tables reject an unexpected size`() {
        assertThrows(IOException::class.java) {
            PrecomputedTables.initFrom(ByteArray(PrecomputedTables.TABLE_SIZE_BYTES - 1))
        }
    }

    private fun ByteArray.sha256(): String =
        MessageDigest.getInstance("SHA-256")
            .digest(this)
            .joinToString(separator = "") { byte -> "%02x".format(byte) }

    private class TestCancellation : RuntimeException()

    private companion object {
        const val SCRAMBLE = "R U2 F' L D2 B U' R2 F D' L2 U B' R D F2 L' U2 B R'"
        const val SLOW_TAIL_SCRAMBLE =
            "B' L2 D R' U2 F2 R B U F' R' B L2 D B2 L' D2 R2 F D'"
        const val SOLVED = "UUUUUUUUURRRRRRRRRFFFFFFFFFDDDDDDDDDLLLLLLLLLBBBBBBBBB"
        const val TABLE_RESOURCE = "/cs/min2phase/tables.bin"
        const val TABLE_SHA_256 =
            "588e6d14418416df076762abe0f54b2fb4197df9bec301dd77f5b1987127f8d1"
    }
}
