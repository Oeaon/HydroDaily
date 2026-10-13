package com.oeaon.hydrodaily.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.oeaon.hydrodaily.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    override val remindersEnabled: Flow<Boolean> = dataStore.data
        .map { it[REMINDERS_ENABLED] ?: true }
        .distinctUntilChanged()

    override val notificationPermissionAsked: Flow<Boolean> = dataStore.data
        .map { it[PERMISSION_ASKED] ?: false }
        .distinctUntilChanged()

    override suspend fun setRemindersEnabled(enabled: Boolean) {
        dataStore.edit { it[REMINDERS_ENABLED] = enabled }
    }

    override suspend fun setNotificationPermissionAsked() {
        dataStore.edit { it[PERMISSION_ASKED] = true }
    }

    private companion object {
        val REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
        val PERMISSION_ASKED = booleanPreferencesKey("notification_permission_asked")
    }
}
