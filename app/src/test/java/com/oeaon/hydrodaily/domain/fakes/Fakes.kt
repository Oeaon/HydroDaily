package com.oeaon.hydrodaily.domain.fakes

import com.oeaon.hydrodaily.domain.TimeProvider
import com.oeaon.hydrodaily.domain.analytics.AnalyticsEvent
import com.oeaon.hydrodaily.domain.analytics.AnalyticsTracker
import com.oeaon.hydrodaily.domain.model.DailyIntake
import com.oeaon.hydrodaily.domain.model.ReminderSettings
import com.oeaon.hydrodaily.domain.model.UserProfile
import com.oeaon.hydrodaily.domain.reminder.ReminderScheduler
import com.oeaon.hydrodaily.domain.repository.GoalHistoryRepository
import com.oeaon.hydrodaily.domain.repository.IntakeRepository
import com.oeaon.hydrodaily.domain.repository.ProfileRepository
import com.oeaon.hydrodaily.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class FakeTimeProvider(var current: ZonedDateTime = ZonedDateTime.of(2026, 10, 14, 10, 0, 0, 0, ZoneId.of("Europe/Madrid"))) :
    TimeProvider {
    override fun now(): ZonedDateTime = current
}

class FakeProfileRepository(initial: UserProfile? = null) : ProfileRepository {
    val state = MutableStateFlow(initial)
    override val profile: Flow<UserProfile?> = state
    override suspend fun save(profile: UserProfile) {
        state.value = profile
    }
}

class FakeIntakeRepository : IntakeRepository {
    private val entries = MutableStateFlow<Map<LocalDate, List<Int>>>(emptyMap())
    override fun observe(date: LocalDate): Flow<DailyIntake> =
        entries.map { DailyIntake(date, it[date].orEmpty()) }

    override suspend fun add(date: LocalDate, amountMl: Int) {
        entries.value = entries.value + (date to entries.value[date].orEmpty() + amountMl)
    }

    override suspend fun removeLast(date: LocalDate) {
        entries.value = entries.value + (date to entries.value[date].orEmpty().dropLast(1))
    }
}

class FakeGoalHistoryRepository(initial: Set<LocalDate> = emptySet()) : GoalHistoryRepository {
    val state = MutableStateFlow(initial)
    override val goalDays: Flow<Set<LocalDate>> = state
    override suspend fun setGoalReached(date: LocalDate, reached: Boolean) {
        state.value = if (reached) state.value + date else state.value - date
    }
}

class FakeSettingsRepository(reminders: ReminderSettings = ReminderSettings()) : SettingsRepository {
    val reminderState = MutableStateFlow(reminders)
    val lastReviewState = MutableStateFlow<LocalDate?>(null)
    private val permissionAsked = MutableStateFlow(false)
    private val analytics = MutableStateFlow(true)

    override val reminderSettings: Flow<ReminderSettings> = reminderState
    override val notificationPermissionAsked: Flow<Boolean> = permissionAsked
    override val analyticsEnabled: Flow<Boolean> = analytics
    override val lastReviewPrompt: Flow<LocalDate?> = lastReviewState

    override suspend fun saveReminderSettings(settings: ReminderSettings) {
        reminderState.value = settings
    }

    override suspend fun setNotificationPermissionAsked() {
        permissionAsked.value = true
    }

    override suspend fun setAnalyticsEnabled(enabled: Boolean) {
        analytics.value = enabled
    }

    override suspend fun setLastReviewPrompt(date: LocalDate) {
        lastReviewState.value = date
    }
}

class FakeAnalyticsTracker : AnalyticsTracker {
    val events = mutableListOf<AnalyticsEvent>()
    var collectionState = true
    override fun track(event: AnalyticsEvent) {
        events += event
    }

    override fun setCollectionEnabled(enabled: Boolean) {
        collectionState = enabled
    }
}

class FakeReminderScheduler : ReminderScheduler {
    var scheduled: ZonedDateTime? = null
    override fun scheduleAt(trigger: ZonedDateTime) {
        scheduled = trigger
    }

    override fun cancel() {
        scheduled = null
    }
}
