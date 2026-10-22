package com.oeaon.hydrodaily.domain.usecase

import com.oeaon.hydrodaily.domain.fakes.FakeAnalyticsTracker
import com.oeaon.hydrodaily.domain.fakes.FakeGoalHistoryRepository
import com.oeaon.hydrodaily.domain.fakes.FakeIntakeRepository
import com.oeaon.hydrodaily.domain.fakes.FakeProfileRepository
import com.oeaon.hydrodaily.domain.fakes.FakeTimeProvider
import com.oeaon.hydrodaily.domain.model.ActivityLevel
import com.oeaon.hydrodaily.domain.model.Gender
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveProfileUseCaseTest {

    private val repository = FakeProfileRepository()
    private val analytics = FakeAnalyticsTracker()
    private val saveProfile = SaveProfileUseCase(
        repository,
        CalculateDailyGoalUseCase(),
        UpdateGoalHistoryUseCase(repository, FakeIntakeRepository(), FakeGoalHistoryRepository(), FakeTimeProvider()),
        analytics
    )

    @Test
    fun `valid input saves profile with calculated goal`() = runTest {
        val result = saveProfile(70.0, Gender.MALE, ActivityLevel.MODERATE)

        assertTrue(result is SaveProfileUseCase.Result.Saved)
        assertEquals(3450, repository.state.value?.dailyGoalMl)
        assertEquals(listOf("profile_saved"), analytics.events.map { it.name })
    }

    @Test
    fun `missing or out of range weight is rejected`() = runTest {
        assertEquals(SaveProfileUseCase.Result.InvalidWeight, saveProfile(null, Gender.MALE, ActivityLevel.LOW))
        assertEquals(SaveProfileUseCase.Result.InvalidWeight, saveProfile(5.0, Gender.MALE, ActivityLevel.LOW))
        assertNull(repository.state.value)
    }

    @Test
    fun `missing selections are rejected`() = runTest {
        assertEquals(SaveProfileUseCase.Result.MissingGender, saveProfile(70.0, null, ActivityLevel.LOW))
        assertEquals(SaveProfileUseCase.Result.MissingActivityLevel, saveProfile(70.0, Gender.FEMALE, null))
    }
}
