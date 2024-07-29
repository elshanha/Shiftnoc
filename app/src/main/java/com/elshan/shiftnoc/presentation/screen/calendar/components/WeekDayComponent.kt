package com.elshan.shiftnoc.presentation.screen.calendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elshan.shiftnoc.presentation.calendar.AppState
import com.elshan.shiftnoc.presentation.components.DayType
import com.elshan.shiftnoc.presentation.components.ShiftType
import com.elshan.shiftnoc.presentation.screen.settings.components.toColor
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.core.WeekDayPosition
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun WeekDayComponent(
    day: WeekDay,
    dayType: DayType?,
    onClick: (WeekDay) -> Unit,
    appState: AppState
) {

    val backgroundColor = when (dayType) {
        DayType.WORK_MORNING -> appState.selectedDayColor[DayType.WORK_MORNING]?.toColor()
            ?: MaterialTheme.colorScheme.primary

        DayType.WORK_NIGHT -> appState.selectedDayColor[DayType.WORK_NIGHT]?.toColor()
            ?: MaterialTheme.colorScheme.secondary

        DayType.WORK_OFF -> appState.selectedDayColor[DayType.WORK_OFF]?.toColor()
            ?: Color.Transparent

        DayType.VACATION -> appState.selectedDayColor[DayType.VACATION]?.toColor()
            ?: MaterialTheme.colorScheme.tertiary

        DayType.HOLIDAY -> appState.selectedDayColor[DayType.HOLIDAY]?.toColor()
            ?: MaterialTheme.colorScheme.onBackground

        null -> Color.Transparent // Default for no shift type
    }

    val textColor = when (dayType) {
        DayType.WORK_MORNING -> MaterialTheme.colorScheme.background
        DayType.WORK_NIGHT -> MaterialTheme.colorScheme.background
        DayType.WORK_OFF -> MaterialTheme.colorScheme.secondary
        DayType.VACATION -> MaterialTheme.colorScheme.background
        DayType.HOLIDAY -> MaterialTheme.colorScheme.background
        null -> MaterialTheme.colorScheme.secondary // Default for no shift type
    }

    val dayTextStyle = MaterialTheme.typography.bodyLarge

    Box(
        modifier = Modifier
            .aspectRatio(0.6f)
            .padding(6.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                backgroundColor
            )
            .border(
                if (day.date == LocalDate.now()) 2.dp else (-1).dp,
                MaterialTheme.colorScheme.onBackground,
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick(day) },
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                style = dayTextStyle,
                color = if (DayOfWeek.SUNDAY == day.date.dayOfWeek) MaterialTheme.colorScheme.error else textColor
            )
            val notes = appState.notes[day.date] ?: emptyList()

            if (notes.isNotEmpty()) {
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    notes.forEach { note ->
                        Box(
                            modifier = Modifier
                                .size(11.dp)
                                .padding(2.dp)
                                .background(
                                    Color(android.graphics.Color.parseColor(note.color)),
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }

            Text(
                text = day.date.month.getDisplayName(
                    TextStyle.SHORT,
                    Locale.getDefault()
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
            )
        }

    }
}