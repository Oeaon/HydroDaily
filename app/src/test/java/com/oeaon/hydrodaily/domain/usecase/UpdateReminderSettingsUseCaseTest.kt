package com.oeaon.hydrodaily.domain.usecase

import com.oeaon.hydrodaily.domain.fakes.FakeAnalyticsTracker
import com.oeaon.hydrodaily.domain.fakes.FakeReminderScheduler
import com.oeaon.hydrodaily.domain.fakes.FakeSettingsRepository
import com.oeaon.hydrodaily.domain.fakes.FakeTimeProvider
import com.oeaon.hydrodaily.domain.reminder.NextReminderCalculator
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalTime

class UpdateReminderSettingsUseCaseTest {

    private val time = FakeTimeProvider()
    private val settings = FakeSettingsRepository()
    private val scheduler = FakeReminderScheduler()
    private val update = UpdateReminderSettingsUseCase(
        settings,
        SyncReminderUseCase(settings, scheduler, NextReminderCalculator(), time),
        FakeAnalyticsTracker()
    )

    @Test
    fun `changing the time reschedules the reminder`() = runTest {
        update { it.copy(startTime = LocalTime.of(11, 15)) }
        assertEquals(LocalTime.of(11, 15), scheduler.scheduled?.toLocalTime())
    }

    @Test
    fun `turning reminders off cancels the alarm`() = runTest {
        update { it.copy(startTime = LocalTime.of(11, 15)) }
        update { it.copy(enabled = false) }
        assertNull(scheduler.scheduled)
    }

    @Test
    fun `end time before start time is clamped`() = runTest {
        update { it.copy(startTime = LocalTime.of(18, 0), endTime = LocalTime.of(9, 0)) }
        assertEquals(LocalTime.of(18, 0), settings.reminderState.value.endTime)
    }
}
