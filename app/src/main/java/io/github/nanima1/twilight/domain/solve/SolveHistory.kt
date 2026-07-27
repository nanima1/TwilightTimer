package io.github.nanima1.twilight.domain.solve

import java.time.Instant
import java.time.ZoneId

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

data class SolveHistoryQuery(
    val sinceEpochMillis: Long = Long.MIN_VALUE
)

enum class SolveHistoryFilter {
    ALL,
    TODAY,
    LAST_7_DAYS,
    LAST_30_DAYS;

    fun toQuery(
        nowEpochMillis: Long,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): SolveHistoryQuery {
        if (this == ALL) return SolveHistoryQuery()

        val daysBeforeToday = when (this) {
            ALL -> error("All history does not have a calendar boundary")
            TODAY -> 0L
            LAST_7_DAYS -> 6L
            LAST_30_DAYS -> 29L
        }
        val sinceEpochMillis = Instant
            .ofEpochMilli(nowEpochMillis)
            .atZone(zoneId)
            .toLocalDate()
            .minusDays(daysBeforeToday)
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()
        return SolveHistoryQuery(sinceEpochMillis)
    }
}

data class SolveRecord(
    val id: Long,
    val durationMillis: Long,
    val scramble: String,
    val completedAtEpochMillis: Long,
    val penalty: SolvePenalty = SolvePenalty.NONE,
    val note: String? = null
) {
    val adjustedDurationMillis: Long?
        get() = penalty.applyTo(durationMillis)

    companion object {
        const val MAX_NOTE_LENGTH = 240

        fun normalizeNote(value: String?): String? = value
            ?.trim()
            ?.take(MAX_NOTE_LENGTH)
            ?.takeIf(String::isNotEmpty)
    }
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
