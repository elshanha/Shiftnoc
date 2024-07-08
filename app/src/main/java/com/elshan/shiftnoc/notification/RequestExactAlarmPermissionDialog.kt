package com.elshan.shiftnoc.notification

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun RequestExactAlarmPermissionDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        onDismissRequest = onDismiss,
        title = { Text("Permission Required") },
        text = { Text("This app needs the exact alarm permission to send reminders at the exact time. Please grant the permission in the settings.") },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Go to Settings")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
