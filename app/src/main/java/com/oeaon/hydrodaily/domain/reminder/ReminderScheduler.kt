package com.oeaon.hydrodaily.domain.reminder

import java.time.ZonedDateTime

interface ReminderScheduler {
    fun scheduleAt(trigger: ZonedDateTime)
    fun cancel()
}
