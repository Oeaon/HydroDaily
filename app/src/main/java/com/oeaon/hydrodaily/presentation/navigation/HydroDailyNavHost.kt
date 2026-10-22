package com.oeaon.hydrodaily.presentation.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.oeaon.hydrodaily.presentation.components.BannerAd
import com.oeaon.hydrodaily.presentation.profile.ProfileRoute
import com.oeaon.hydrodaily.presentation.today.TodayRoute

@Composable
fun HydroDailyNavHost(
    startDestination: Destination,
    bannerAdUnitId: String?,
    privacyOptionsRequired: Boolean,
    onPrivacyOptions: () -> Unit,
    onProfileSaved: (navigateToToday: () -> Unit) -> Unit,
    onRequestReview: () -> Unit,
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Column {
                bannerAdUnitId?.let { BannerAd(adUnitId = it) }
                NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
                    Destination.entries.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = { navController.navigateToTab(destination) },
                            icon = { Icon(painterResource(destination.icon), contentDescription = null) },
                            label = { Text(stringResource(destination.label)) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination.route,
            modifier = Modifier
                .consumeWindowInsets(innerPadding)
                .imePadding()
        ) {
            composable(Destination.Today.route) {
                TodayRoute(
                    contentPadding = innerPadding,
                    onSetUpProfile = { navController.navigateToTab(Destination.Profile) },
                    onRequestReview = onRequestReview
                )
            }
            composable(Destination.Profile.route) {
                ProfileRoute(
                    contentPadding = innerPadding,
                    privacyOptionsRequired = privacyOptionsRequired,
                    onPrivacyOptions = onPrivacyOptions,
                    onProfileSaved = {
                        onProfileSaved { navController.navigateToTab(Destination.Today) }
                    }
                )
            }
        }
    }
}

private fun NavHostController.navigateToTab(destination: Destination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
