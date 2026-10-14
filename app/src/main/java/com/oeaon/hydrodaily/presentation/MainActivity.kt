package com.oeaon.hydrodaily.presentation

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.oeaon.hydrodaily.ads.AdUnitIds
import com.oeaon.hydrodaily.ads.AppOpenAdManager
import com.oeaon.hydrodaily.ads.ConsentManager
import com.oeaon.hydrodaily.ads.InterstitialAdManager
import com.oeaon.hydrodaily.ads.MobileAdsInitializer
import com.oeaon.hydrodaily.presentation.navigation.HydroDailyNavHost
import com.oeaon.hydrodaily.presentation.review.InAppReviewLauncher
import com.oeaon.hydrodaily.presentation.theme.HydroDailyTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var consentManager: ConsentManager
    @Inject lateinit var adsInitializer: MobileAdsInitializer
    @Inject lateinit var interstitialAdManager: InterstitialAdManager
    @Inject lateinit var appOpenAdManager: AppOpenAdManager
    @Inject lateinit var adUnitIds: AdUnitIds
    @Inject lateinit var reviewLauncher: InAppReviewLauncher

    private val viewModel: MainViewModel by viewModels()
    private var consentResolved by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition { viewModel.startDestination.value == null }
        enableEdgeToEdge()

        gatherConsentAndInitAds()

        setContent {
            HydroDailyTheme {
                val startDestination by viewModel.startDestination.collectAsStateWithLifecycle()
                val adsReady by adsInitializer.isReady.collectAsStateWithLifecycle()
                val privacyOptionsRequired by consentManager.privacyOptionsRequired.collectAsStateWithLifecycle()

                NotificationPermissionRequest()

                startDestination?.let { destination ->
                    HydroDailyNavHost(
                        startDestination = destination,
                        bannerAdUnitId = adUnitIds.banner.takeIf { adsReady },
                        privacyOptionsRequired = privacyOptionsRequired,
                        onPrivacyOptions = { consentManager.showPrivacyOptionsForm(this) },
                        onProfileSaved = { navigateToToday ->
                            interstitialAdManager.showIfReady(this, onFinished = navigateToToday)
                        },
                        onRequestReview = {
                            lifecycleScope.launch { reviewLauncher.launch(this@MainActivity) }
                        }
                    )
                }
            }
        }
    }

    private fun gatherConsentAndInitAds() {
        consentManager.gatherConsent(this) { canRequestAds ->
            consentResolved = true
            if (canRequestAds) initializeAds()
        }
        if (consentManager.canRequestAds) initializeAds()
    }

    private fun initializeAds() {
        adsInitializer.initialize {
            interstitialAdManager.preload()
            appOpenAdManager.preload()
        }
    }

    @Composable
    private fun NotificationPermissionRequest() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val alreadyAsked by viewModel.notificationPermissionAsked.collectAsStateWithLifecycle()
        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
        LaunchedEffect(consentResolved, alreadyAsked) {
            if (consentResolved && !alreadyAsked) {
                viewModel.onNotificationPermissionRequested()
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
