package com.oeaon.hydrodaily.ads

import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.oeaon.hydrodaily.di.ApplicationScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MobileAdsInitializer @Inject constructor(
    @ApplicationContext private val context: Context,
    @ApplicationScope private val scope: CoroutineScope
) {

    private val started = AtomicBoolean(false)
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    fun initialize(onReady: () -> Unit = {}) {
        if (!started.compareAndSet(false, true)) {
            if (_isReady.value) onReady()
            return
        }
        scope.launch(Dispatchers.IO) {
            MobileAds.initialize(context) {
                scope.launch(Dispatchers.Main) {
                    _isReady.value = true
                    onReady()
                }
            }
        }
    }
}
