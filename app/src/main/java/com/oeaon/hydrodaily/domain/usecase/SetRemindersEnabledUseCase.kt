package com.oeaon.hydrodaily.domain.usecase

import com.oeaon.hydrodaily.domain.reminder.ReminderScheduler
import com.oeaon.hydrodaily.domain.repository.SettingsRepository
import javax.inject.Inject

class SetRemindersEnabledUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val reminderScheduler: ReminderScheduler
) {

    suspend operator fun invoke(enabled: Boolean) {
        settingsRepository.setRemindersEnabled(enabled)
        if (enabled) reminderScheduler.scheduleNext() else reminderScheduler.cancel()
    }
}
