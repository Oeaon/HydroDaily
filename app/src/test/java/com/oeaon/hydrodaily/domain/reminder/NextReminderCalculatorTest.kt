package com.oeaon.hydrodaily.domain.reminder

import com.oeaon.hydrodaily.domain.model.ReminderFrequency
import com.oeaon.hydrodaily.domain.model.ReminderSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class NextReminderCalculatorTest {

    private val calculator = NextReminderCalculator()
    private val madrid = ZoneId.of("Europe/Madrid")
    private val noonDaily = ReminderSettings()

    private fun at(day: Int, hour: Int, minute: Int = 0, zone: ZoneId = madrid) =
        ZonedDateTime.of(LocalDateTime.of(2026, 3, day, hour, minute), zone)

    @Test
    fun `before noon schedules today at noon by default`() {
        assertEquals(at(10, 12), calculator.nextTrigger(at(10, 9, 30), noonDaily))
    }

    @Test
    fun `exactly at noon schedules next day`() {
        assertEquals(at(11, 12), calculator.nextTrigger(at(10, 12), noonDaily))
    }

    @Test
    fun `respects local time zone across daylight saving change`() {
        val next = calculator.nextTrigger(at(28, 15), noonDaily)!!
        assertEquals(12, next.hour)
        assertEquals(29, next.dayOfMonth)
    }

    @Test
    fun `uses the zone of the given time`() {
        val tokyo = ZoneId.of("Asia/Tokyo")
        assertEquals(at(1, 12, zone = tokyo), calculator.nextTrigger(at(1, 8, zone = tokyo), noonDaily))
    }

    @Test
    fun `interval reminders pick the next slot of the day`() {
        val settings = ReminderSettings(
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(21, 0),
            frequency = ReminderFrequency.EVERY_2_HOURS
        )
        assertEquals(at(10, 15), calculator.nextTrigger(at(10, 13, 5), settings))
    }

    @Test
    fun `after the last slot moves to the first slot of the next day`() {
        val settings = ReminderSettings(
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(20, 0),
            frequency = ReminderFrequency.EVERY_3_HOURS
        )
        assertEquals(at(11, 9), calculator.nextTrigger(at(10, 18, 30), settings))
    }

    @Test
    fun `skips days that are not selected`() {
        val weekdays = setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
        val friday = at(13, 13)
        assertEquals(DayOfWeek.FRIDAY, friday.dayOfWeek)
        assertEquals(at(16, 12), calculator.nextTrigger(friday, noonDaily.copy(days = weekdays)))
    }

    @Test
    fun `disabled or no days returns null`() {
        assertNull(calculator.nextTrigger(at(10, 9), noonDaily.copy(enabled = false)))
        assertNull(calculator.nextTrigger(at(10, 9), noonDaily.copy(days = emptySet())))
    }
}
