package com.example.hydrodaily

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging

class MyApp : Application() {

    companion object {
         const val CHANNEL_NAME = "HidroDaily Channel"
         const val CHANNEL_ID = "HidroDailyChannelID"
         const val CHANNEL_DESCRIPTION = "Notifications for HidroDaily"
         const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate() {
            super.onCreate()

        Firebase.messaging.token.addOnCompleteListener(){
            if (!it.isSuccessful){
                println("el token no se pudo obtener")
            }
            else{
                val token = it.result
                println("el token es: "+ token)
            }
            createNotificationChannel()
        }
    }

private fun createNotificationChannel(){
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
        channel.description ="Estas notificaciones van a ser recibidas desde FCM"
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
}
}