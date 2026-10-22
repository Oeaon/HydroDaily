package com.oeaon.hydrodaily.domain.streak

import com.oeaon.hydrodaily.domain.model.Streak
import java.time.LocalDate
import javax.inject.Inject

class StreakCalculator @Inject constructor() {

    fun calculate(goalDays: Set<LocalDate>, today: LocalDate): Streak {
        val anchor = when {
            today in goalDays -> today
            today.minusDays(1) in goalDays -> today.minusDays(1)
            else -> null
        }
        val current = anchor?.let { runEndingAt(it, goalDays) } ?: 0
        return Streak(
            current = current,
            best = maxOf(bestRun(goalDays), current),
            completedToday = today in goalDays
        )
    }

    private fun runEndingAt(day: LocalDate, goalDays: Set<LocalDate>): Int =
        generateSequence(day) { it.minusDays(1) }.takeWhile { it in goalDays }.count()

    private fun bestRun(goalDays: Set<LocalDate>): Int {
        var best = 0
        var run = 0
        var previous: LocalDate? = null
        goalDays.sorted().forEach { day ->
            run = if (previous?.plusDays(1) == day) run + 1 else 1
            best = maxOf(best, run)
            previous = day
        }
        return best
    }
}
