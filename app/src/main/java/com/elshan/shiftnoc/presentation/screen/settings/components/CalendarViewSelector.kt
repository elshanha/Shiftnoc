package com.elshan.shiftnoc.presentation.screen.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.elshan.shiftnoc.R
import com.elshan.shiftnoc.util.enums.CalendarView

@Composable
fun CalendarViewSelector(
    selectedCalendarView: CalendarView,
    onSelect: (CalendarView) -> Unit
) {
    var selectedView by remember { mutableStateOf(selectedCalendarView) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.calendar_view),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Column(Modifier.verticalScroll(rememberScrollState())) {
            CalendarView.entries.forEach { calendarView ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedView == calendarView,
                        onClick = {
                            selectedView = calendarView
                            onSelect(selectedView)
                        },
                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                    )
                    Text(
                        text = when (calendarView) {
                            CalendarView.WEEKLY -> stringResource(R.string.weekly)
                            CalendarView.VERTICAL_MONTHLY -> stringResource(R.string.vertical_monthly)
                            CalendarView.HORIZONTAL_MONTHLY -> stringResource(R.string.horizontal_monthly)
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }
    }
}