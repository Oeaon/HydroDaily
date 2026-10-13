package com.oeaon.hydrodaily.domain.model

import java.time.LocalDate

data class DailyIntake(
    val date: LocalDate,
    val entriesMl: List<Int>
) {
    val totalMl: Int get() = entriesMl.sum()

    companion object {
        fun empty(date: LocalDate) = DailyIntake(date, emptyList())
    }
}
