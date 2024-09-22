package com.elshan.shiftnoc.notification.firebase

import android.provider.Settings.Global.getString
import android.util.Log
import android.widget.Toast
import com.elshan.shiftnoc.R
import com.elshan.shiftnoc.notification.local.NotificationsService
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.internal.Logger.TAG
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.messaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationsService: NotificationsService

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.let {
            CoroutineScope(Dispatchers.IO).launch {
                notificationsService.showFCMNotification(it.title, "${it.body}")
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        sendTokenToFirebase(token)
    }

    private fun sendTokenToFirebase(token: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("pushNotification", "Token: ${task.result}")
            } else {
                Log.d("pushNotification", "Error: ${task.exception}")
            }
        }
    }
}


