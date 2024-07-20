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
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.util.trace
import androidx.core.app.NotificationCompat
import com.elshan.shiftnoc.R
import com.elshan.shiftnoc.data.local.NoteEntity
import com.elshan.shiftnoc.domain.repository.MainRepository
import com.elshan.shiftnoc.presentation.calendar.CalendarEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

const val NOTIFICATION_CHANNEL_ID = "CH-1"
const val NOTIFICATION_CHANNEL_NAME = "Reminder"
const val REQUEST_CODE = 200

interface NotificationsService {
    suspend fun showNotification(note: NoteEntity, onPermissionError: () -> Unit = {})
    fun createNotificationChannel()
    fun hideNotification(note: NoteEntity)
}

class NotificationsServiceImpl @Inject constructor(
    private val context: Context,
) : NotificationsService {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

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
            note.id.hashCode(),
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
        }

        notificationManager.createNotificationChannel(notificationChannel)
    }

    override fun hideNotification(note: NoteEntity) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            note.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        notificationManager.cancel(note.id.hashCode())
    }
}


