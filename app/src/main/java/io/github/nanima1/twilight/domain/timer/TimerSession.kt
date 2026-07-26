package io.github.nanima1.twilight.domain.timer

import io.github.nanima1.twilight.domain.solve.SolvePenalty

enum class TimerPhase {
    READY,
    INSPECTING,
    RUNNING
}

data class TimerSession(
    val phase: TimerPhase = TimerPhase.READY,
    val startedAtMillis: Long? = null,
    val elapsedMillis: Long = 0L,
    val inspectionElapsedMillis: Long = 0L,
    val penalty: SolvePenalty = SolvePenalty.NONE
)

object InspectionRules {
    const val LIMIT_MILLIS = 15_000L
    const val DNF_MILLIS = 17_000L

    fun penaltyFor(elapsedMillis: Long): SolvePenalty = when {
        elapsedMillis.coerceAtLeast(0L) <= LIMIT_MILLIS -> SolvePenalty.NONE
        elapsedMillis <= DNF_MILLIS -> SolvePenalty.PLUS_TWO
        else -> SolvePenalty.DNF
    }
}

object TimerSessionReducer {
    fun startDirect(session: TimerSession, nowMillis: Long): TimerSession {
        check(session.phase == TimerPhase.READY) {
            "A direct timer start can only begin from the ready state."
        }
        return session.copy(
            phase = TimerPhase.RUNNING,
            startedAtMillis = nowMillis,
            elapsedMillis = 0L,
            inspectionElapsedMillis = 0L,
            penalty = SolvePenalty.NONE
        )
    }

    fun beginInspection(session: TimerSession, nowMillis: Long): TimerSession {
        check(session.phase == TimerPhase.READY) {
            "Inspection can only begin from the ready state."
        }
        return session.copy(
            phase = TimerPhase.INSPECTING,
            startedAtMillis = nowMillis,
            elapsedMillis = 0L,
            inspectionElapsedMillis = 0L,
            penalty = SolvePenalty.NONE
        )
    }

    fun start(session: TimerSession, nowMillis: Long): TimerSession {
        check(session.phase == TimerPhase.INSPECTING) {
            "A timer can only start from the inspection state."
        }
        val inspected = tick(session, nowMillis)
        return inspected.copy(
            phase = TimerPhase.RUNNING,
            startedAtMillis = nowMillis,
            elapsedMillis = 0L,
            penalty = InspectionRules.penaltyFor(inspected.inspectionElapsedMillis)
        )
    }

    fun tick(session: TimerSession, nowMillis: Long): TimerSession {
        val startedAtMillis = session.startedAtMillis ?: return session
        val phaseElapsedMillis = (nowMillis - startedAtMillis).coerceAtLeast(0L)
        return when (session.phase) {
            TimerPhase.READY -> session
            TimerPhase.INSPECTING -> session.copy(inspectionElapsedMillis = phaseElapsedMillis)
            TimerPhase.RUNNING -> session.copy(elapsedMillis = phaseElapsedMillis)
        }
    }

    fun stop(session: TimerSession, nowMillis: Long): TimerSession {
        val completed = tick(session, nowMillis)
        check(completed.phase == TimerPhase.RUNNING) { "A timer can only stop while running." }
        return completed.copy(
            phase = TimerPhase.READY,
            startedAtMillis = null
        )
    }
}
