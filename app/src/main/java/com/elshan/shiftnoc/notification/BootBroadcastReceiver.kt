package com.elshan.shiftnoc.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.elshan.shiftnoc.data.repository.MainRepositoryImpl
import com.elshan.shiftnoc.domain.repository.MainRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@AndroidEntryPoint
class BootBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var mainRepository: MainRepository

    @Inject
    lateinit var notificationsService: NotificationsService

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            context?.let {
                rescheduleNotifications()
            }
        }
    }

    private fun rescheduleNotifications() {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            val notes = mainRepository.getAllNotes().firstOrNull() ?: emptyList()
            notes.forEach { note ->
                note.reminder?.let { reminder ->
                    if (reminder.isAfter(LocalDateTime.now())) {
                        notificationsService.showNotification(note)
                    }
                }
            }
        }
    }
}

