package io.github.nanima1.twilight.domain.solve

import kotlinx.coroutines.flow.Flow

interface SolveRepository {
    val history: Flow<SolveHistory>

    suspend fun addSolve(
        durationMillis: Long,
        scramble: String,
        completedAtEpochMillis: Long,
        penalty: SolvePenalty = SolvePenalty.NONE
    )

    suspend fun setPenalty(id: Long, penalty: SolvePenalty)

    suspend fun setNote(id: Long, note: String?)

    suspend fun deleteSolve(id: Long)
}
