package com.oeaon.hydrodaily.domain.model

data class UserProfile(
    val weightKg: Double,
    val gender: Gender,
    val activityLevel: ActivityLevel,
    val dailyGoalMl: Int
)
