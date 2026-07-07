package com.medrem.app.data.remote

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.medrem.app.util.NotificationHelper
import android.util.Log

/**
 * Firebase Cloud Messaging service for receiving push notifications.
 */
class MedRemFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token generated: $token")
        // Token will be sent to backend during Login/Register flow 
        // or stored in TokenManager for the next app start.
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FCM", "Message received: ${message.data}")

        val title = message.notification?.title ?: message.data["title"] ?: "Medication Reminder"
        val body = message.notification?.body ?: message.data["body"] ?: "It's time to take your medicine."
        
        NotificationHelper.showNotification(
            context = applicationContext,
            title = title,
            message = body,
            data = message.data
        )
    }
}
