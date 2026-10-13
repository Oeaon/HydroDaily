package com.oeaon.hydrodaily.domain.reminder

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class NextReminderCalculatorTest {

    private val calculator = NextReminderCalculator()
    private val madrid = ZoneId.of("Europe/Madrid")

    @Test
    fun `before noon schedules today at noon`() {
        val now = ZonedDateTime.of(LocalDateTime.of(2026, 3, 10, 9, 30), madrid)
        assertEquals(ZonedDateTime.of(LocalDateTime.of(2026, 3, 10, 12, 0), madrid), calculator.nextTrigger(now))
    }

    @Test
    fun `exactly at noon schedules next day`() {
        val now = ZonedDateTime.of(LocalDateTime.of(2026, 3, 10, 12, 0), madrid)
        assertEquals(ZonedDateTime.of(LocalDateTime.of(2026, 3, 11, 12, 0), madrid), calculator.nextTrigger(now))
    }

    @Test
    fun `after noon schedules next day`() {
        val now = ZonedDateTime.of(LocalDateTime.of(2026, 3, 10, 18, 0), madrid)
        assertEquals(ZonedDateTime.of(LocalDateTime.of(2026, 3, 11, 12, 0), madrid), calculator.nextTrigger(now))
    }

    @Test
    fun `respects local time zone across daylight saving change`() {
        val now = ZonedDateTime.of(LocalDateTime.of(2026, 3, 28, 15, 0), madrid)
        val next = calculator.nextTrigger(now)
        assertEquals(12, next.hour)
        assertEquals(29, next.dayOfMonth)
    }

    @Test
    fun `uses the zone of the given time`() {
        val tokyo = ZoneId.of("Asia/Tokyo")
        val now = ZonedDateTime.of(LocalDateTime.of(2026, 7, 1, 8, 0), tokyo)
        assertEquals(ZonedDateTime.of(LocalDateTime.of(2026, 7, 1, 12, 0), tokyo), calculator.nextTrigger(now))
    }
}
