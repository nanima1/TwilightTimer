package io.github.nanima1.twilight.domain.timer

data class TimerSettings(
    val inspectionEnabled: Boolean = true,
    val inspectionHapticsEnabled: Boolean = true
)

enum class InspectionCue(val thresholdMillis: Long) {
    EIGHT_SECONDS(8_000L),
    TWELVE_SECONDS(12_000L)
}
