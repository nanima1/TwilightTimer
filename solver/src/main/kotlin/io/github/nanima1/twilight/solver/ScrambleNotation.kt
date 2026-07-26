package io.github.nanima1.twilight.solver

internal object ScrambleNotation {
    private val move = Regex("[RLUDFB](?:2|')?")
    private val whitespace = Regex("\\s+")

    fun parse(scramble: String): List<String> {
        val normalized = scramble.trim()
        if (normalized.isEmpty()) {
            throw InvalidScrambleException("Scramble must contain at least one move.")
        }

        val moves = normalized.split(whitespace)
        moves.firstOrNull { !move.matches(it) }?.let { invalidMove ->
            throw InvalidScrambleException("Unsupported move: $invalidMove")
        }
        return moves
    }
}
