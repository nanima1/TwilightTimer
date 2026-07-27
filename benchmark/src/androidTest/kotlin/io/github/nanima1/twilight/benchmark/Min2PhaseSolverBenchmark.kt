package io.github.nanima1.twilight.benchmark

import android.util.Log
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import cs.min2phase.Tools
import io.github.nanima1.twilight.solver.Min2PhaseSolver
import io.github.nanima1.twilight.solver.SolutionMethod
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Min2PhaseSolverBenchmark {
    @get:Rule
    val benchmarkRule = BenchmarkRule()

    private val solver = Min2PhaseSolver()

    @Before
    fun initializeSolver() {
        val startNanos = System.nanoTime()
        solver.initialize()
        Log.i(LOG_TAG, "full_init_nanos=${System.nanoTime() - startNanos}")
    }

    @Test
    fun solveWarmGeneratedLengthScramble() {
        solveRepeatedly(REPRESENTATIVE_SCRAMBLE)
    }

    @Test
    fun solveWarmHardCorpusScramble() {
        solveRepeatedly(HARD_CORPUS_SCRAMBLE)
    }

    private fun solveRepeatedly(scramble: String) {
        benchmarkRule.measureRepeated {
            val solution = solver.solve(scramble)
            runWithTimingDisabled {
                check(solution.method == SolutionMethod.TWO_PHASE)
                check(Tools.fromScramble("$scramble ${solution.algorithm}") == SOLVED)
            }
        }
    }

    private companion object {
        const val REPRESENTATIVE_SCRAMBLE =
            "R U2 F' L D2 B U' R2 F D' L2 U B' R D F2 L' U2 B R'"
        const val HARD_CORPUS_SCRAMBLE =
            "B' L2 D R' U2 F2 R B U F' R' B L2 D B2 L' D2 R2 F D'"
        const val SOLVED = "UUUUUUUUURRRRRRRRRFFFFFFFFFDDDDDDDDDLLLLLLLLLBBBBBBBBB"
        const val LOG_TAG = "TwilightSolverBenchmark"
    }
}
