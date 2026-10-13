package com.oeaon.hydrodaily.domain.reminder

interface ReminderScheduler {
    fun scheduleNext()
    fun cancel()
}
