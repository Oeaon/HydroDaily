package com.oeaon.hydrodaily.presentation.common

import android.content.Context
import android.text.format.DateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

fun Context.formatTime(time: LocalTime): String {
    val pattern = if (DateFormat.is24HourFormat(this)) "HH:mm" else "h:mm a"
    return time.format(DateTimeFormatter.ofPattern(pattern, Locale.getDefault()))
}
