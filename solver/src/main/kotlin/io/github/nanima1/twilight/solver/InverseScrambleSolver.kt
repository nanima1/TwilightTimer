package io.github.nanima1.twilight.solver

class InverseScrambleSolver : CubeSolver {
    override fun solve(scramble: String): CubeSolution {
        val moves = ScrambleNotation.parse(scramble)

        return CubeSolution(
            algorithm = moves.asReversed().joinToString(separator = " ", transform = ::inverseOf),
            moveCount = moves.size,
            method = SolutionMethod.INVERSE_SCRAMBLE
        )
    }

    private fun inverseOf(move: String): String = when {
        move.endsWith("2") -> move
        move.endsWith("'") -> move.dropLast(1)
        else -> "$move'"
    }
}
