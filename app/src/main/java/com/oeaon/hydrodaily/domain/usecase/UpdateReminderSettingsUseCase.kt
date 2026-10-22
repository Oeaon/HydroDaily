package com.oeaon.hydrodaily.domain.usecase

import com.oeaon.hydrodaily.domain.analytics.AnalyticsEvent
import com.oeaon.hydrodaily.domain.analytics.AnalyticsTracker
import com.oeaon.hydrodaily.domain.model.ReminderSettings
import com.oeaon.hydrodaily.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UpdateReminderSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val syncReminder: SyncReminderUseCase,
    private val analytics: AnalyticsTracker
) {

    suspend operator fun invoke(transform: (ReminderSettings) -> ReminderSettings) {
        val current = settingsRepository.reminderSettings.first()
        val updated = transform(current).normalized()
        if (updated == current) return
        settingsRepository.saveReminderSettings(updated)
        syncReminder()
        analytics.track(
            AnalyticsEvent.RemindersUpdated(
                enabled = updated.enabled,
                remindersPerDay = updated.timesOfDay().size,
                activeDays = updated.days.size
            )
        )
    }

    private fun ReminderSettings.normalized(): ReminderSettings =
        if (endTime.isBefore(startTime)) copy(endTime = startTime) else this
}
