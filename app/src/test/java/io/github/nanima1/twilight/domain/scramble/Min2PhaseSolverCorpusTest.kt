package io.github.nanima1.twilight.domain.scramble

import cs.min2phase.Tools
import io.github.nanima1.twilight.solver.Min2PhaseSolver
import io.github.nanima1.twilight.solver.SolutionMethod
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Min2PhaseSolverCorpusTest {
    @Test
    fun `deterministic scramble corpus produces valid solutions`() {
        val generator = ScrambleGenerator(Random(CORPUS_SEED))
        val solver = Min2PhaseSolver()
        solver.initialize()

        repeat(CORPUS_SIZE) {
            val scramble = generator.generate()
            val solution = solver.solve(scramble)

            assertEquals(SolutionMethod.TWO_PHASE, solution.method)
            assertTrue(solution.moveCount <= 21)
            assertEquals(SOLVED, Tools.fromScramble("$scramble ${solution.algorithm}"))
        }
    }

    private companion object {
        const val CORPUS_SEED = 20_260_727
        const val CORPUS_SIZE = 128
        const val SOLVED = "UUUUUUUUURRRRRRRRRFFFFFFFFFDDDDDDDDDLLLLLLLLLBBBBBBBBB"
    }
}
