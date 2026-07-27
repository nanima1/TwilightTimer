package io.github.nanima1.twilight.data.solve

import io.github.nanima1.twilight.domain.solve.SolveHistory
import io.github.nanima1.twilight.domain.solve.SolveHistoryQuery
import io.github.nanima1.twilight.domain.solve.SolvePenalty
import io.github.nanima1.twilight.domain.solve.SolveRecord
import io.github.nanima1.twilight.domain.solve.SolveRepository
import io.github.nanima1.twilight.domain.solve.SolveStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class RoomSolveRepository(
    private val solveDao: SolveDao,
    private val historyLimit: Int = DEFAULT_HISTORY_LIMIT
) : SolveRepository {
    override fun observeHistory(query: SolveHistoryQuery): Flow<SolveHistory> = combine(
        solveDao.observeRecent(query.sinceEpochMillis, historyLimit),
        solveDao.observeStats(query.sinceEpochMillis)
    ) { solves, stats ->
        val recentSolves = solves.map { it.toDomain() }
        SolveHistory(
            recentSolves = recentSolves,
            stats = SolveStats(
                solveCount = stats.solveCount,
                lastSolveMillis = recentSolves.firstOrNull()?.durationMillis,
                lastSolvePenalty = recentSolves.firstOrNull()?.penalty,
                bestSolveMillis = stats.bestSolveMillis
            )
        )
    }

    override suspend fun setPenalty(id: Long, penalty: SolvePenalty) {
        solveDao.setPenalty(id, penalty.id)
    }

    override suspend fun setNote(id: Long, note: String?) {
        solveDao.setNote(id, SolveRecord.normalizeNote(note))
    }

    override suspend fun addSolve(
        durationMillis: Long,
        scramble: String,
        completedAtEpochMillis: Long,
        penalty: SolvePenalty
    ) {
        solveDao.insert(
            SolveEntity(
                durationMillis = durationMillis.coerceAtLeast(0L),
                scramble = scramble,
                completedAtEpochMillis = completedAtEpochMillis,
                penaltyId = penalty.id
            )
        )
    }

    override suspend fun deleteSolve(id: Long) {
        solveDao.deleteById(id)
    }

    private fun SolveEntity.toDomain(): SolveRecord = SolveRecord(
        id = id,
        durationMillis = durationMillis,
        scramble = scramble,
        completedAtEpochMillis = completedAtEpochMillis,
        penalty = SolvePenalty.fromId(penaltyId),
        note = SolveRecord.normalizeNote(note)
    )

    private companion object {
        const val DEFAULT_HISTORY_LIMIT = 100
    }
}
