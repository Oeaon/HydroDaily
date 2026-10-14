package com.oeaon.hydrodaily.domain.usecase

import com.oeaon.hydrodaily.domain.TimeProvider
import com.oeaon.hydrodaily.domain.reminder.NextReminderCalculator
import com.oeaon.hydrodaily.domain.reminder.ReminderScheduler
import com.oeaon.hydrodaily.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SyncReminderUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val reminderScheduler: ReminderScheduler,
    private val calculator: NextReminderCalculator,
    private val timeProvider: TimeProvider
) {

    suspend operator fun invoke() {
        val settings = settingsRepository.reminderSettings.first()
        val next = calculator.nextTrigger(timeProvider.now(), settings)
        if (next == null) reminderScheduler.cancel() else reminderScheduler.scheduleAt(next)
    }
}
