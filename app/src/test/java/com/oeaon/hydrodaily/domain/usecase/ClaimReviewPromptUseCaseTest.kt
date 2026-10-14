package com.oeaon.hydrodaily.domain.usecase

import com.oeaon.hydrodaily.domain.fakes.FakeAnalyticsTracker
import com.oeaon.hydrodaily.domain.fakes.FakeSettingsRepository
import com.oeaon.hydrodaily.domain.fakes.FakeTimeProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClaimReviewPromptUseCaseTest {

    private val time = FakeTimeProvider()
    private val settings = FakeSettingsRepository()
    private val claim = ClaimReviewPromptUseCase(settings, time, FakeAnalyticsTracker())

    @Test
    fun `short streaks never prompt`() = runTest {
        assertFalse(claim(2))
    }

    @Test
    fun `first eligible streak prompts and records the date`() = runTest {
        assertTrue(claim(3))
        assertEquals(time.today(), settings.lastReviewState.value)
    }

    @Test
    fun `waits sixty days between prompts`() = runTest {
        settings.lastReviewState.value = time.today().minusDays(59)
        assertFalse(claim(10))
        settings.lastReviewState.value = time.today().minusDays(60)
        assertTrue(claim(10))
    }
}
