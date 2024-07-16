package com.elshan.shiftnoc.notification

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.elshan.shiftnoc.presentation.calendar.CalendarEvent
import com.elshan.shiftnoc.util.DIALOGS

@Composable
fun RequestExactAlarmPermissionDialog(
    onEvent: (CalendarEvent) -> Unit,
) {
    val context = LocalContext.current

    AlertDialog(
        containerColor = MaterialTheme.colorScheme.onPrimary,
        titleContentColor = MaterialTheme.colorScheme.primary,
        textContentColor = MaterialTheme.colorScheme.primary,
        onDismissRequest = {
            onEvent(CalendarEvent.HideDialog(DIALOGS.REQUEST_EXACT_ALARM_PERMISSION))
            Toast.makeText(
                context,
                "Please grant the permission in the settings.",
                Toast.LENGTH_LONG
            ).show()
        },
        title = { Text("Permission Required", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Text(
                "This app needs the exact alarm permission to send reminders at the exact time. Please grant the permission in the settings.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            FilledTonalButton(
                onClick = {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val intent =
                                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    putExtra(
                                        Settings.EXTRA_APP_PACKAGE,
                                        context.packageName
                                    )
                                }
                            context.startActivity(intent)
                        } else {
                            val intent =
                                Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                            context.startActivity(intent)
                        }
                    } catch (e: Exception) {
                        Toast.makeText(
                            context,
                            "Unable to open settings",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    onEvent(CalendarEvent.HideDialog(DIALOGS.REQUEST_EXACT_ALARM_PERMISSION))
                }) {
                Text("Open Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onEvent(CalendarEvent.HideDialog(DIALOGS.REQUEST_EXACT_ALARM_PERMISSION))
                Toast.makeText(
                    context,
                    "Please grant the permission in the settings.",
                    Toast.LENGTH_LONG
                ).show()
            }) {
                Text("Cancel")
            }
        }
    )
}
