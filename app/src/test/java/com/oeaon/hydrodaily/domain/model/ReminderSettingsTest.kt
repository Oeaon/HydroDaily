package com.oeaon.hydrodaily.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalTime

class ReminderSettingsTest {

    @Test
    fun `once a day uses only the start time`() {
        assertEquals(listOf(LocalTime.of(8, 30)), ReminderSettings(startTime = LocalTime.of(8, 30)).timesOfDay())
    }

    @Test
    fun `interval includes the end time when it falls on a slot`() {
        val settings = ReminderSettings(
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(12, 0),
            frequency = ReminderFrequency.EVERY_90_MINUTES
        )
        assertEquals(listOf(LocalTime.of(9, 0), LocalTime.of(10, 30), LocalTime.of(12, 0)), settings.timesOfDay())
    }

    @Test
    fun `end before start falls back to a single reminder`() {
        val settings = ReminderSettings(
            startTime = LocalTime.of(20, 0),
            endTime = LocalTime.of(8, 0),
            frequency = ReminderFrequency.EVERY_HOUR
        )
        assertEquals(listOf(LocalTime.of(20, 0)), settings.timesOfDay())
    }
}
