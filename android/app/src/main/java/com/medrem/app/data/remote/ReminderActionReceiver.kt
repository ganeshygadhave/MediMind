package com.medrem.app.data.remote

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.medrem.app.data.local.TokenManager
import com.medrem.app.data.remote.api.MedicationApi
import com.medrem.app.data.remote.dto.DoseLogRequestDto
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var apiService: MedicationApi

    @Inject
    lateinit var tokenManager: TokenManager

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val medicationId = intent.getStringExtra("medication_id") ?: return
        val scheduledTime = intent.getStringExtra("scheduled_time") ?: return
        val notificationId = intent.getIntExtra("notification_id", -1)

        // Close the notification
        if (notificationId != -1) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notificationId)
        }

        val actionType = when (action) {
            "ACTION_TAKE" -> "taken"
            "ACTION_SKIP" -> "skipped"
            else -> return
        }

        // Send to backend in background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = tokenManager.getTokenSync()
                if (token != null) {
                    val request = DoseLogRequestDto(
                        medicationId = medicationId,
                        scheduledTime = scheduledTime,
                        action = actionType
                    )
                    
                    if (actionType == "taken") {
                        apiService.markMedicationTaken("Bearer $token", request)
                    } else {
                        apiService.markMedicationSkipped("Bearer $token", request)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
