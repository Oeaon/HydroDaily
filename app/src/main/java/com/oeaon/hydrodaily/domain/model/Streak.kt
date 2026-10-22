package com.oeaon.hydrodaily.domain.model

data class Streak(
    val current: Int = 0,
    val best: Int = 0,
    val completedToday: Boolean = false
)
