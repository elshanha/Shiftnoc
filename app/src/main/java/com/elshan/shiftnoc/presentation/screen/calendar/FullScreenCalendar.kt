package com.elshan.shiftnoc.presentation.screen.calendar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.elshan.shiftnoc.presentation.calendar.AppState
import com.elshan.shiftnoc.presentation.calendar.CalendarEvent
import com.elshan.shiftnoc.presentation.components.CustomMonthHeader
import com.elshan.shiftnoc.presentation.components.DatePickerDayComponent
import com.elshan.shiftnoc.presentation.components.Header
import com.elshan.shiftnoc.presentation.components.MonthBody
import com.elshan.shiftnoc.presentation.components.MonthHeader
import com.elshan.shiftnoc.presentation.components.getShiftType
import com.elshan.shiftnoc.presentation.datastore.UserPreferencesRepository
import com.elshan.shiftnoc.presentation.screen.calendar.components.ActionsSection
import com.elshan.shiftnoc.presentation.screen.calendar.components.MonthDayComponent
import com.elshan.shiftnoc.presentation.screen.calendar.week.WeeklyCalendar
import com.elshan.shiftnoc.presentation.screen.note.AddEditNoteDialog
import com.elshan.shiftnoc.util.CalendarView
import com.elshan.shiftnoc.util.DIALOGS
import com.elshan.shiftnoc.util.bottomWindowInsetsPadding
import com.elshan.shiftnoc.util.displayText
import com.elshan.shiftnoc.util.endWindowInsetsPadding
import com.elshan.shiftnoc.util.rememberFirstVisibleMonthAfterScroll
import com.elshan.shiftnoc.util.startWindowInsetsPadding
import com.elshan.shiftnoc.util.updateLocale
import com.kizitonwose.calendar.compose.ContentHeightMode
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.VerticalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.OutDateStyle
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.yearMonth
import java.time.LocalDate
import java.time.Year
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenCalendar(
    onEvent: (CalendarEvent) -> Unit,
    appState: AppState,
    navController: NavController
) {

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val dataPreference = UserPreferencesRepository(context = context)
        dataPreference.languagePreference.collect {
            updateLocale(context, it)
        }
    }

    val selectedMonth by remember {
        derivedStateOf {
            appState.startDate?.yearMonth ?: LocalDate.now().yearMonth
        }
    }
    val currentMonth = remember { selectedMonth }
    val startMonth = remember { currentMonth.minusMonths(50) }
    val endMonth = remember { currentMonth.plusMonths(200) }
    val daysOfWeek = remember {
        daysOfWeek(
            appState.firstDayOfWeek
        )
    }

    val monthState = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = appState.firstDayOfWeek,
        outDateStyle = OutDateStyle.EndOfRow
    )


    if (appState.visibleDialogs.contains(DIALOGS.ADD_EDIT_NOTE)) {
        AddEditNoteDialog(
            appState = appState,
            onEvent = onEvent,
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {


            AnimatedVisibility(
                visible = appState.calendarView == CalendarView.HORIZONTAL_MONTHLY,
                enter = slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = spring(dampingRatio = 0.9f)
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    val monthTitle = if (monthState.lastVisibleMonth.yearMonth.year == Year.now().value) {
                        monthState.lastVisibleMonth.yearMonth.month.displayText().replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                        }
                    } else {
                        monthState.lastVisibleMonth.yearMonth.displayText().replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                        }
                    }
                    CenterAlignedTopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            titleContentColor = MaterialTheme.colorScheme.primary
                        ),
                        title = {
                            Text(
                                text = monthTitle,
                                style = MaterialTheme.typography.headlineLarge,
                            )
                        },
                    )
                    CustomMonthHeader(daysOfWeek = daysOfWeek)

                    HorizontalCalendar(

                        state = monthState,
                        contentHeightMode = ContentHeightMode.Wrap,
                        calendarScrollPaged = true,
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

                            MonthDayComponent(
                                day = day,
                                shiftType = shiftType,
                                appState = appState,
                                onClick = {
                                    appState.selectedDate = day.date
                                    onEvent(
                                        CalendarEvent.ShowDialog(
                                            DIALOGS.ADD_EDIT_NOTE
                                        )
                                    )
                                }
                            )
                        },
                    )
                }
            }

            Header(
                modifier = Modifier.padding(
                    horizontal = if (appState.calendarView == CalendarView.VERTICAL_MONTHLY) 20.dp else 0.dp
                ),
                daysOfWeek = daysOfWeek
            )

            AnimatedVisibility(
                visible = appState.calendarView == CalendarView.WEEKLY,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ),
            ) {
                WeeklyCalendar(
                    appState = appState,
                    onEvent = onEvent
                )
            }

            AnimatedVisibility(
                visible = appState.calendarView == CalendarView.VERTICAL_MONTHLY,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            ) {

                VerticalCalendar(
                    modifier = Modifier.fillMaxSize(),
                    state = monthState,
                    calendarScrollPaged = false,
                    contentHeightMode = ContentHeightMode.Wrap,
                    contentPadding = PaddingValues(
                        top = 20.dp,
                        bottom = 20.dp + bottomWindowInsetsPadding(),
                        start = 20.dp + startWindowInsetsPadding(),
                        end = 20.dp + endWindowInsetsPadding()
                    ),
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

                        MonthDayComponent(
                            day = day,
                            shiftType = shiftType,
                            appState = appState,
                            onClick = {
                                appState.selectedDate = day.date
                                onEvent(
                                    CalendarEvent.ShowDialog(
                                        DIALOGS.ADD_EDIT_NOTE
                                    )
                                )
                            }
                        )
                    },
                    monthHeader = { month -> MonthHeader(month) },
                    monthBody = { _, content -> MonthBody(content) },
                )
            }
        }



        ActionsSection(
            navController = navController,
            appState = appState,
            onEvent = onEvent,
            modifier = Modifier
                .align(Alignment.BottomCenter)

        )
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FullScreenCalendarPreview() {
//    FullScreenCalendar(navController = NavController(context = LocalContext.current))
}