package com.oeaon.hydrodaily.domain.repository

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface GoalHistoryRepository {
    val goalDays: Flow<Set<LocalDate>>
    suspend fun setGoalReached(date: LocalDate, reached: Boolean)
}
