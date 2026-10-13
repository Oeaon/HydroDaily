package com.oeaon.hydrodaily.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val remindersEnabled: Flow<Boolean>
    val notificationPermissionAsked: Flow<Boolean>
    suspend fun setRemindersEnabled(enabled: Boolean)
    suspend fun setNotificationPermissionAsked()
}
