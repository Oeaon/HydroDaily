package com.oeaon.hydrodaily.data.time

import com.oeaon.hydrodaily.domain.TimeProvider
import java.time.ZonedDateTime
import javax.inject.Inject

class SystemTimeProvider @Inject constructor() : TimeProvider {
    override fun now(): ZonedDateTime = ZonedDateTime.now()
}
