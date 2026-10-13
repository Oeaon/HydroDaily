package com.oeaon.hydrodaily.domain.usecase

import com.oeaon.hydrodaily.domain.model.ActivityLevel
import com.oeaon.hydrodaily.domain.model.Gender
import javax.inject.Inject
import kotlin.math.roundToInt

class CalculateDailyGoalUseCase @Inject constructor() {

    operator fun invoke(weightKg: Double, gender: Gender, activityLevel: ActivityLevel): Int {
        val totalMl = weightKg * ML_PER_KG + genderBonus(gender) + activityBonus(activityLevel)
        return (totalMl / ROUNDING_STEP_ML).roundToInt() * ROUNDING_STEP_ML
    }

    private fun genderBonus(gender: Gender): Int = when (gender) {
        Gender.FEMALE -> 0
        Gender.MALE -> 500
    }

    private fun activityBonus(activityLevel: ActivityLevel): Int = when (activityLevel) {
        ActivityLevel.LOW -> 0
        ActivityLevel.MODERATE -> 500
        ActivityLevel.HIGH -> 1000
    }

    private companion object {
        const val ML_PER_KG = 35.0
        const val ROUNDING_STEP_ML = 50
    }
}
