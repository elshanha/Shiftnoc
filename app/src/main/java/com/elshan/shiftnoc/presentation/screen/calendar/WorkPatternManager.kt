package com.elshan.shiftnoc.presentation.screen.calendar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.PopupProperties
import com.elshan.shiftnoc.R
import com.elshan.shiftnoc.presentation.calendar.AppState
import com.elshan.shiftnoc.presentation.calendar.CalendarEvent
import com.elshan.shiftnoc.presentation.calendar.VacationDialog
import com.elshan.shiftnoc.presentation.components.ShiftType
import com.elshan.shiftnoc.presentation.components.WorkPattern
import com.elshan.shiftnoc.presentation.components.getName
import com.elshan.shiftnoc.ui.theme.ShiftnocTheme
import com.elshan.shiftnoc.util.DIALOGS
import com.google.firebase.crashlytics.FirebaseCrashlytics

@Composable
fun WorkPatternManager(
    selectedPattern: WorkPattern,
    predefinedPatterns: List<WorkPattern>,
    customPatterns: List<WorkPattern>,
    appState: AppState,
    onEvent: (CalendarEvent) -> Unit,
) {
    if (appState.visibleDialogs.contains(DIALOGS.CUSTOM_WORK_PATTERN)) {
        CustomWorkPatternDialog(
            appState = appState,
            onEvent = onEvent
        )
    }
    if (appState.visibleDialogs.contains(DIALOGS.VACATION_DIALOG)) {
        VacationDialog(
            appState = appState,
            onEvent = onEvent
        )
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.predefined_patterns),
        stringResource(R.string.custom_patterns)
    )

    AlertDialog(
        containerColor = MaterialTheme.colorScheme.onPrimary,
        titleContentColor = MaterialTheme.colorScheme.primary,
        textContentColor = MaterialTheme.colorScheme.primary,
        onDismissRequest = {
            onEvent(CalendarEvent.HideDialog(DIALOGS.WORK_PATTERN_MANAGER))
        },
        confirmButton = {
            OutlinedButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 0.dp,
                        start = 12.dp,
                        end = 12.dp,
                        bottom = 0.dp
                    ),
                onClick = {
                    onEvent(CalendarEvent.ShowDialog(DIALOGS.VACATION_DIALOG))
                },
            ) {
                Text(stringResource(R.string.add_vacations))
            }
        },
        title = { Text(stringResource(R.string.select_work_pattern)) },
        text = {
            Column {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.onPrimary,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    tabs.forEachIndexed { index, tab ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            selectedContentColor = MaterialTheme.colorScheme.primary,
                            unselectedContentColor = MaterialTheme.colorScheme.outline,
                            text = { Text(tab) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                when (selectedTab) {
                    0 -> {
                        predefinedPatterns.forEach { pattern ->
                            PatternRow(
                                pattern = pattern,
                                isSelected = pattern == selectedPattern,
                                onEvent = onEvent,
                                appState = appState
                            )
                        }
                    }

                    1 -> {
                        customPatterns.forEach { pattern ->
                            PatternRow(
                                pattern = pattern,
                                isSelected = pattern == selectedPattern,
                                onEvent = onEvent,
                                appState = appState
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        if (customPatterns.size == 3) {
                            Text(
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                text = stringResource(R.string.you_can_add_up_to_3_custom_patterns),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.error
                                )
                            )
                        }
                        OutlinedButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    top = 12.dp,
                                    start = 12.dp,
                                    end = 12.dp,
                                    bottom = 0.dp
                                ),
                            onClick = {
                                try {
                                    onEvent.apply {
                                        this(CalendarEvent.SetSelectedPattern(null))
                                        this(CalendarEvent.ShowDialog(DIALOGS.CUSTOM_WORK_PATTERN_DIALOG))
                                    }
                                } catch (e: Exception) {
                                    FirebaseCrashlytics.getInstance().recordException(e)
                                }

                            },
                            enabled = customPatterns.size < 3
                        ) {
                            Text(stringResource(R.string.add_custom_pattern))
                        }
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PatternRow(
    appState: AppState,
    pattern: WorkPattern,
    isSelected: Boolean,
    onEvent: (CalendarEvent) -> Unit
) {
    if (appState.visibleDialogs.contains(DIALOGS.EDIT_OR_DELETE_DIALOG)) {
        EditOrDeleteDialog(onEvent = onEvent, appState = appState)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .combinedClickable(
                onLongClick = {
                    onEvent(CalendarEvent.SetSelectedPattern(pattern))
                    onEvent(CalendarEvent.ShowDialog(DIALOGS.EDIT_OR_DELETE_DIALOG))
                },
                onClick = {
                    onEvent(CalendarEvent.OnWorkPatternSelected(pattern))
                    onEvent(CalendarEvent.HideDialog(DIALOGS.WORK_PATTERN_MANAGER))
                }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = {
                onEvent(CalendarEvent.OnWorkPatternSelected(pattern))
                onEvent(CalendarEvent.HideDialog(DIALOGS.WORK_PATTERN_MANAGER))
            }
        )
        Text(
            text = if (pattern.isCustom) {
                pattern.name
            } else {
                stringResource(pattern.nameResId)
            },
            style = MaterialTheme.typography.bodyMedium
        )
    }
}


@Composable
fun CustomWorkPatternDialog(
    appState: AppState,
    onEvent: (CalendarEvent) -> Unit,
) {
    var patternName by remember { mutableStateOf(appState.selectedPattern?.name ?: "") }
    var shifts by remember {
        mutableStateOf(
            appState.selectedPattern?.pattern ?: emptyList()
        )
    }
    var selectedShiftType by remember { mutableStateOf(ShiftType.MORNING) }
    var shiftTypeMenuExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current

    AlertDialog(
        containerColor = MaterialTheme.colorScheme.onPrimary,
        titleContentColor = MaterialTheme.colorScheme.primary,
        textContentColor = MaterialTheme.colorScheme.primary,
        onDismissRequest = {
            onEvent(CalendarEvent.HideDialog(DIALOGS.CUSTOM_WORK_PATTERN))
        },
        confirmButton = {
            OutlinedButton(
                enabled = patternName.isNotEmpty(),
                onClick = {
                    val newPattern = WorkPattern(
                        name = patternName,
                        pattern = shifts,
                    )
                    if (appState.selectedPattern != null) {
                        onEvent(
                            CalendarEvent.EditCustomWorkPattern(
                                appState.selectedPattern,
                                newPattern
                            )
                        )

                    } else {
                        onEvent(CalendarEvent.OnCustomWorkPatternAdded(newPattern))
                    }
                    onEvent(CalendarEvent.HideDialog(DIALOGS.CUSTOM_WORK_PATTERN))
                }) {
                Text(
                    text = if (appState.selectedPattern != null) stringResource(R.string.edit_pattern) else stringResource(
                        R.string.add_pattern
                    )
                )
            }
        },
        dismissButton = {
            OutlinedButton(onClick = {
                onEvent(CalendarEvent.HideDialog(DIALOGS.CUSTOM_WORK_PATTERN))
            }) {
                Text(stringResource(R.string.cancel))
            }
        },
        title = {
            Text(
                text = if (appState.selectedPattern != null) stringResource(R.string.edit_pattern) else stringResource(
                    R.string.add_custom_pattern
                )
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                    ),
                    shape = RoundedCornerShape(10.dp),
                    value = patternName,
                    onValueChange = { patternName = it },
                    label = { Text(stringResource(R.string.pattern_name)) },
                    colors = OutlinedTextFieldDefaults.colors().copy(
                        focusedTextColor = MaterialTheme.colorScheme.secondary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Box {
                        Button(
                            onClick = { shiftTypeMenuExpanded = true }) {
                            Text(
                                stringResource(selectedShiftType.stringResId),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        DropdownMenu(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .background(
                                    MaterialTheme.colorScheme.onPrimary,
                                ),
                            expanded = shiftTypeMenuExpanded,
                            onDismissRequest = { shiftTypeMenuExpanded = false },
                            properties = PopupProperties(
                                dismissOnBackPress = true,
                                dismissOnClickOutside = true,
                            )
                        ) {
                            ShiftType.entries.forEach { shift ->
                                DropdownMenuItem(
                                    colors = MenuDefaults.itemColors().copy(
                                        textColor = MaterialTheme.colorScheme.primary
                                    ),
                                    text = { Text(stringResource(shift.stringResId)) },
                                    onClick = {
                                        selectedShiftType = shift
                                        shiftTypeMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    if (shifts.isNotEmpty()) {
                        IconButton(onClick = { shifts = emptyList() }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = stringResource(R.string.clear_shifts)
                            )
                        }
                    }
                    Button(onClick = { shifts = shifts + selectedShiftType }) {
                        Text(
                            stringResource(R.string.add_shift),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    stringResource(
                        R.string.pattern_shifts,
                        shifts.joinToString(", ") { it.getName(context) }
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    )
}

@Composable
fun EditOrDeleteDialog(
    onEvent: (CalendarEvent) -> Unit,
    appState: AppState
) {

    val pattern = appState.selectedPattern ?: return

    AlertDialog(
        containerColor = MaterialTheme.colorScheme.onPrimary,
        titleContentColor = MaterialTheme.colorScheme.primary,
        textContentColor = MaterialTheme.colorScheme.primary,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        ),
        onDismissRequest = {
            onEvent(CalendarEvent.HideDialog(DIALOGS.EDIT_OR_DELETE_DIALOG))
        },
        confirmButton = {  },
        title = { Text(text = "Edit or Delete Pattern") },
        text = {
            Column {
                OutlinedButton(
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                        .fillMaxWidth(0.7f),
                    onClick = {
                        onEvent(CalendarEvent.ShowDialog(DIALOGS.CUSTOM_WORK_PATTERN))
                        onEvent(CalendarEvent.UpdateCustomWorkPattern(pattern, pattern))
                        onEvent(CalendarEvent.HideDialog(DIALOGS.EDIT_OR_DELETE_DIALOG))
                    }) {
                    Text(text = "Edit")
                }
                OutlinedButton(
                    modifier = Modifier
                        .fillMaxWidth(0.7f),
                    onClick = {
                        onEvent(CalendarEvent.RemoveCustomWorkPattern(pattern))
                        onEvent(CalendarEvent.HideDialog(DIALOGS.EDIT_OR_DELETE_DIALOG))
                    }) {
                    Text(text = "Delete")
                }
            }
        }
    )
}

@Preview
@Composable
fun WorkPatternManagerPreview() {
    ShiftnocTheme {
        WorkPatternManager(
            selectedPattern = WorkPattern(
                name = "Work Pattern",
                pattern = listOf(ShiftType.MORNING, ShiftType.NIGHT)
            ),
            predefinedPatterns = listOf(
                WorkPattern(
                    name = "Work Pattern",
                    pattern = listOf(ShiftType.OFF)
                ),
                WorkPattern(
                    name = "Work Pattern 2",
                    pattern = listOf(ShiftType.MORNING, ShiftType.NIGHT)
                )
            ),
            customPatterns = listOf(
                WorkPattern(
                    name = "Work Pattern 3",
                    pattern = listOf(ShiftType.MORNING, ShiftType.NIGHT)
                ),
                WorkPattern(
                    name = "Work Pattern 4",
                    pattern = listOf(ShiftType.MORNING, ShiftType.NIGHT)
                )
            ),
            onEvent = {},
            appState = AppState()
        )
    }
}








