package com.oeaon.hydrodaily.domain.usecase

import com.oeaon.hydrodaily.domain.model.ActivityLevel
import com.oeaon.hydrodaily.domain.model.Gender
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateDailyGoalUseCaseTest {

    private val calculate = CalculateDailyGoalUseCase()

    @Test
    fun `female with low activity gets base intake`() {
        assertEquals(2100, calculate(60.0, Gender.FEMALE, ActivityLevel.LOW))
    }

    @Test
    fun `male with high activity adds both bonuses`() {
        assertEquals(3950, calculate(70.0, Gender.MALE, ActivityLevel.HIGH))
    }

    @Test
    fun `moderate activity adds half a liter`() {
        assertEquals(2600, calculate(60.0, Gender.FEMALE, ActivityLevel.MODERATE))
    }

    @Test
    fun `result is rounded to nearest 50 ml`() {
        assertEquals(2550, calculate(73.0, Gender.FEMALE, ActivityLevel.LOW))
    }
}
