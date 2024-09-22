package com.elshan.shiftnoc.notification.local

import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.elshan.shiftnoc.R
import com.elshan.shiftnoc.presentation.calendar.CalendarEvent
import com.elshan.shiftnoc.util.DIALOGS
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RequestExactAlarmPermissionDialog(
    onEvent: (CalendarEvent) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    fun event() {
        onEvent(
            CalendarEvent.ShowSnackBar(
                message = context.getString(R.string.please_grant_the_permission_in_the_settings),
                actionLabel = context.getString(R.string.settings),
                duration = SnackbarDuration.Short,
                onAction = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val intent =
                            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                putExtra(
                                    Settings.EXTRA_APP_PACKAGE,
                                    context.packageName
                                )
                            }
                        context.startActivity(intent)
                        scope.launch {
                            delay(500)
                            Toast.makeText(
                                context,
                                context.getString(R.string.please_allow),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            )
        )
    }

    AlertDialog(
        containerColor = MaterialTheme.colorScheme.onPrimary,
        titleContentColor = MaterialTheme.colorScheme.primary,
        textContentColor = MaterialTheme.colorScheme.primary,
        onDismissRequest = {
            onEvent(CalendarEvent.HideDialog(DIALOGS.REQUEST_EXACT_ALARM_PERMISSION))
            event()
        },
        title = {
            Text(
                stringResource(R.string.permission_required),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                stringResource(R.string.this_app_needs_the_exact_alarm_permission_to_send_reminders_at_the_exact_time_please_grant_the_permission_in_the_settings),
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
                            scope.launch {
                                delay(1000)
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.please_allow),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            val intent =
                                Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                            context.startActivity(intent)
                        }
                    } catch (e: Exception) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.unable_to_open_settings),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    onEvent(CalendarEvent.HideDialog(DIALOGS.REQUEST_EXACT_ALARM_PERMISSION))
                    onEvent(CalendarEvent.SetRequestExactAlarmPermission)
                }) {
                Text(stringResource(R.string.open_settings))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onEvent(CalendarEvent.HideDialog(DIALOGS.REQUEST_EXACT_ALARM_PERMISSION))
                event()
                onEvent(CalendarEvent.SetRequestExactAlarmPermission)
            }) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
