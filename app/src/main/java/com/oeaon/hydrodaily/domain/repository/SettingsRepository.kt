package com.oeaon.hydrodaily.domain.repository

import com.oeaon.hydrodaily.domain.model.ReminderSettings
import com.oeaon.hydrodaily.domain.premium.PremiumPlan
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

interface SettingsRepository {
    val reminderSettings: Flow<ReminderSettings>
    val notificationPermissionAsked: Flow<Boolean>
    val analyticsEnabled: Flow<Boolean>
    val lastReviewPrompt: Flow<LocalDate?>
    val premiumPlan: Flow<PremiumPlan?>
    val isPremium: Flow<Boolean> get() = premiumPlan.map { it != null }
    suspend fun saveReminderSettings(settings: ReminderSettings)
    suspend fun setNotificationPermissionAsked()
    suspend fun setAnalyticsEnabled(enabled: Boolean)
    suspend fun setLastReviewPrompt(date: LocalDate)
    suspend fun setPremiumPlan(plan: PremiumPlan?)
}
