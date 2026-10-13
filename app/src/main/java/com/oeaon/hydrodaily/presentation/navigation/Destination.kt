package com.oeaon.hydrodaily.presentation.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.oeaon.hydrodaily.R

enum class Destination(
    val route: String,
    @StringRes val label: Int,
    @DrawableRes val icon: Int
) {
    Today("today", R.string.nav_today, R.drawable.ic_water_drop),
    Profile("profile", R.string.nav_profile, R.drawable.ic_person)
}
