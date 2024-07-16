package com.elshan.shiftnoc.presentation.screen.calendar.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarViewMonth
import androidx.compose.material.icons.outlined.CalendarViewWeek
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.elshan.shiftnoc.R
import com.elshan.shiftnoc.navigation.Screen
import com.elshan.shiftnoc.presentation.calendar.AppState
import com.elshan.shiftnoc.presentation.calendar.CalendarEvent
import com.elshan.shiftnoc.presentation.components.CustomButton
import com.elshan.shiftnoc.presentation.components.CustomDatePickerDialog
import com.elshan.shiftnoc.presentation.components.predefinedWorkPatterns
import com.elshan.shiftnoc.presentation.screen.calendar.WorkPatternManager
import com.elshan.shiftnoc.util.CalendarView
import com.elshan.shiftnoc.util.DIALOGS
import com.elshan.shiftnoc.util.horizontalWindowInsetsPadding
import com.elshan.shiftnoc.util.verticalWindowInsetsPadding
import java.time.LocalDate
import java.util.Locale


@Composable
fun ActionsSection(
    navController: NavController,
    appState: AppState,
    onEvent: (CalendarEvent) -> Unit,
    modifier: Modifier = Modifier
) {

    Box(
        modifier = modifier
            .horizontalWindowInsetsPadding()
            .verticalWindowInsetsPadding()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            AnimatedVisibility(
                visible = appState.visibleDialogs.contains(DIALOGS.SHOW_EXPANDABLE_BUTTONS),
                enter = slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ),
                exit = slideOutHorizontally(
                    targetOffsetX = {
                        it * 2
                    },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            ) {
                ExpandedActions(appState = appState, onEvent = onEvent)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {

                AnimatedVisibility(
                    visible = appState.visibleDialogs.contains(DIALOGS.SHOW_EXPANDABLE_BUTTONS_TO_LEFT),
                    enter = slideInHorizontally(
                        initialOffsetX = { -it / 2 },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ),

                    exit = slideOutHorizontally(
                        targetOffsetX = {
                            it
                        },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                ) {
                    ExpandableActionsToLeft(
                        navController = navController,
                        appState = appState,
                        onEvent = onEvent,
                        modifier = modifier
                            .fillMaxWidth(0.66f)
                    )
                }

                CustomButton(
                    modifier = Modifier
                        .fillMaxWidth(
                            if (appState.visibleDialogs.contains(DIALOGS.SHOW_EXPANDABLE_BUTTONS_TO_LEFT)) {
                                1f
                            } else {
                                0.33f
                            }
                        ),
                    text = stringResource(R.string.actions, Locale.getDefault()),
                    icon = if (appState.visibleDialogs.contains(DIALOGS.SHOW_EXPANDABLE_BUTTONS)) Icons.Rounded.ExpandMore else Icons.Outlined.ExpandLess
                ) {

                    onEvent(CalendarEvent.ToggleDialog(DIALOGS.SHOW_EXPANDABLE_BUTTONS))
                    onEvent(CalendarEvent.ToggleDialog(DIALOGS.SHOW_EXPANDABLE_BUTTONS_TO_LEFT))
                }

            }
        }

    }
}

@Composable
fun ExpandableActionsToLeft(
    navController: NavController,
    appState: AppState,
    onEvent: (CalendarEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    if (appState.visibleDialogs.contains(DIALOGS.SHOW_EXPANDABLE_BUTTONS_TO_LEFT)) {

        Row(
            modifier = modifier,
        ) {
            CustomButton(
                modifier = Modifier
                    .fillMaxWidth(0.5f),
                text = when (appState.calendarView) {
                    CalendarView.WEEKLY -> stringResource(R.string.monthly)
                    CalendarView.VERTICAL_MONTHLY -> stringResource(R.string.weekly)
                    CalendarView.HORIZONTAL_MONTHLY -> stringResource(R.string.monthly)
                },
                icon = if (appState.calendarView == CalendarView.WEEKLY) {
                    Icons.Outlined.CalendarViewMonth
                } else {
                    Icons.Outlined.CalendarViewWeek
                }
            ) {
                when (appState.calendarView) {
                    CalendarView.WEEKLY -> {
                        onEvent(CalendarEvent.OnCalendarViewChanged(CalendarView.HORIZONTAL_MONTHLY))
                    }

                    CalendarView.VERTICAL_MONTHLY -> {
                        onEvent(CalendarEvent.OnCalendarViewChanged(CalendarView.WEEKLY))
                    }

                    CalendarView.HORIZONTAL_MONTHLY -> {
                        onEvent(CalendarEvent.OnCalendarViewChanged(CalendarView.VERTICAL_MONTHLY))
                    }
                }
            }
            CustomButton(
                modifier = Modifier
                    .fillMaxWidth(),
                text = stringResource(R.string.settings),
                icon = Icons.Outlined.Settings
            ) {
                navController.navigate(Screen.Settings)
            }
        }
    }
}

@Composable
fun ExpandedActions(
    appState: AppState,
    onEvent: (CalendarEvent) -> Unit,
) {

    if (appState.visibleDialogs.contains(
            DIALOGS.WORK_PATTERN_MANAGER
        )
    ) {
        WorkPatternManager(
            selectedPattern = appState.selectedPattern ?: predefinedWorkPatterns.first(),
            predefinedPatterns = predefinedWorkPatterns,
            customPatterns = appState.customWorkPatterns,
            appState = appState,
            onEvent = onEvent,
        )
    }

    if (appState.visibleDialogs.contains(DIALOGS.SHOW_DAY_PICKER)) {
        CustomDatePickerDialog(
            appState = appState,
            defaultDay = appState.startDate ?: LocalDate.now(),
            onClose = { date ->
                onEvent.apply {
                    this(CalendarEvent.OnDateSelected(date))
                    this(CalendarEvent.HideDialog(DIALOGS.SHOW_DAY_PICKER))
                }
            }
        )
    }

    if (appState.calendarView == CalendarView.HORIZONTAL_MONTHLY) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            CustomButton(
                modifier = Modifier
                    .fillMaxWidth(0.33f)
                    .padding(bottom = 8.dp),
                text = stringResource(R.string.pattern),
                icon = Icons.Outlined.EditCalendar
            ) {
                onEvent(CalendarEvent.ShowDialog(DIALOGS.WORK_PATTERN_MANAGER))
            }
            CustomButton(
                modifier = Modifier
                    .fillMaxWidth(0.33f)
                    .padding(bottom = 8.dp),
                text = stringResource(R.string.date),
                icon = Icons.Outlined.DateRange
            ) {
                onEvent(CalendarEvent.ShowDialog(DIALOGS.SHOW_DAY_PICKER))
            }
            CustomButton(
                modifier = Modifier
                    .fillMaxWidth(0.33f)
                    .padding(bottom = 8.dp),
                text = stringResource(R.string.delete_all),
                icon = Icons.Rounded.DeleteForever
            ) {
                if (appState.notes.isNotEmpty()) {
                    onEvent(CalendarEvent.DeleteAllNotes)
                }
            }
        }
    } else {
        if (appState.visibleDialogs.contains(DIALOGS.DELETE_ALL_NOTES_DIALOG)) {
            DeleteAllNotesDialog(
                onEvent = onEvent
            )
        }

        Row(
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            CustomButton(
                modifier = Modifier
                    .fillMaxWidth(0.33f)
                    .padding(bottom = 8.dp),
                text = stringResource(R.string.pattern),
                icon = Icons.Outlined.EditCalendar
            ) {
                onEvent(CalendarEvent.ShowDialog(DIALOGS.WORK_PATTERN_MANAGER))
            }
            CustomButton(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .padding(bottom = 8.dp),
                text = stringResource(R.string.date),
                icon = Icons.Outlined.DateRange
            ) {
                onEvent(CalendarEvent.ShowDialog(DIALOGS.SHOW_DAY_PICKER))
            }
            CustomButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                text = stringResource(R.string.delete_all),
                icon = Icons.Rounded.DeleteForever
            ) {
                if (appState.notes.isNotEmpty()) {
                    onEvent(CalendarEvent.ShowDialog(DIALOGS.DELETE_ALL_NOTES_DIALOG))
                }
            }
        }
    }
}

