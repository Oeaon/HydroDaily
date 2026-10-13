package com.oeaon.hydrodaily.domain.usecase

import com.oeaon.hydrodaily.domain.model.ActivityLevel
import com.oeaon.hydrodaily.domain.model.Gender
import com.oeaon.hydrodaily.domain.model.UserProfile
import com.oeaon.hydrodaily.domain.repository.ProfileRepository
import javax.inject.Inject

class SaveProfileUseCase @Inject constructor(
    private val repository: ProfileRepository,
    private val calculateDailyGoal: CalculateDailyGoalUseCase
) {

    suspend operator fun invoke(
        weightKg: Double?,
        gender: Gender?,
        activityLevel: ActivityLevel?
    ): Result {
        if (weightKg == null || weightKg !in VALID_WEIGHT_RANGE) return Result.InvalidWeight
        if (gender == null) return Result.MissingGender
        if (activityLevel == null) return Result.MissingActivityLevel

        val goal = calculateDailyGoal(weightKg, gender, activityLevel)
        val profile = UserProfile(weightKg, gender, activityLevel, goal)
        repository.save(profile)
        return Result.Saved(profile)
    }

    sealed interface Result {
        data class Saved(val profile: UserProfile) : Result
        data object InvalidWeight : Result
        data object MissingGender : Result
        data object MissingActivityLevel : Result
    }

    companion object {
        val VALID_WEIGHT_RANGE = 20.0..300.0
    }
}
