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
        ORDER BY completed_at_epoch_millis DESC, id DESC
        LIMIT :limit
        """
    )
    fun observeRecent(limit: Int): Flow<List<SolveEntity>>

    @Query(
        """
        SELECT COUNT(*) AS solveCount, MIN(duration_millis) AS bestSolveMillis
        FROM solves
        """
    )
    fun observeStats(): Flow<SolveStatsEntity>

    @Insert
    suspend fun insert(solve: SolveEntity)

    @Query("DELETE FROM solves WHERE id = :id")
    suspend fun deleteById(id: Long)
}