@Composable
fun DeleteAllNotesDialog(
    onEvent: (CalendarEvent) -> Unit,
) {
    AlertDialog(
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        ),
        containerColor = MaterialTheme.colorScheme.onPrimary,
        titleContentColor = MaterialTheme.colorScheme.primary,
        textContentColor = MaterialTheme.colorScheme.primary,
        onDismissRequest = {
            onEvent(CalendarEvent.HideDialog(DIALOGS.DELETE_ALL_NOTES_DIALOG))
        },
        confirmButton = {
            TextButton(onClick = {
                onEvent.apply {
                    this(CalendarEvent.DeleteAllNotes)
                    this(CalendarEvent.HideDialog(DIALOGS.DELETE_ALL_NOTES_DIALOG))
                }
            }) {
                Text(stringResource(R.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onEvent(CalendarEvent.HideDialog(DIALOGS.DELETE_ALL_NOTES_DIALOG))
            }) {
                Text(stringResource(R.string.cancel))
            }
        },
        title = {
            Text(
                text = stringResource(R.string.delete_all_notes),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                text = stringResource(R.string.are_you_sure_you_want_to_delete_all_notes_this_action_cannot_be_undone),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    )
}


@Preview
@Composable
fun ExpandedActionsPreview() {
    ExpandedActions(
        appState = AppState(

        ),
        onEvent = {}
    )
}

@Preview
@Composable
fun ActionsSectionPreview() {
    ActionsSection(
        navController = NavController(androidx.compose.ui.platform.LocalContext.current),
        appState = AppState(),
        onEvent = {}
    )

}

@Preview
@Composable
fun ExpandableActionsToLeftPreview() {
    ExpandableActionsToLeft(
        navController = NavController(androidx.compose.ui.platform.LocalContext.current),
        appState = AppState(
            visibleDialogs = setOf(DIALOGS.SHOW_EXPANDABLE_BUTTONS_TO_LEFT)
        ),
        onEvent = {
        }
    )
}