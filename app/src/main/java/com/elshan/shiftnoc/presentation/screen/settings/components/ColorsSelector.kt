package com.elshan.shiftnoc.presentation.screen.settings.components

import android.graphics.Color.parseColor
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatColorReset
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elshan.shiftnoc.R
import com.elshan.shiftnoc.presentation.calendar.CalendarEvent
import com.elshan.shiftnoc.presentation.components.ColorPickerDialog
import com.elshan.shiftnoc.presentation.components.DayType
import com.elshan.shiftnoc.presentation.components.isValidColor
import com.elshan.shiftnoc.ui.theme.ShiftnocTheme
import com.elshan.shiftnoc.util.DIALOGS
import com.elshan.shiftnoc.util.enums.CalendarView


@Composable
fun ColorsSelector(
    selectedDayColors: Map<DayType, String>,
    onEvent: (CalendarEvent) -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedDayType by remember { mutableStateOf<DayType?>(null) }
    val context = LocalContext.current
    val defaultColors = mapOf(
        DayType.WORK_MORNING to "#A3C9FE",
        DayType.WORK_NIGHT to "#E1E2E8",
        DayType.WORK_OFF to "#00000000",
        DayType.VACATION to "#FFC107",
        DayType.HOLIDAY to "#00000000"
    )
    if (showDialog) {
        ColorPickerDialog(
            initialColor = selectedDayColors[selectedDayType]?.toColor() ?: Color.Transparent,
            onColorSelected = { colorEnvelope ->
                val hexCodeWithHash = "#" + colorEnvelope.hexCode
                if (isValidColor(hexCodeWithHash)) {
                    selectedDayType?.let { dayType ->
                        onEvent(CalendarEvent.SaveDayColor(dayType, hexCodeWithHash))
                    }
                }
                showDialog = false
            },
            onEvent = onEvent
        )
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier
                .padding(start = 12.dp, end = 12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.color_picker),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(R.string.reset),
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                modifier = Modifier.clickable {
                    onEvent.apply {
                        this(CalendarEvent.ResetDayColors)
                        this(CalendarEvent.ShowToast(context.getString(R.string.colors_reset)))
                    }
                }
            )
        }
        HorizontalDivider(
            thickness = 2.dp,
            modifier = Modifier.padding(top = 12.dp, start = 12.dp, end = 12.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Column(Modifier.verticalScroll(rememberScrollState())) {
            DayType.entries.forEach { dayType ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .clickable {
                            selectedDayType = dayType
                            showDialog = !showDialog
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(id = dayType.stringResId),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                color = if (selectedDayColors[dayType] != null) {
                                    Color(parseColor(selectedDayColors[dayType]))
                                } else {
                                    Color(parseColor(defaultColors[dayType]))
                                },
                                shape = CircleShape
                            )
                    )
                }
            }
        }
    }
}

fun String.toColor(): Color {
    return Color(parseColor(this))
}


@Preview(showBackground = true)
@Composable
fun ColorsSelectorPreview() {
    ShiftnocTheme {
        ColorsSelector(
            selectedDayColors = mapOf(
                DayType.WORK_MORNING to "#FF0000",
                DayType.WORK_NIGHT to "#00FF00",
                DayType.WORK_OFF to "#FFFFFF"
            )
        )
    }
}