package com.oeaon.hydrodaily.notification

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.oeaon.hydrodaily.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HydroMessagingService : FirebaseMessagingService() {

    @Inject lateinit var notifier: ReminderNotifier

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: message.data[KEY_TITLE] ?: getString(R.string.notification_title)
        val body = message.notification?.body ?: message.data[KEY_BODY] ?: return
        notifier.show(title, body, ReminderNotifier.PUSH_MESSAGE_ID)
    }

    private companion object {
        const val KEY_TITLE = "title"
        const val KEY_BODY = "body"
    }
}
