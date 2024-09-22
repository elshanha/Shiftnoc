package com.elshan.shiftnoc.presentation.main.components

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.elshan.shiftnoc.R
import com.elshan.shiftnoc.presentation.calendar.AppState
import com.elshan.shiftnoc.presentation.calendar.CalendarEvent
import com.elshan.shiftnoc.util.DIALOGS

@Composable
fun ShowAutostartInstructionsDialogIfNeeded(
    onEvent: (CalendarEvent) -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        containerColor = MaterialTheme.colorScheme.onPrimary,
        titleContentColor = MaterialTheme.colorScheme.primary,
        textContentColor = MaterialTheme.colorScheme.primary,
        onDismissRequest = {
            onEvent(CalendarEvent.HideDialog(DIALOGS.SHOW_AUTOSTART_INSTRUCTIONS))
        },
        title = {
            Text(text = stringResource(R.string.enable_autostart), style = MaterialTheme.typography.headlineSmall)
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.to_ensure_notifications_work_properly_please_enable_autostart_and_disable_battery_optimization_for_this_app_in_your_device_settings),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    text = stringResource(R.string.settings_apps_permissions_enable_autostart_shiftnoc),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
            }

        },
        confirmButton = {
            FilledTonalButton(
                onClick = {
                    try {
                        val intent = Intent().apply {
                            component = ComponentName(
                                "com.miui.securitycenter",
                                "com.miui.permcenter.autostart.AutoStartManagementActivity"
                            )
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        try {
                            val intent =
                                Intent(Settings.ACTION_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                            context.startActivity(intent)
                        } catch (ex: Exception) {
                            try {
                                val intent = Intent(Settings.ACTION_SETTINGS)
                                context.startActivity(intent)
                            } catch (ex: Exception) {
                                Toast.makeText(
                                    context,
                                    R.string.unable_to_open_settings,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                    onEvent(CalendarEvent.HideDialog(DIALOGS.SHOW_AUTOSTART_INSTRUCTIONS))
                    onEvent(CalendarEvent.SetAutostartInstructionsShown)
                }
            ) {
                Text(stringResource(R.string.open_settings))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onEvent(CalendarEvent.HideDialog(DIALOGS.SHOW_AUTOSTART_INSTRUCTIONS))
                    onEvent(CalendarEvent.SetAutostartInstructionsShown)
                }
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
