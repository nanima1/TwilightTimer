package io.github.nanima1.twilight.domain.timer

enum class TimerPhase {
    READY,
    RUNNING
}

data class TimerSession(
    val phase: TimerPhase = TimerPhase.READY,
    val startedAtMillis: Long? = null,
    val elapsedMillis: Long = 0L
)

object TimerSessionReducer {
    fun start(session: TimerSession, nowMillis: Long): TimerSession {
        check(session.phase == TimerPhase.READY) { "A timer can only start from the ready state." }
        return session.copy(phase = TimerPhase.RUNNING, startedAtMillis = nowMillis, elapsedMillis = 0L)
    }

    fun tick(session: TimerSession, nowMillis: Long): TimerSession {
        val startedAtMillis = session.startedAtMillis ?: return session
        return session.copy(elapsedMillis = (nowMillis - startedAtMillis).coerceAtLeast(0L))
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
