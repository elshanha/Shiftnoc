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
import androidx.compose.ui.util.trace
import androidx.core.app.NotificationCompat
import com.elshan.shiftnoc.R
import java.time.LocalDateTime
import java.time.ZoneId

const val NOTIFICATION_CHANNEL_ID = "CH-1"
const val NOTIFICATION_CHANNEL_NAME = "Reminder"
const val REQUEST_CODE = 200

interface NotificationsService {
    suspend fun showNotification(noteId: Long, content: String, reminder: LocalDateTime)
    fun createNotificationChannel()
    fun hideNotification(notificationId: Int)
}


class NotificationsServiceImpl(
    private val context: Context,
) : NotificationsService {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override suspend fun showNotification(noteId: Long, content: String, reminder: LocalDateTime) {
        // Calculate the delay until the reminder time
        val notificationTime = reminder.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        // Create an intent for the AlarmReceiver
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("noteId", noteId)
            putExtra("content", content)
        }

        // Create a PendingIntent for the alarm
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            noteId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.canScheduleExactAlarms()
            } else {
                true
            }
        ) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                notificationTime,
                pendingIntent
            )
        }
    }

    override fun createNotificationChannel() {
        val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.sms_notification}")

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


    override fun hideNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }
}

