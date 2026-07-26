package io.github.nanima1.twilight.solver

import cs.min2phase.Search
import cs.min2phase.Tools

class Min2PhaseSolver(
    private val maxDepth: Int = DEFAULT_MAX_DEPTH,
    private val probeMax: Long = DEFAULT_PROBE_MAX
) : CubeSolver {
    init {
        require(maxDepth in 1..MAX_SUPPORTED_DEPTH) {
            "Maximum depth must be between 1 and $MAX_SUPPORTED_DEPTH."
        }
        require(probeMax > 0) { "Probe limit must be positive." }
    }

    fun initialize() {
        Search.init()
    }

    override fun solve(scramble: String): CubeSolution {
        val moves = ScrambleNotation.parse(scramble)
        initialize()

        val result = Search().solution(
            Tools.fromScramble(moves.joinToString(separator = " ")),
            maxDepth,
            probeMax,
            0L,
            0
        ).trim()

        if (result.startsWith(ERROR_PREFIX)) {
            throw SolverComputationException("Two-phase solver failed: $result")
        }

        val solutionMoves = if (result.isEmpty()) {
            emptyList()
        } else {
            ScrambleNotation.parse(result)
        }
        return CubeSolution(
            algorithm = solutionMoves.joinToString(separator = " "),
            moveCount = solutionMoves.size,
            method = SolutionMethod.TWO_PHASE
        )
    }

    private companion object {
        const val DEFAULT_MAX_DEPTH = 21
        const val MAX_SUPPORTED_DEPTH = 30
        const val DEFAULT_PROBE_MAX = 10_000_000L
        const val ERROR_PREFIX = "Error"
    }
}
