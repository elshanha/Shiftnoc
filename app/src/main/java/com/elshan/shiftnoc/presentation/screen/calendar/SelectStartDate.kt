package com.elshan.shiftnoc.presentation.screen.calendar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.Date

@Composable
fun StartDatePicker(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    calendar.time = Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant())

    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        android.app.DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedLocalDate = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay)
                onDateSelected(selectedLocalDate)
                showDialog = false
            }, year, month, day
        ).show()
    }

    AlertDialog(
        containerColor = MaterialTheme.colorScheme.onPrimary,
        titleContentColor = MaterialTheme.colorScheme.primary,
        textContentColor = MaterialTheme.colorScheme.primary,
        onDismissRequest = { onDismiss() },
        confirmButton = {},
        title = { Text(text = "Select Start Date", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                Text(
                    modifier = Modifier.padding(bottom = 8.dp),
                    text = "Selected Date: $selectedDate",
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(onClick = { showDialog = true }) {
                    Text(text = "Select Date")
                }

            }
        }
    )
}


