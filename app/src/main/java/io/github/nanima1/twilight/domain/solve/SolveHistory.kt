package io.github.nanima1.twilight.domain.solve

enum class SolvePenalty(val id: String) {
    NONE("none"),
    PLUS_TWO("plus_two"),
    DNF("dnf");

    fun applyTo(durationMillis: Long): Long? {
        val normalizedDuration = durationMillis.coerceAtLeast(0L)
        return when (this) {
            NONE -> normalizedDuration
            PLUS_TWO -> normalizedDuration
                .coerceAtMost(Long.MAX_VALUE - PLUS_TWO_MILLIS) + PLUS_TWO_MILLIS
            DNF -> null
        }
    }

    companion object {
        private const val PLUS_TWO_MILLIS = 2_000L

        fun fromId(id: String?): SolvePenalty = entries.firstOrNull { it.id == id } ?: NONE
    }
}

data class SolveRecord(
    val id: Long,
    val durationMillis: Long,
    val scramble: String,
    val completedAtEpochMillis: Long,
    val penalty: SolvePenalty = SolvePenalty.NONE
) {
    val adjustedDurationMillis: Long?
        get() = penalty.applyTo(durationMillis)
}

data class SolveStats(
    val solveCount: Long = 0L,
    val lastSolveMillis: Long? = null,
    val lastSolvePenalty: SolvePenalty? = null,
    val bestSolveMillis: Long? = null
)

data class SolveHistory(
    val recentSolves: List<SolveRecord> = emptyList(),
    val stats: SolveStats = SolveStats()
)
