package com.oeaon.hydrodaily.presentation.common

import android.content.Context
import com.oeaon.hydrodaily.R
import java.text.NumberFormat

fun Context.formatVolume(ml: Int): String {
    if (ml < ML_PER_LITER) return getString(R.string.volume_ml, NumberFormat.getIntegerInstance().format(ml))
    val liters = NumberFormat.getNumberInstance().apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 2
    }.format(ml / ML_PER_LITER.toDouble())
    return getString(R.string.volume_l, liters)
}

private const val ML_PER_LITER = 1000
