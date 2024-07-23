package com.elshan.shiftnoc.presentation.calendar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring.DampingRatioLowBouncy
import androidx.compose.animation.core.Spring.StiffnessHigh
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.commandiron.wheel_picker_compose.WheelDatePicker
import com.elshan.shiftnoc.R
import com.elshan.shiftnoc.ui.theme.ShiftnocTheme
import com.elshan.shiftnoc.util.DIALOGS
import com.elshan.shiftnoc.util.enums.DateSort
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun VacationDialog(
    onEvent: (CalendarEvent) -> Unit,
    appState: AppState
) {
    val targetModifier =
        if (appState.visibleDialogs.contains(DIALOGS.VACATION_DATE_DIALOG)) Modifier.fillMaxSize() else Modifier

    val context = LocalContext.current
    AlertDialog(
        modifier = targetModifier,
        properties = DialogProperties(
            usePlatformDefaultWidth = !appState.visibleDialogs.contains(DIALOGS.VACATION_DATE_DIALOG),
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        ),
        containerColor = MaterialTheme.colorScheme.onPrimary,
        titleContentColor = MaterialTheme.colorScheme.primary,
        textContentColor = MaterialTheme.colorScheme.primary,
        onDismissRequest = {
            onEvent(CalendarEvent.HideDialog(DIALOGS.VACATION_DIALOG))
            onEvent(CalendarEvent.HideDialog(DIALOGS.VACATION_DATE_DIALOG))
        },
        confirmButton = {
            FilledTonalButton(
                onClick = {
                    if (appState.vacations.size == 6) {
                        onEvent(CalendarEvent.ShowToast(context.getString(R.string.max_vacations_reached)))
                        return@FilledTonalButton
                    }
                    val vacations = appState.vacations.toMutableList()
                    appState.selectedVacation?.let { vacations.add(it) }
                    onEvent(CalendarEvent.SaveVacations(vacations))
                    onEvent(CalendarEvent.HideDialog(DIALOGS.VACATION_DIALOG))
                    onEvent(CalendarEvent.HideDialog(DIALOGS.VACATION_DATE_DIALOG))
                },
                enabled = appState.selectedVacation.let {
                    it?.startDate?.isBefore(it.endDate) ?: false
                } || appState.vacations.size <= 6
            ) {
                Text(
                    text = stringResource(id = R.string.done),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onEvent(CalendarEvent.HideDialog(DIALOGS.VACATION_DIALOG))
                onEvent(CalendarEvent.HideDialog(DIALOGS.VACATION_DATE_DIALOG))
            }) {
                Text(
                    text = stringResource(id = R.string.dismiss),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        },
        title = {
            Text(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .clickable {
                        onEvent(CalendarEvent.ShowDialog(DIALOGS.SHOW_DAY_PICKER))
                    },
                text = stringResource(R.string.select_your_vacations),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                DateSelector(
                    dateSort = DateSort.VACATION_START,
                    onEvent = onEvent,
                    appState = appState
                )
                DateSelector(
                    dateSort = DateSort.VACATION_END,
                    onEvent = onEvent,
                    appState = appState
                )

                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 16.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.primary
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(appState.vacations) { vacation ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${vacation.startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}\n${
                                    vacation.endDate.format(
                                        DateTimeFormatter.ofPattern("dd/MM/yyyy")
                                    )
                                }",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${
                                        vacation.endDate.plusDays(+1)
                                            .toEpochDay() - vacation.startDate.toEpochDay()
                                    }",
                                    style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onPrimary),
                                )
                            }
                            IconButton(onClick = {
                                onEvent(CalendarEvent.DeleteVacation(vacation))
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete vacation")
                            }
                        }
                    }
                }
            }
        },
    )
}

@Composable
private fun ColumnScope.DateSelector(
    dateSort: DateSort,
    appState: AppState,
    onEvent: (CalendarEvent) -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val date = when (dateSort) {
        DateSort.VACATION_START -> appState.selectedVacation?.startDate ?: LocalDate.now()
        DateSort.VACATION_END -> appState.selectedVacation?.endDate ?: LocalDate.now()
        else -> null
    }
    val dateText = when (dateSort) {
        DateSort.VACATION_START -> stringResource(R.string.start_date)
        DateSort.VACATION_END -> stringResource(R.string.end_date)
        else -> ""
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            modifier = Modifier
                .padding(
                    end = if (dateSort == DateSort.VACATION_END) 8.dp else 0.dp
                ),
            text = dateText,
            style = MaterialTheme.typography.bodyLarge
        )
        VerticalDivider(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .height(20.dp),
            thickness = 2.dp,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = date?.format(dateFormatter) ?: LocalDate.now().format(dateFormatter),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .clickable {
                    onEvent(CalendarEvent.ToggleDialog(DIALOGS.VACATION_DATE_DIALOG))
                }
        )
    }
    AnimatedVisibility(
        visible = appState.visibleDialogs.contains(DIALOGS.VACATION_DATE_DIALOG),
        enter = slideInHorizontally(
            initialOffsetX = { it * 2 },
            animationSpec = spring(
                dampingRatio = DampingRatioLowBouncy,
                stiffness = StiffnessHigh
            )
        ),
        exit = slideOutHorizontally(
            targetOffsetX = { -it * 2 },
            animationSpec = spring(
                dampingRatio = DampingRatioLowBouncy,
                stiffness = StiffnessHigh
            )
        )
    ) {
        WheelDatePicker(
            modifier = Modifier
                .fillMaxWidth(),
            startDate = date ?: LocalDate.now(),
            onSnappedDate = { newDate ->
                onEvent(CalendarEvent.OnVacationSelected(newDate, dateSort))
            }
        )
    }
}


@Preview
@Composable
fun VacationDialogPreview() {
    ShiftnocTheme {
        VacationDialog(
            onEvent = {}, appState = AppState(
                vacations = listOf(
                    VacationDays(
                        startDate = LocalDate.now(),
                        endDate = LocalDate.now().plusDays(2),
                        description = "Vacation 1"
                    )
                )
            )
        )
    }
}



























