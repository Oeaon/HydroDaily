package com.oeaon.hydrodaily.presentation.review

import android.app.Activity
import android.content.Context
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InAppReviewLauncher @Inject constructor(
    @ApplicationContext context: Context
) {

    private val reviewManager: ReviewManager = ReviewManagerFactory.create(context)

    suspend fun launch(activity: Activity) {
        runCatching {
            val reviewInfo = reviewManager.requestReviewFlow().await()
            reviewManager.launchReviewFlow(activity, reviewInfo).await()
        }
    }
}
