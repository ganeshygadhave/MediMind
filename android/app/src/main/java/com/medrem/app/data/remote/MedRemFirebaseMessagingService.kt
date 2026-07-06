package com.medrem.app.data.remote

// import com.google.firebase.messaging.FirebaseMessagingService
// import com.google.firebase.messaging.RemoteMessage

/**
 * Firebase Cloud Messaging service for receiving push notifications.
 * Currently disabled because Firebase dependencies are not yet configured.
 */
class MedRemFirebaseMessagingService /* : FirebaseMessagingService() */ {

    /*
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // TODO: Send the new FCM token to the backend
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // Handle medication reminder notifications
        val notificationType = message.data["type"]
        when (notificationType) {
            "medication_reminder" -> {
                val medicationId = message.data["medication_id"]
                val scheduledTime = message.data["scheduled_time"]
                // TODO: Show notification
            }
        }
    }
    */
}
