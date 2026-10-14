package com.oeaon.hydrodaily.domain.streak

import com.oeaon.hydrodaily.domain.model.Streak
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class StreakCalculatorTest {

    private val calculator = StreakCalculator()
    private val today = LocalDate.of(2026, 10, 14)

    private fun daysAgo(vararg offsets: Long) = offsets.map { today.minusDays(it) }.toSet()

    @Test
    fun `no history means no streak`() {
        assertEquals(Streak(0, 0, false), calculator.calculate(emptySet(), today))
    }

    @Test
    fun `streak includes today when goal is reached`() {
        assertEquals(Streak(3, 3, true), calculator.calculate(daysAgo(0, 1, 2), today))
    }

    @Test
    fun `streak stays alive until today ends`() {
        assertEquals(Streak(2, 2, false), calculator.calculate(daysAgo(1, 2), today))
    }

    @Test
    fun `a missed day breaks the streak but keeps the best`() {
        assertEquals(Streak(0, 4, false), calculator.calculate(daysAgo(2, 3, 4, 5), today))
    }

    @Test
    fun `best streak is the longest run in history`() {
        assertEquals(Streak(1, 3, true), calculator.calculate(daysAgo(0, 5, 6, 7, 10), today))
    }
}
