package com.oeaon.hydrodaily.domain.usecase

import com.oeaon.hydrodaily.domain.model.ActivityLevel
import com.oeaon.hydrodaily.domain.model.Gender
import com.oeaon.hydrodaily.domain.model.UserProfile
import com.oeaon.hydrodaily.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveProfileUseCaseTest {

    private val repository = FakeProfileRepository()
    private val saveProfile = SaveProfileUseCase(repository, CalculateDailyGoalUseCase())

    @Test
    fun `valid input saves profile with calculated goal`() = runTest {
        val result = saveProfile(70.0, Gender.MALE, ActivityLevel.MODERATE)

        assertTrue(result is SaveProfileUseCase.Result.Saved)
        assertEquals(3450, repository.state.value?.dailyGoalMl)
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

    private class FakeProfileRepository : ProfileRepository {
        val state = MutableStateFlow<UserProfile?>(null)
        override val profile: Flow<UserProfile?> = state
        override suspend fun save(profile: UserProfile) {
            state.value = profile
        }
    }
}
