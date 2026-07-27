package io.github.nanima1.twilight.domain.solve

import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class SolveHistoryFilterTest {
    private val now = Instant.parse("2026-07-27T04:00:00Z").toEpochMilli()
    private val shanghai = ZoneId.of("Asia/Shanghai")

    @Test
    fun `all history has no effective lower boundary`() {
        assertEquals(Long.MIN_VALUE, SolveHistoryFilter.ALL.toQuery(now, shanghai).sinceEpochMillis)
    }

    @Test
    fun `today begins at local midnight`() {
        val expected = Instant.parse("2026-07-26T16:00:00Z").toEpochMilli()

        assertEquals(
            expected,
            SolveHistoryFilter.TODAY.toQuery(now, shanghai).sinceEpochMillis
        )
    }

    @Test
    fun `multi-day ranges include the current calendar day`() {
        val sevenDayStart = Instant.parse("2026-07-20T16:00:00Z").toEpochMilli()
        val thirtyDayStart = Instant.parse("2026-06-27T16:00:00Z").toEpochMilli()

        assertEquals(
            sevenDayStart,
            SolveHistoryFilter.LAST_7_DAYS.toQuery(now, shanghai).sinceEpochMillis
        )
        assertEquals(
            thirtyDayStart,
            SolveHistoryFilter.LAST_30_DAYS.toQuery(now, shanghai).sinceEpochMillis
        )
    }
}
