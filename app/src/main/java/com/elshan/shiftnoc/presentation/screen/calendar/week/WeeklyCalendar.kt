package com.elshan.shiftnoc.presentation.screen.calendar.week

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.elshan.shiftnoc.presentation.calendar.CalendarEvent
import com.elshan.shiftnoc.presentation.calendar.AppState
import com.elshan.shiftnoc.presentation.components.getShiftType
import com.elshan.shiftnoc.presentation.screen.calendar.components.WeekDayComponent
import com.elshan.shiftnoc.util.DIALOGS
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import java.time.LocalDate

@Composable
fun WeeklyCalendar(
    appState: AppState,
    onEvent: (CalendarEvent) -> Unit,
) {

    val currentDate = remember { LocalDate.now() }
    val startDate = remember { currentDate.minusDays(400) }
    val endDate = remember { currentDate.plusDays(400) }
    val weekState = rememberWeekCalendarState(
        startDate = startDate,
        endDate = endDate,
        firstDayOfWeek = appState.firstDayOfWeek
    )

    WeekCalendar(
        state = weekState,
        modifier = Modifier
            .fillMaxSize()

        ,
        dayContent = { day ->

            val shiftType = appState.startDate?.let {
                appState.selectedPattern?.let { pattern ->
                    getShiftType(
                        date = day.date,
                        workPattern = pattern,
                        startDate = it
                    )
                }
            }

            WeekDayComponent(
                day = day,
                shiftType = shiftType,
                appState = appState,
                onClick = {
                    appState.selectedDate = day.date
                    onEvent(CalendarEvent.ShowDialog(DIALOGS.ADD_EDIT_NOTE))
                }
            )
        }
    )
}