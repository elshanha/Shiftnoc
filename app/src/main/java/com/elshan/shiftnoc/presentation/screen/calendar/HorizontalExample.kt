package com.elshan.shiftnoc.presentation.screen.calendar

//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.AccountCircle
//import androidx.compose.material.icons.filled.DateRange
//import androidx.compose.material.icons.filled.Menu
//import androidx.compose.material3.CenterAlignedTopAppBar
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.LocalContentColor
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.material3.TopAppBarDefaults
//import androidx.compose.material3.darkColorScheme
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.CompositionLocalProvider
//import androidx.compose.runtime.mutableStateListOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.testTag
//import androidx.compose.ui.res.colorResource
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import com.kizitonwose.calendar.compose.rememberCalendarState
//import com.kizitonwose.calendar.core.CalendarDay
//import com.kizitonwose.calendar.core.DayPosition
//import com.kizitonwose.calendar.core.OutDateStyle
//import com.kizitonwose.calendar.core.daysOfWeek
//import com.kizitonwose.calendar.core.yearMonth
//import java.time.LocalDate
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun Example8Page(
//    horizontal: Boolean = true,
//) {
//    val today = remember { LocalDate.now() }
//    val currentMonth = remember(today) { today.yearMonth }
//    val startMonth = remember { currentMonth.minusMonths(500) }
//    val endMonth = remember { currentMonth.plusMonths(500) }
//    val selections = remember { mutableStateListOf<CalendarDay>() }
//    val daysOfWeek = remember { daysOfWeek() }
//    StatusBarColorUpdateEffect(color = colorResource(id = R.color.example_1_bg_light))
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(MaterialTheme.colorScheme.background)
//            .padding(top = 20.dp),
//    ) {
//
//        val state = rememberCalendarState(
//            startMonth = startMonth,
//            endMonth = endMonth,
//            firstVisibleMonth = currentMonth,
//            firstDayOfWeek = daysOfWeek.first(),
//            outDateStyle = OutDateStyle.EndOfGrid,
//        )
//        val scope = rememberCoroutineScope()
//        val visibleMonth = rememberFirstVisibleMonthAfterScroll(state)
//        // Draw light content on dark background.
//        CompositionLocalProvider(LocalContentColor provides darkColorScheme().onSurface) {
//            CenterAlignedTopAppBar(
//                colors = TopAppBarDefaults.topAppBarColors(colorResource(id = R.color.example_1_bg_light)),
//                title = {
//                    Text(text = visibleMonth.yearMonth.displayText(), color = Color.White)
//                },
//                navigationIcon = {
//                    IconButton(onClick = {
//
//                    }) {
//                        Icon(
//                            tint = Color.White,
//                            imageVector = Icons.Default.Menu,
//                            contentDescription = null,
//                        )
//                    }
//                },
//                actions = {
//                    IconButton(onClick = { }) {
//                        Icon(
//                            imageVector = Icons.Default.DateRange,
//                            contentDescription = null,
//                            tint = Color.White
//                        )
//                    }
//                    IconButton(onClick = { }) {
//                        Icon(
//                            imageVector = Icons.Default.AccountCircle,
//                            contentDescription = null,
//                            tint = Color.White
//                        )
//                    }
//                }
//            )
//            FullScreenCalendar(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .background(colorResource(id = R.color.example_1_bg))
//                    .testTag("Calendar"),
//                state = state,
//                horizontal = horizontal,
//                dayContent = { day ->
//                    ShiftDay(
//                        day = day,
//                        isSelected = selections.contains(day),
//                        isToday = day.position == DayPosition.MonthDate && day.date == today,
//                    ) { calendarDay ->
//
//
//                        if (selections.contains(calendarDay)) {
//                            selections.remove(calendarDay)
//                        } else {
//                            selections.add(calendarDay)
//                        }
//                    }
//                },
//                // The month body is only needed for ui test tag.
//                monthBody = { _, content ->
//                    Box(
//                        modifier = Modifier
//                            .weight(1f)
//                            .testTag("MonthBody"),
//                    ) {
//                        content()
//                    }
//                },
//                monthHeader = {
//                    MonthHeader(daysOfWeek = daysOfWeek)
//                },
//                monthFooter = { month ->
//                    val count = month.weekDays.flatten()
//                        .count { selections.contains(it) }
//                    MonthFooter(selectionCount = count)
//                },
//            )
//        }
//    }
//}
//
//
//@Preview(showBackground = true)
//@Composable
//private fun Example8Preview() {
//    //  Example8Page()
//}