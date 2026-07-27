package io.github.nanima1.twilight.solver

import cs.min2phase.PrecomputedTables
import cs.min2phase.Search
import cs.min2phase.Tools
import java.io.IOException

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
        initializeTables()
    }

    override fun solve(scramble: String): CubeSolution {
        return solveInternal(scramble, cancellationCheck = null)
    }

    fun solve(
        scramble: String,
        cancellationCheck: () -> Unit
    ): CubeSolution {
        return solveInternal(scramble, Runnable(cancellationCheck))
    }

    private fun solveInternal(
        scramble: String,
        cancellationCheck: Runnable?
    ): CubeSolution {
        val moves = ScrambleNotation.parse(scramble)
        initialize()

        val facelets = Tools.fromScramble(moves.joinToString(separator = " "))
        val search = Search()
        val result = if (cancellationCheck == null) {
            search.solution(facelets, maxDepth, probeMax, 0L, 0)
        } else {
            search.solution(facelets, maxDepth, probeMax, 0L, 0, cancellationCheck)
        }.trim()

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
        @Volatile
        var tablesInitialized = false

        val initializationLock = Any()

        const val DEFAULT_MAX_DEPTH = 21
        const val MAX_SUPPORTED_DEPTH = 30
        const val DEFAULT_PROBE_MAX = 10_000_000L
        const val ERROR_PREFIX = "Error"
        const val TABLE_RESOURCE = "/cs/min2phase/tables.bin"

        fun initializeTables() {
            if (tablesInitialized) return

            synchronized(initializationLock) {
                if (tablesInitialized) return

                if (!loadPrecomputedTables()) {
                    Search.init()
                }
                tablesInitialized = true
            }
        }

        fun loadPrecomputedTables(): Boolean {
            val tableStream = Min2PhaseSolver::class.java.getResourceAsStream(TABLE_RESOURCE)
                ?: return false
            return try {
                val data = tableStream.use { it.readBytes() }
                PrecomputedTables.initFrom(data)
                true
            } catch (_: IOException) {
                false
            }
        }
    }
}
