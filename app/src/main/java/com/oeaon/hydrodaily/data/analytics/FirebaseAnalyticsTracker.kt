package com.oeaon.hydrodaily.data.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.oeaon.hydrodaily.domain.analytics.AnalyticsEvent
import com.oeaon.hydrodaily.domain.analytics.AnalyticsTracker
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAnalyticsTracker @Inject constructor(
    @ApplicationContext context: Context
) : AnalyticsTracker {

    private val analytics = FirebaseAnalytics.getInstance(context)

    override fun track(event: AnalyticsEvent) {
        analytics.logEvent(event.name, event.params.toBundle())
    }

    override fun setCollectionEnabled(enabled: Boolean) {
        analytics.setAnalyticsCollectionEnabled(enabled)
    }

    private fun Map<String, Any>.toBundle(): Bundle = Bundle().apply {
        forEach { (key, value) ->
            when (value) {
                is Int -> putLong(key, value.toLong())
                is Long -> putLong(key, value)
                is Double -> putDouble(key, value)
                else -> putString(key, value.toString())
            }
        }
    }
}
