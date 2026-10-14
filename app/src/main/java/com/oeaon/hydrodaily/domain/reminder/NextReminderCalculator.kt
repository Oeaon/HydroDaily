package com.oeaon.hydrodaily.domain.reminder

import com.oeaon.hydrodaily.domain.model.ReminderSettings
import java.time.ZonedDateTime
import javax.inject.Inject

class NextReminderCalculator @Inject constructor() {

    fun nextTrigger(now: ZonedDateTime, settings: ReminderSettings): ZonedDateTime? {
        if (!settings.enabled || settings.days.isEmpty()) return null
        val times = settings.timesOfDay()
        return (0L..DAYS_TO_SEARCH).asSequence()
            .map { now.toLocalDate().plusDays(it) }
            .filter { it.dayOfWeek in settings.days }
            .flatMap { date -> times.asSequence().map { date.atTime(it).atZone(now.zone) } }
            .firstOrNull { it.isAfter(now) }
    }

    private companion object {
        const val DAYS_TO_SEARCH = 7L
    }
}
