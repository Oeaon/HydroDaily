package com.oeaon.hydrodaily.domain.repository

import com.oeaon.hydrodaily.domain.model.ReminderSettings
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface SettingsRepository {
    val reminderSettings: Flow<ReminderSettings>
    val notificationPermissionAsked: Flow<Boolean>
    val analyticsEnabled: Flow<Boolean>
    val lastReviewPrompt: Flow<LocalDate?>
    suspend fun saveReminderSettings(settings: ReminderSettings)
    suspend fun setNotificationPermissionAsked()
    suspend fun setAnalyticsEnabled(enabled: Boolean)
    suspend fun setLastReviewPrompt(date: LocalDate)
}
