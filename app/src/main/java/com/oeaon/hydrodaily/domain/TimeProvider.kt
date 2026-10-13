package com.oeaon.hydrodaily.domain

import java.time.LocalDate
import java.time.ZonedDateTime

interface TimeProvider {
    fun now(): ZonedDateTime
    fun today(): LocalDate = now().toLocalDate()
}
