package com.oeaon.hydrodaily.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.oeaon.hydrodaily.domain.model.ReminderFrequency
import com.oeaon.hydrodaily.domain.model.ReminderSettings
import com.oeaon.hydrodaily.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    override val reminderSettings: Flow<ReminderSettings> = dataStore.data
        .map { it.toReminderSettings() }
        .distinctUntilChanged()

    override val notificationPermissionAsked: Flow<Boolean> = dataStore.data
        .map { it[PERMISSION_ASKED] ?: false }
        .distinctUntilChanged()

    override val analyticsEnabled: Flow<Boolean> = dataStore.data
        .map { it[ANALYTICS_ENABLED] ?: true }
        .distinctUntilChanged()

    override val lastReviewPrompt: Flow<LocalDate?> = dataStore.data
        .map { prefs -> prefs[LAST_REVIEW_PROMPT]?.let { runCatching { LocalDate.parse(it) }.getOrNull() } }
        .distinctUntilChanged()

    override suspend fun saveReminderSettings(settings: ReminderSettings) {
        dataStore.edit { prefs ->
            prefs[REMINDERS_ENABLED] = settings.enabled
            prefs[REMINDER_START_MINUTE] = settings.startTime.minuteOfDay()
            prefs[REMINDER_END_MINUTE] = settings.endTime.minuteOfDay()
            prefs[REMINDER_FREQUENCY] = settings.frequency.name
            prefs[REMINDER_DAYS] = settings.days.map { it.value }.sorted().joinToString(SEPARATOR)
            prefs[REMINDER_SKIP_WHEN_REACHED] = settings.skipWhenGoalReached
        }
    }

    override suspend fun setNotificationPermissionAsked() {
        dataStore.edit { it[PERMISSION_ASKED] = true }
    }

    override suspend fun setAnalyticsEnabled(enabled: Boolean) {
        dataStore.edit { it[ANALYTICS_ENABLED] = enabled }
    }

    override suspend fun setLastReviewPrompt(date: LocalDate) {
        dataStore.edit { it[LAST_REVIEW_PROMPT] = date.toString() }
    }

    private fun Preferences.toReminderSettings(): ReminderSettings {
        val defaults = ReminderSettings()
        return ReminderSettings(
            enabled = this[REMINDERS_ENABLED] ?: defaults.enabled,
            startTime = this[REMINDER_START_MINUTE]?.toLocalTime() ?: defaults.startTime,
            endTime = this[REMINDER_END_MINUTE]?.toLocalTime() ?: defaults.endTime,
            frequency = this[REMINDER_FREQUENCY]
                ?.let { name -> ReminderFrequency.entries.firstOrNull { it.name == name } }
                ?: defaults.frequency,
            days = this[REMINDER_DAYS]?.toDays() ?: defaults.days,
            skipWhenGoalReached = this[REMINDER_SKIP_WHEN_REACHED] ?: defaults.skipWhenGoalReached
        )
    }

    private fun String.toDays(): Set<DayOfWeek> =
        split(SEPARATOR).mapNotNull { it.toIntOrNull()?.takeIf { day -> day in 1..7 }?.let(DayOfWeek::of) }.toSet()

    private fun LocalTime.minuteOfDay(): Int = hour * MINUTES_PER_HOUR + minute

    private fun Int.toLocalTime(): LocalTime? =
        takeIf { it in 0 until MINUTES_PER_DAY }?.let { LocalTime.of(it / MINUTES_PER_HOUR, it % MINUTES_PER_HOUR) }

    private companion object {
        val REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
        val REMINDER_START_MINUTE = intPreferencesKey("reminder_start_minute")
        val REMINDER_END_MINUTE = intPreferencesKey("reminder_end_minute")
        val REMINDER_FREQUENCY = stringPreferencesKey("reminder_frequency")
        val REMINDER_DAYS = stringPreferencesKey("reminder_days")
        val REMINDER_SKIP_WHEN_REACHED = booleanPreferencesKey("reminder_skip_when_goal_reached")
        val PERMISSION_ASKED = booleanPreferencesKey("notification_permission_asked")
        val ANALYTICS_ENABLED = booleanPreferencesKey("analytics_enabled")
        val LAST_REVIEW_PROMPT = stringPreferencesKey("last_review_prompt")
        const val SEPARATOR = ","
        const val MINUTES_PER_HOUR = 60
        const val MINUTES_PER_DAY = 24 * 60
    }
}
