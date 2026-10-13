package com.oeaon.hydrodaily.domain.reminder

import java.time.LocalTime
import java.time.ZonedDateTime
import javax.inject.Inject

class NextReminderCalculator @Inject constructor() {

    fun nextTrigger(now: ZonedDateTime, time: LocalTime = REMINDER_TIME): ZonedDateTime {
        val today = now.toLocalDate().atTime(time).atZone(now.zone)
        return if (today.isAfter(now)) today else now.toLocalDate().plusDays(1).atTime(time).atZone(now.zone)
    }

    companion object {
        val REMINDER_TIME: LocalTime = LocalTime.NOON
    }
}
