package com.elshan.shiftnoc.notification

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.compose.ui.util.trace
import androidx.core.app.NotificationCompat
import com.elshan.shiftnoc.R
import com.elshan.shiftnoc.data.local.NoteEntity
import java.time.LocalDateTime
import java.time.ZoneId

const val NOTIFICATION_CHANNEL_ID = "CH-1"
const val NOTIFICATION_CHANNEL_NAME = "Reminder"
const val REQUEST_CODE = 200

interface NotificationsService {
    suspend fun showNotification(note: NoteEntity)
    fun createNotificationChannel()
    fun hideNotification(note: NoteEntity)
}


class NotificationsServiceImpl(
    private val context: Context,
) : NotificationsService {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override suspend fun showNotification(note: NoteEntity) {
        val notificationTime =
            (note.reminder?.atZone(ZoneId.systemDefault())?.toEpochSecond() ?: 0) * 1000

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("noteId", note.id)
            putExtra("content", note.content)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            note.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            notificationTime,
            pendingIntent
        )
        Log.d("Notification", "Notification scheduled for ${note.reminder}")
        Log.d("alarm,","will be set at $notificationTime")
    }

    override fun createNotificationChannel() {
        val soundUri =
            Uri.parse("android.resource://${context.packageName}/${R.raw.sms_notification}")

        val vibration = longArrayOf(0, 2000, 100, 2000)

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_ALARM) // Set usage to ALARM to increase priority
            .build()

        val notificationChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Reminder Channel"
            enableVibration(true)
            vibrationPattern = vibration
            setSound(soundUri, audioAttributes)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
    }


    override fun hideNotification(note: NoteEntity) {
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context,
                note.id.hashCode(),
                Intent(context, AlarmReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
        notificationManager.cancel(note.id.hashCode())
    }
}

