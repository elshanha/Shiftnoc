package com.elshan.shiftnoc.presentation.screen.details

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.elshan.shiftnoc.R
import com.elshan.shiftnoc.data.local.NoteEntity
import com.elshan.shiftnoc.presentation.calendar.AppState
import com.elshan.shiftnoc.presentation.calendar.CalendarEvent
import com.elshan.shiftnoc.presentation.components.DayType
import com.elshan.shiftnoc.presentation.components.IncomeCalculations
import com.elshan.shiftnoc.presentation.components.ListOfDays
import com.elshan.shiftnoc.presentation.components.defaultWorkPattern
import com.elshan.shiftnoc.ui.theme.ShiftnocTheme
import com.elshan.shiftnoc.util.DIALOGS
import com.elshan.shiftnoc.util.displayText
import com.elshan.shiftnoc.util.truncateDecimalPlaces
import com.kizitonwose.calendar.core.CalendarMonth
import java.time.LocalDate
import java.time.Month
import java.time.Year
import java.time.format.DateTimeFormatter

@Composable
fun DetailsScreen(
    title: String,
    appState: AppState,
    onEvent: (CalendarEvent) -> Unit = {}
) {

    Column(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.background
            ),

        ) {
        Text(
            modifier = Modifier
                .padding(bottom = 12.dp)
                .align(Alignment.CenterHorizontally),
            text = title, style = MaterialTheme.typography.headlineLarge.copy(
                color = MaterialTheme.colorScheme.primary
            )
        )
        MonthDetailsCard(appState)
        MonthlyIncomeCard(appState)
        MonthlyNotesCard(
            appState = appState,
            onEvent = onEvent
        )
    }
}


@Composable
fun MonthDetailsCard(
    appState: AppState
) {

    val incomeClass = IncomeCalculations(appState)
    val listOfDays = ListOfDays(appState)

    listOfDays.getListOfDaysPerMonth(
        year = appState.selectedMonth.yearMonth.year,
        month = appState.selectedMonth.yearMonth.month
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .background(
                MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.large
            )
    ) {
        val count = incomeClass.countDayTypesInMonth(
            year = appState.selectedMonth.yearMonth.year,
            month = appState.selectedMonth.yearMonth.month
        )
        count.entries.forEach { (dayType, count) ->

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = dayType.stringResId),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(text = count.toString(), color = MaterialTheme.colorScheme.primary)
            }
        }
    }

}

@Composable
fun MonthlyIncomeCard(
    appState: AppState
) {
    val incomeClass = IncomeCalculations(appState)
    val dayCount = incomeClass.countDayTypesInMonth(
        year = appState.selectedMonth.yearMonth.year,
        month = appState.selectedMonth.yearMonth.month
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .background(
                MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.large
            )
    ) {

        val income = incomeClass.calculateMonthlyIncome(
            mornings = dayCount[DayType.WORK_MORNING] ?: 0,
            nights = dayCount[DayType.WORK_NIGHT] ?: 0,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(R.string.income), color = MaterialTheme.colorScheme.primary)
            Text(
                text = truncateDecimalPlaces(income.toString()),
                color = MaterialTheme.colorScheme.primary
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = "info",
                tint = MaterialTheme.colorScheme.surface
            )
            Text(
                text = stringResource(R.string.there_can_be_miscalculation_please_consider_that),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.surface
                )
            )
        }

    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MonthlyNotesCard(
    appState: AppState,
    onEvent: (CalendarEvent) -> Unit
) {
    val yearMonth = appState.selectedMonth.yearMonth
    val firstDayOfMonth = yearMonth.atDay(1)
    val lastDayOfMonth = yearMonth.atEndOfMonth()

    // Collect notes for the entire month
    val monthlyNotes = mutableMapOf<LocalDate, List<NoteEntity>>()
    var currentDate = firstDayOfMonth
    while (currentDate <= lastDayOfMonth) {
        val dailyNotes = appState.notes[currentDate] ?: emptyList()
        if (dailyNotes.isNotEmpty()) {
            monthlyNotes[currentDate] = dailyNotes
        }
        currentDate = currentDate.plusDays(1)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(
                min = 50.dp,
                max = 300.dp
            )
            .padding(top = 12.dp)
            .background(
                MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.large
            )
    ) {
        if (monthlyNotes.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically),
            ) {

                monthlyNotes.forEach { (date, notesForDay) ->
                    item {
                        // Date header
                        Text(
                            modifier = Modifier
                                .padding(bottom = 4.dp, start = 18.dp),
                            text = date.format(DateTimeFormatter.ofPattern("dd MMMM")),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline,
                            thickness = 1.5.dp,
                            modifier = Modifier.padding(start = 18.dp, top = 4.dp)
                        )

                        notesForDay.forEach { note ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = {

                                        },
                                        onClickLabel = "Note",
                                        onLongClick = {
                                            onEvent(CalendarEvent.DeleteNote(note))
                                        },
                                        onLongClickLabel = "Delete note"
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .background(
                                            Color(android.graphics.Color.parseColor(note.color)),
                                            CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        modifier = Modifier
                                            .fillMaxWidth(0.8f),
                                        text = note.content,
                                        maxLines = if (appState.isFullScreen) Int.MAX_VALUE else 3,
                                        overflow = if (appState.isFullScreen) TextOverflow.Visible else TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    )
                                    Text(
                                        text = note.reminder?.format(
                                            DateTimeFormatter.ofPattern("HH:mm")
                                        ) ?: "",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Text(
                text = stringResource(R.string.no_notes_for_this_month),
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.tertiary
                ),
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
}


@Preview
@Composable
fun MonthDetailsCardPreview() {
    ShiftnocTheme {
        DetailsScreen(
            title = "Details",
            appState = AppState(),
        )
    }
}