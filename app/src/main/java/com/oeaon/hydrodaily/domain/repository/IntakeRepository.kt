package com.oeaon.hydrodaily.domain.repository

import com.oeaon.hydrodaily.domain.model.DailyIntake
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface IntakeRepository {
    fun observe(date: LocalDate): Flow<DailyIntake>
    suspend fun add(date: LocalDate, amountMl: Int)
    suspend fun removeLast(date: LocalDate)
}
