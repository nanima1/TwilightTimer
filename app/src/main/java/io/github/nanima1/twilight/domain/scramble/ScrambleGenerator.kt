package io.github.nanima1.twilight.domain.scramble

import kotlin.random.Random

class ScrambleGenerator(private val random: Random = Random.Default) {
    private data class Move(val notation: String, val axis: Axis)

    private enum class Axis {
        X,
        Y,
        Z
    }

    private val moves = listOf(
        Move("R", Axis.X), Move("L", Axis.X),
        Move("U", Axis.Y), Move("D", Axis.Y),
        Move("F", Axis.Z), Move("B", Axis.Z)
    )
    private val suffixes = listOf("", "'", "2")

    fun generate(moveCount: Int = DEFAULT_MOVE_COUNT): String {
        require(moveCount > 0) { "Move count must be positive." }

        val selected = ArrayList<String>(moveCount)
        var previousAxis: Axis? = null
        repeat(moveCount) {
            val available = moves.filter { it.axis != previousAxis }
            val move = available[random.nextInt(available.size)]
            selected += move.notation + suffixes[random.nextInt(suffixes.size)]
            previousAxis = move.axis
        }
        return selected.joinToString(separator = " ")
    }

    private companion object {
        const val DEFAULT_MOVE_COUNT = 20
    }
}
