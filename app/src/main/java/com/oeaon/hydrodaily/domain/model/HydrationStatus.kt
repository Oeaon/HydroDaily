package com.oeaon.hydrodaily.domain.model

data class HydrationStatus(
    val profile: UserProfile?,
    val intake: DailyIntake
) {
    val goalMl: Int get() = profile?.dailyGoalMl ?: 0

    val progress: Float
        get() = if (goalMl == 0) 0f else (intake.totalMl.toFloat() / goalMl).coerceIn(0f, 1f)

    val remainingMl: Int get() = (goalMl - intake.totalMl).coerceAtLeast(0)

    val isGoalReached: Boolean get() = goalMl > 0 && intake.totalMl >= goalMl
}
