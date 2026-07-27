package io.github.nanima1.twilight.solver

internal object ScrambleNotation {
    fun parse(scramble: String): List<String> {
        val moves = ArrayList<String>()
        var index = 0
        while (index < scramble.length) {
            while (index < scramble.length && scramble[index].isWhitespace()) index++
            if (index == scramble.length) break

            val moveStart = index
            while (index < scramble.length && !scramble[index].isWhitespace()) index++
            val move = scramble.substring(moveStart, index)
            if (!move.isSupportedMove()) {
                throw InvalidScrambleException("Unsupported move: $move")
            }
            moves += move
        }

        if (moves.isEmpty()) {
            throw InvalidScrambleException("Scramble must contain at least one move.")
        }
        return moves
    }

    private fun String.isSupportedMove(): Boolean =
        length in 1..2 &&
            first() in SUPPORTED_FACES &&
            (length == 1 || this[1] == '2' || this[1] == '\'')

    private const val SUPPORTED_FACES = "RLUDFB"
}
