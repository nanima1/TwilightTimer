package io.github.nanima1.twilight.solver

fun interface CubeSolver {
    /**
     * Returns a solution in Singmaster face-turn notation.
     *
     * Implementations are blocking. Callers integrating a search-based solver must invoke this
     * interface away from the main thread.
     *
     * @throws InvalidScrambleException when [scramble] is empty or unsupported.
     */
    fun solve(scramble: String): CubeSolution
}

data class CubeSolution(
    val algorithm: String,
    val moveCount: Int,
    val method: SolutionMethod
)

enum class SolutionMethod {
    INVERSE_SCRAMBLE
}

class InvalidScrambleException(message: String) : IllegalArgumentException(message)
