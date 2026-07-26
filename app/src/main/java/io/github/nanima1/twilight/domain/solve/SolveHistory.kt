package io.github.nanima1.twilight.domain.solve

data class SolveRecord(
    val id: Long,
    val durationMillis: Long,
    val scramble: String,
    val completedAtEpochMillis: Long
)

data class SolveStats(
    val solveCount: Long = 0L,
    val lastSolveMillis: Long? = null,
    val bestSolveMillis: Long? = null
)

data class SolveHistory(
    val recentSolves: List<SolveRecord> = emptyList(),
    val stats: SolveStats = SolveStats()
)
