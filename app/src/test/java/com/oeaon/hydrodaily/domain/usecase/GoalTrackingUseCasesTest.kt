package com.oeaon.hydrodaily.domain.usecase

import com.oeaon.hydrodaily.domain.analytics.AnalyticsEvent
import com.oeaon.hydrodaily.domain.fakes.FakeAnalyticsTracker
import com.oeaon.hydrodaily.domain.fakes.FakeGoalHistoryRepository
import com.oeaon.hydrodaily.domain.fakes.FakeIntakeRepository
import com.oeaon.hydrodaily.domain.fakes.FakeProfileRepository
import com.oeaon.hydrodaily.domain.fakes.FakeTimeProvider
import com.oeaon.hydrodaily.domain.model.ActivityLevel
import com.oeaon.hydrodaily.domain.model.Gender
import com.oeaon.hydrodaily.domain.model.UserProfile
import com.oeaon.hydrodaily.domain.streak.StreakCalculator
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GoalTrackingUseCasesTest {

    private val time = FakeTimeProvider()
    private val today = time.today()
    private val profiles = FakeProfileRepository(UserProfile(60.0, Gender.FEMALE, ActivityLevel.LOW, 1000))
    private val intake = FakeIntakeRepository()
    private val history = FakeGoalHistoryRepository(setOf(today.minusDays(1), today.minusDays(2)))
    private val analytics = FakeAnalyticsTracker()
    private val updateGoalHistory = UpdateGoalHistoryUseCase(profiles, intake, history, time)
    private val observeStreak = ObserveStreakUseCase(history, ObserveCurrentDateUseCase(time), StreakCalculator())
    private val addWater = AddWaterUseCase(intake, time, updateGoalHistory, observeStreak, analytics)
    private val undo = UndoLastIntakeUseCase(intake, time, updateGoalHistory, analytics)

    @Test
    fun `reaching the goal records the day once and reports the streak`() = runTest {
        assertFalse(addWater(500).goalJustReached)

        val reached = addWater(500)
        assertTrue(reached.goalJustReached)
        assertEquals(3, reached.streakDays)
        assertTrue(today in history.state.value)

        assertFalse(addWater(250).goalJustReached)
        assertEquals(1, analytics.events.count { it is AnalyticsEvent.GoalReached })
    }

    @Test
    fun `undoing below the goal removes the day`() = runTest {
        addWater(1000)
        undo()
        assertFalse(today in history.state.value)
    }
}
