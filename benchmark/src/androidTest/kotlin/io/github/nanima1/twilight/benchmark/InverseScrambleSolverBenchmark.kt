package io.github.nanima1.twilight.benchmark

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.nanima1.twilight.solver.CubeSolver
import io.github.nanima1.twilight.solver.InverseScrambleSolver
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InverseScrambleSolverBenchmark {
    @get:Rule
    val benchmarkRule = BenchmarkRule()

    private val solver: CubeSolver = InverseScrambleSolver()

    @Test
    fun solveGeneratedLengthScramble() {
        benchmarkRule.measureRepeated {
            val solution = solver.solve(SCRAMBLE)
            runWithTimingDisabled {
                check(solution.moveCount == 20)
            }
        }
    }

    private companion object {
        const val SCRAMBLE = "R U2 F' L D2 B U' R2 F D' L2 U B' R D F2 L' U2 B R'"
    }
}
