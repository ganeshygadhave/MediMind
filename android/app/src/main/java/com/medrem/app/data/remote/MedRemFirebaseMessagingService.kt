package com.medrem.app.data.remote

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.medrem.app.data.local.TokenManager
import com.medrem.app.data.remote.api.UserApi
import com.medrem.app.data.remote.dto.UserProfileUpdateDto
import com.medrem.app.util.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Firebase Cloud Messaging service for receiving push notifications.
 */
@AndroidEntryPoint
class MedRemFirebaseMessagingService : FirebaseMessagingService() {

    @Inject lateinit var tokenManager: TokenManager
    @Inject lateinit var userApi: UserApi

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token generated: $token")
        tokenManager.saveFCMToken(token)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (tokenManager.getTokenSync() != null) {
                    userApi.updateProfile(UserProfileUpdateDto(fcmToken = token))
                    Log.d("FCM", "FCM token synced to backend")
                }
            } catch (e: Exception) {
                Log.e("FCM", "Failed to sync FCM token", e)
            }
        }
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
