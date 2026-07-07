package com.medrem.app.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.medrem.app.MainActivity
import com.medrem.app.R

object NotificationHelper {
    private const val CHANNEL_ID = "medication_reminders"
    private const val CHANNEL_NAME = "Medication Reminders"
    private const val CHANNEL_DESC = "Notifications for your scheduled medications"

    fun showNotification(context: Context, title: String, message: String, data: Map<String, String>? = null) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                description = CHANNEL_DESC
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            data?.forEach { (key, value) -> putExtra(key, value) }
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Notification Action: Take
        val takeIntent = Intent(context, com.medrem.app.data.remote.ReminderActionReceiver::class.java).apply {
            action = "ACTION_TAKE"
            data?.forEach { (key, value) -> putExtra(key, value) }
            putExtra("notification_id", (data?.get("medication_id")?.hashCode() ?: 0))
        }
        val takePendingIntent = PendingIntent.getBroadcast(
            context, 1, takeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Notification Action: Skip
        val skipIntent = Intent(context, com.medrem.app.data.remote.ReminderActionReceiver::class.java).apply {
            action = "ACTION_SKIP"
            data?.forEach { (key, value) -> putExtra(key, value) }
            putExtra("notification_id", (data?.get("medication_id")?.hashCode() ?: 0))
        }
        val skipPendingIntent = PendingIntent.getBroadcast(
            context, 2, skipIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(0, "✅ Take", takePendingIntent)
            .addAction(0, "❌ Skip", skipPendingIntent)

        val notificationId = data?.get("medication_id")?.hashCode() ?: System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, builder.build())
    }
}
