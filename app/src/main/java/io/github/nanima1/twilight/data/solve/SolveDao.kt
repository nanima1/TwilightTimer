package io.github.nanima1.twilight.data.solve

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SolveDao {
    @Query(
        """
        SELECT * FROM solves
        WHERE completed_at_epoch_millis >= :sinceEpochMillis
        ORDER BY completed_at_epoch_millis DESC, id DESC
        LIMIT :limit
        """
    )
    fun observeRecent(sinceEpochMillis: Long, limit: Int): Flow<List<SolveEntity>>

    @Query(
        """
        SELECT
            COUNT(*) AS solveCount,
            MIN(
                CASE penalty
                    WHEN 'dnf' THEN NULL
                    WHEN 'plus_two' THEN duration_millis + 2000
                    ELSE duration_millis
                END
            ) AS bestSolveMillis
        FROM solves
        WHERE completed_at_epoch_millis >= :sinceEpochMillis
        """
    )
    fun observeStats(sinceEpochMillis: Long): Flow<SolveStatsEntity>

    @Insert
    suspend fun insert(solve: SolveEntity)

    @Query("UPDATE solves SET penalty = :penaltyId WHERE id = :id")
    suspend fun setPenalty(id: Long, penaltyId: String)

    @Query("UPDATE solves SET note = :note WHERE id = :id")
    suspend fun setNote(id: Long, note: String?)

    @Query("DELETE FROM solves WHERE id = :id")
    suspend fun deleteById(id: Long)
}
