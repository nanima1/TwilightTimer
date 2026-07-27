package io.github.nanima1.twilight.data.solve

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.nanima1.twilight.domain.solve.SolvePenalty

@Entity(
    tableName = "solves",
    indices = [
        Index(value = ["completed_at_epoch_millis"]),
        Index(value = ["duration_millis"])
    ]
)
data class SolveEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "duration_millis")
    val durationMillis: Long,
    val scramble: String,
    @ColumnInfo(name = "completed_at_epoch_millis")
    val completedAtEpochMillis: Long,
    @ColumnInfo(name = "penalty", defaultValue = "'none'")
    val penaltyId: String = SolvePenalty.NONE.id,
    val note: String? = null
)

data class SolveStatsEntity(
    val solveCount: Long,
    val bestSolveMillis: Long?
)
