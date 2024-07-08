package com.elshan.shiftnoc.presentation.screen.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import com.elshan.shiftnoc.util.Languages
import java.time.DayOfWeek
import java.util.Locale

@Composable
fun WeekSelectorDialog(
    dayOfWeek: DayOfWeek,
    onDayOfWeekSelected: (DayOfWeek) -> Unit
) {
    var selectedDayOfWeek by remember { mutableStateOf(dayOfWeek) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.select_first_day_of_the_week),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Column(Modifier.verticalScroll(rememberScrollState())) {
            DayOfWeek.entries.forEach { dayOfWeek ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedDayOfWeek == dayOfWeek,
                        onClick = {
                            selectedDayOfWeek = dayOfWeek
                            onDayOfWeekSelected(selectedDayOfWeek)
                        },
                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                    )
                    Text(
                        text = dayOfWeek.getDisplayName(
                            java.time.format.TextStyle.FULL,
                            Locale.getDefault()
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }
    }

}