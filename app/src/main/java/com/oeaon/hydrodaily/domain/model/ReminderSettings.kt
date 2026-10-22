package com.oeaon.hydrodaily.domain.model

import java.time.DayOfWeek
import java.time.LocalTime

data class ReminderSettings(
    val enabled: Boolean = true,
    val startTime: LocalTime = LocalTime.NOON,
    val endTime: LocalTime = DEFAULT_END_TIME,
    val frequency: ReminderFrequency = ReminderFrequency.ONCE,
    val days: Set<DayOfWeek> = DayOfWeek.entries.toSet(),
    val skipWhenGoalReached: Boolean = false
) {

    fun timesOfDay(): List<LocalTime> {
        val intervalMinutes = frequency.intervalMinutes ?: return listOf(startTime)
        val lastMinute = endTime.toSecondOfDay() / SECONDS_PER_MINUTE
        return generateSequence(startTime.toSecondOfDay() / SECONDS_PER_MINUTE) { it + intervalMinutes }
            .takeWhile { it <= lastMinute }
            .map { LocalTime.ofSecondOfDay(it * SECONDS_PER_MINUTE.toLong()) }
            .toList()
            .ifEmpty { listOf(startTime) }
    }

    companion object {
        val DEFAULT_END_TIME: LocalTime = LocalTime.of(21, 0)
        private const val SECONDS_PER_MINUTE = 60
    }
}

enum class ReminderFrequency(val intervalMinutes: Int?) {
    ONCE(null),
    EVERY_HOUR(60),
    EVERY_90_MINUTES(90),
    EVERY_2_HOURS(120),
    EVERY_3_HOURS(180),
    EVERY_4_HOURS(240)
}
