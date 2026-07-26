package io.github.nanima1.twilight.data.solve

import io.github.nanima1.twilight.domain.solve.SolveHistory
import io.github.nanima1.twilight.domain.solve.SolveRecord
import io.github.nanima1.twilight.domain.solve.SolveRepository
import io.github.nanima1.twilight.domain.solve.SolveStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class RoomSolveRepository(
    private val solveDao: SolveDao,
    historyLimit: Int = DEFAULT_HISTORY_LIMIT
) : SolveRepository {
    override val history: Flow<SolveHistory> = combine(
        solveDao.observeRecent(historyLimit),
        solveDao.observeStats()
    ) { solves, stats ->
        val recentSolves = solves.map { it.toDomain() }
        SolveHistory(
            recentSolves = recentSolves,
            stats = SolveStats(
                solveCount = stats.solveCount,
                lastSolveMillis = recentSolves.firstOrNull()?.durationMillis,
                bestSolveMillis = stats.bestSolveMillis
            )
        )
    }

    override suspend fun addSolve(
        durationMillis: Long,
        scramble: String,
        completedAtEpochMillis: Long
    ) {
        solveDao.insert(
            SolveEntity(
                durationMillis = durationMillis.coerceAtLeast(0L),
                scramble = scramble,
                completedAtEpochMillis = completedAtEpochMillis
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
        completedAtEpochMillis = completedAtEpochMillis
    )

    private companion object {
        const val DEFAULT_HISTORY_LIMIT = 100
    }
}
