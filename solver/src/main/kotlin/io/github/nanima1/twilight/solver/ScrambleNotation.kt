package io.github.nanima1.twilight.solver

internal object ScrambleNotation {
    fun parse(scramble: String): List<String> {
        val moves = ArrayList<String>()
        forEachMove(scramble) { start, end ->
            moves += scramble.substring(start, end)
        }
        return moves
    }

    fun parseEncoded(scramble: String): IntArray {
        var moveCount = 0
        forEachMove(scramble) { _, _ -> moveCount++ }

        val moves = IntArray(moveCount)
        var moveIndex = 0
        forEachMove(scramble) { start, end ->
            moves[moveIndex++] = scramble.encodedMove(start, end)
        }
        return moves
    }

    private inline fun forEachMove(
        scramble: String,
        action: (start: Int, end: Int) -> Unit
    ) {
        var index = 0
        var moveCount = 0
        while (index < scramble.length) {
            while (index < scramble.length && scramble[index].isWhitespace()) index++
            if (index == scramble.length) break

            val moveStart = index
            while (index < scramble.length && !scramble[index].isWhitespace()) index++
            if (!scramble.isSupportedMove(moveStart, index)) {
                throw InvalidScrambleException(
                    "Unsupported move: ${scramble.substring(moveStart, index)}"
                )
            }
            action(moveStart, index)
            moveCount++
        }

        if (moveCount == 0) {
            throw InvalidScrambleException("Scramble must contain at least one move.")
        }
    }

    private fun String.isSupportedMove(start: Int, end: Int): Boolean {
        val length = end - start
        return length in 1..2 &&
            this[start] in SUPPORTED_FACES &&
            (length == 1 || this[start + 1] == '2' || this[start + 1] == '\'')
    }

    private fun String.encodedMove(start: Int, end: Int): Int {
        val face = when (this[start]) {
            'U' -> 0
            'R' -> 3
            'F' -> 6
            'D' -> 9
            'L' -> 12
            'B' -> 15
            else -> error("Move was encoded before validation.")
        }
        val suffix = when {
            end - start == 1 -> 0
            this[start + 1] == '2' -> 1
            else -> 2
        }
        return face + suffix
    }

    private const val SUPPORTED_FACES = "RLUDFB"
}
