package com.oeaon.hydrodaily.notification

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.oeaon.hydrodaily.R
import com.oeaon.hydrodaily.presentation.MainActivity
import com.oeaon.hydrodaily.presentation.common.formatVolume
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderNotifier @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val channelId: String get() = context.getString(R.string.reminder_channel_id)

    fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            channelId,
            context.getString(R.string.reminder_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.reminder_channel_description)
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun canNotify(): Boolean = NotificationManagerCompat.from(context).areNotificationsEnabled()

    @SuppressLint("MissingPermission")
    fun show(title: String, body: String, notificationId: Int = DAILY_REMINDER_ID, quickAddMl: Int? = null) {
        if (!canNotify()) return
        createChannel()
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ContextCompat.getColor(context, R.color.brand_primary))
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(openAppIntent())
        quickAddMl?.let { amount ->
            builder.addAction(
                R.drawable.ic_water_drop,
                context.getString(R.string.notification_action_add, context.formatVolume(amount)),
                quickAddIntent(amount)
            )
        }
        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }

    fun dismiss(notificationId: Int = DAILY_REMINDER_ID) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }

    private fun quickAddIntent(amountMl: Int): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java)
            .setAction(ReminderReceiver.ACTION_QUICK_ADD)
            .putExtra(ReminderReceiver.EXTRA_AMOUNT_ML, amountMl)
        return PendingIntent.getBroadcast(
            context,
            QUICK_ADD_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun openAppIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val DAILY_REMINDER_ID = 1001
        const val PUSH_MESSAGE_ID = 1002
        private const val QUICK_ADD_REQUEST_CODE = 13
    }
}
