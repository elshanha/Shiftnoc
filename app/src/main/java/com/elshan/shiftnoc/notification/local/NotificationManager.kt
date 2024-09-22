package com.elshan.shiftnoc.notification.local

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.elshan.shiftnoc.R
import com.elshan.shiftnoc.data.local.NoteEntity
import com.elshan.shiftnoc.presentation.main.MainActivity
import java.time.ZoneId
import javax.inject.Inject

const val NOTIFICATION_CHANNEL_ID = "CH-1"
const val NOTIFICATION_CHANNEL_NAME = "Reminder"
const val NOTIFICATION_ID = 1
const val REQUEST_CODE = 100

interface NotificationsService {
    suspend fun showNotification(note: NoteEntity, onPermissionError: () -> Unit = {})
    suspend fun showFCMNotification(title: String?, body: String?)
    fun createNotificationChannel()
    fun hideNotification(note: NoteEntity)
}

class NotificationsServiceImpl @Inject constructor(
    private val context: Context,
) : NotificationsService {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    private val myIntent = Intent(
        context, MainActivity::class.java
    )

    private val myPendingIntent = PendingIntent.getActivity(
        context,
        REQUEST_CODE,
        myIntent,
        PendingIntent.FLAG_IMMUTABLE
    )

    override suspend fun showNotification(note: NoteEntity, onPermissionError: () -> Unit) {
        val notificationTime =
            note.reminder?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli() ?: return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("noteId", note.id)
            putExtra("content", note.content)
            putExtra("reminder", note.reminder)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            note.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    notificationTime,
                    pendingIntent
                )
            } else {
                onPermissionError()
            }
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                notificationTime,
                pendingIntent
            )
        }
    }

    override suspend fun showFCMNotification(title: String?, body: String?) {
        if (!title.isNullOrEmpty() && !body.isNullOrEmpty()) {
            val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.app_logo)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(myPendingIntent)
                .build()

            notificationManager.notify(NOTIFICATION_ID, notification)
        }
    }


    override fun createNotificationChannel() {
        val soundUri =
            Uri.parse("android.resource://${context.packageName}/${R.raw.sms_notification}")

        val vibration = longArrayOf(0, 2000, 100, 2000)

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_ALARM)
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
            setShowBadge(true)
        }

        notificationManager.createNotificationChannel(notificationChannel)
    }

    override fun hideNotification(note: NoteEntity) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            note.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        notificationManager.cancel(note.id.toInt())
    }
}


