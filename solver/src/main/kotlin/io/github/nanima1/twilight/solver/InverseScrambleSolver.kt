package io.github.nanima1.twilight.solver

class InverseScrambleSolver : CubeSolver {
    override fun solve(scramble: String): CubeSolution {
        val normalized = scramble.trim()
        if (normalized.isEmpty()) {
            throw InvalidScrambleException("Scramble must contain at least one move.")
        }

        val moves = normalized.split(WHITESPACE)
        moves.firstOrNull { !MOVE.matches(it) }?.let { invalidMove ->
            throw InvalidScrambleException("Unsupported move: $invalidMove")
        }

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

    private companion object {
        val MOVE = Regex("[RLUDFB](?:2|')?")
        val WHITESPACE = Regex("\\s+")
    }
}
