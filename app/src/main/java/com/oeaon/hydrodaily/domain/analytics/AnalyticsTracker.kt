package com.oeaon.hydrodaily.domain.analytics

interface AnalyticsTracker {
    fun track(event: AnalyticsEvent)
    fun setCollectionEnabled(enabled: Boolean)
}
