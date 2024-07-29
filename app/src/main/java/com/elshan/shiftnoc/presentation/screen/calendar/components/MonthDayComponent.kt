package com.elshan.shiftnoc.presentation.screen.calendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.elshan.shiftnoc.data.local.NoteEntity
import com.elshan.shiftnoc.presentation.calendar.AppState
import com.elshan.shiftnoc.presentation.components.DayType
import com.elshan.shiftnoc.presentation.screen.settings.components.toColor
import com.elshan.shiftnoc.util.enums.CalendarView
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import java.time.DayOfWeek
import java.time.LocalDate

@Composable
fun MonthDayComponent(
    day: CalendarDay,
    dayType: DayType?,
    appState: AppState,
    onClick: (CalendarDay) -> Unit,
) {
    if (day.position != DayPosition.MonthDate) {
        Box(modifier = Modifier.aspectRatio(1f)) // Empty box for in-dates and out-dates
        return
    }

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

    val dayTextStyle = MaterialTheme.typography.bodyMedium

    Box(
        modifier = Modifier
            .aspectRatio(
                when (appState.calendarView) {
                    CalendarView.WEEKLY -> 0.6f
                    CalendarView.HORIZONTAL_MONTHLY -> 0.515f
                    CalendarView.VERTICAL_MONTHLY -> 1f
                }
            )
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center

        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                style = dayTextStyle,
                color = if (DayOfWeek.SUNDAY == day.date.dayOfWeek) MaterialTheme.colorScheme.error else textColor
            )
            val notes = appState.notes[day.date] ?: emptyList()

            if (notes.isNotEmpty()) {
                when (appState.calendarView) {
                    CalendarView.WEEKLY -> {
                        VerticalNoteItemColorful(notes)
                    }

                    CalendarView.HORIZONTAL_MONTHLY -> {
                        HorizontalNoteItemColorful(notes)
                    }

                    CalendarView.VERTICAL_MONTHLY -> {
                        VerticalNoteItemColorful(notes)
                    }
                }
            }
        }
    }
}


@Composable
fun VerticalNoteItemColorful(
    note: List<NoteEntity>,
) {

    Row(
        modifier = Modifier.padding(top = 3.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        note.forEach { note ->
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .padding(2.dp)
                    .background(
                        Color(android.graphics.Color.parseColor(note.color)),
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
fun HorizontalNoteItemColorful(
    note: List<NoteEntity>,
) {
    Column(
        modifier = Modifier.padding(top = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        note.forEach { note ->
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                color = Color(android.graphics.Color.parseColor(note.color)),
                thickness = 5.dp
            )
        }
    }
}