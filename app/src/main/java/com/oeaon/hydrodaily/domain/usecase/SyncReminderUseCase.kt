package com.oeaon.hydrodaily.domain.usecase

import com.oeaon.hydrodaily.domain.reminder.ReminderScheduler
import com.oeaon.hydrodaily.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SyncReminderUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val reminderScheduler: ReminderScheduler
) {

    suspend operator fun invoke() {
        if (settingsRepository.remindersEnabled.first()) {
            reminderScheduler.scheduleNext()
        } else {
            reminderScheduler.cancel()
        }
    }
}
