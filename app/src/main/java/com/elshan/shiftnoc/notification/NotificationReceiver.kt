package com.elshan.shiftnoc.notification

import android.Manifest
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.elshan.shiftnoc.presentation.main.MainActivity
import com.elshan.shiftnoc.R

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val noteId = intent.getLongExtra("noteId", 0L)
        val content = intent.getStringExtra("content") ?: return

        sendNotification(context, noteId, content)

        Log.d("AlarmReceiver", "onReceive: ${noteId.hashCode()}")
        Log.d("AlarmReceiver", "onReceive: $content")

    }

    private fun sendNotification(context: Context, noteId: Long, content: String) {

        val notificationManager = NotificationManagerCompat.from(context)
        val notificationId = noteId.hashCode()

        val notificationIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(notificationIntent)
            getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE)
        }

        val dismissIntent = Intent(context, NotificationDismissReceiver::class.java).apply {
            putExtra("notification_id", notificationId)
        }

        val dismissPendingIntent: PendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            dismissIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle(context.getString(R.string.reminder))
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setAutoCancel(true)
            .addAction(
                R.drawable.app_logo,
                context.getString(R.string.dismiss),
                dismissPendingIntent
            )

        try {
            notificationManager.notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            Log.e("Notification", "Error posting notification", e)
        }
    }
}


