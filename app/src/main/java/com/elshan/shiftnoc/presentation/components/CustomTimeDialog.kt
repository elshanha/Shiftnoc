package com.elshan.shiftnoc.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.commandiron.wheel_picker_compose.WheelTimePicker
import com.elshan.shiftnoc.R
import com.elshan.shiftnoc.presentation.calendar.CalendarEvent
import com.elshan.shiftnoc.ui.theme.ShiftnocTheme
import com.elshan.shiftnoc.util.DIALOGS
import java.time.LocalTime

@Composable
fun CustomTimeDialog(
    onTimeSelected: (LocalTime) -> Unit,
    initialTime: LocalTime = LocalTime.now(),
    onEvent: (CalendarEvent) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            WheelTimePicker(
                startTime = initialTime,
                onSnappedTime = { snappedTime ->
                    onTimeSelected(snappedTime)
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = {
                    onEvent(CalendarEvent.HideDialog(DIALOGS.TIME_PICKER))
                }
            ) {
                Text(stringResource(R.string.done))
            }
        }
    }
}


@Preview
@Composable
fun CustomTimeDialogPreview() {
    ShiftnocTheme {
        CustomTimeDialog(
            onTimeSelected = {},
            initialTime = LocalTime.now(),
            onEvent = {})
    }
}