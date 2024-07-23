package com.elshan.shiftnoc.presentation.screen.note

import android.app.AlarmManager
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateSizeAsState
import androidx.compose.animation.core.animateValueAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.OpenInFull
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarDuration
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.elshan.shiftnoc.R
import com.elshan.shiftnoc.data.local.NoteEntity
import com.elshan.shiftnoc.notification.NotificationsService
import com.elshan.shiftnoc.presentation.calendar.AppState
import com.elshan.shiftnoc.presentation.calendar.CalendarEvent
import com.elshan.shiftnoc.presentation.components.ColorPicker
import com.elshan.shiftnoc.presentation.components.CustomTimeDialog
import com.elshan.shiftnoc.util.DIALOGS
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

@OptIn(ExperimentalFoundationApi::class, ExperimentalFoundationApi::class)
@Composable
fun AddEditNoteDialog(
    appState: AppState,
    onEvent: (CalendarEvent) -> Unit,
) {
    var selectedNote by remember { mutableStateOf(appState.noteEntity) }
    var content by remember { mutableStateOf(selectedNote?.content ?: "") }
    val date = appState.selectedDate ?: LocalDate.now()
    var reminder by remember { mutableStateOf(selectedNote?.reminder) }
    var color by remember { mutableStateOf((selectedNote?.color) ?: "#FFFFFF") }

    val existingNotes = appState.notes[date] ?: emptyList()
    val maxNotesReached = existingNotes.size >= 3

    val targetModifier = if (appState.isFullScreen) Modifier.fillMaxSize() else Modifier

    AlertDialog(
        properties = DialogProperties(
            usePlatformDefaultWidth = !appState.isFullScreen
        ),
        modifier = targetModifier,
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        onDismissRequest = {
            onEvent(CalendarEvent.HideDialog(DIALOGS.ADD_EDIT_NOTE))
            onEvent(CalendarEvent.HideDialog(DIALOGS.TIME_PICKER))
            onEvent(CalendarEvent.HideDialog(DIALOGS.COLOR_PICKER))
        },
        title = {
            NoteDialogTitle(selectedNote, onEvent, appState)
        },
        text = {
            Column {
                AnimatedVisibility(
                    visible = appState.visibleDialogs.contains(DIALOGS.COLOR_PICKER),
                    enter = slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ),
                    exit = slideOutHorizontally(
                        targetOffsetX = { it * -2 },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
                ) {
                    ColorPicker(
                        appState = appState.copy(selectedColor = color, noteEntity = selectedNote),
                        onEvent = onEvent,
                        onColorSelected = { selectedColor ->
                            color = selectedColor
                            onEvent(CalendarEvent.HideDialog(DIALOGS.COLOR_PICKER))
                        },
                    )
                }

                AnimatedVisibility(
                    visible = appState.visibleDialogs.contains(DIALOGS.TIME_PICKER),
                    enter = slideInVertically() + scaleIn(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ),
                    exit = slideOutVertically() + fadeOut(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
                ) {
                    CustomTimeDialog(
                        onTimeSelected = { selectedTime ->
                            reminder = LocalDateTime.of(date, selectedTime)
                        },
                        initialTime = reminder?.toLocalTime() ?: LocalTime.now(),
                        onEvent = onEvent
                    )
                }

                ContentTextField(
                    content = content,
                    onContentChange = { content = it }
                )

                LazyColumn(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(existingNotes) { note ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {
                                        selectedNote = note
                                        content = note.content
                                        color = note.color
                                        reminder = note.reminder
                                    },
                                    onLongClick = {
                                        onEvent(CalendarEvent.DeleteNote(note))
                                    }
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
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = note.reminder?.format(
                                        java.time.format.DateTimeFormatter.ofPattern("HH:mm")
                                    ) ?: "",
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }
                        }
                    }
                }
                if (maxNotesReached && selectedNote == null) {
                    Text(
                        modifier = Modifier.padding(top = 8.dp),
                        text = stringResource(R.string.maximum_number_of_notes_reached_for_this_date),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (content.isNotEmpty() && (selectedNote != null || !maxNotesReached)) {
                        onEvent(
                            CalendarEvent.AddOrUpdateNote(
                                selectedNote?.copy(
                                    content = content,
                                    reminder = reminder,
                                    date = date,
                                    color = color
                                ) ?: NoteEntity(
                                    date = date,
                                    reminder = reminder,
                                    content = content,
                                    color = color
                                )
                            )
                        )
                        onEvent(CalendarEvent.HideDialog(DIALOGS.ADD_EDIT_NOTE))
                        onEvent(CalendarEvent.HideDialog(DIALOGS.TIME_PICKER))
                        onEvent(CalendarEvent.HideDialog(DIALOGS.COLOR_PICKER))
                    }
                },
                enabled = (selectedNote != null || !maxNotesReached)
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onEvent(CalendarEvent.HideDialog(DIALOGS.ADD_EDIT_NOTE))
                onEvent(CalendarEvent.HideDialog(DIALOGS.TIME_PICKER))
                onEvent(CalendarEvent.HideDialog(DIALOGS.COLOR_PICKER))
            }) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Composable
private fun NoteDialogTitle(
    selectedNote: NoteEntity?,
    onEvent: (CalendarEvent) -> Unit,
    appState: AppState
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val alarmManager = context.getSystemService(AlarmManager::class.java)
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val note = appState.notes.getOrDefault(appState.selectedDate, emptyList())
        .find { it.id == selectedNote?.id }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (note == null) stringResource(R.string.add_note) else stringResource(R.string.edit_note),
            style = MaterialTheme.typography.titleLarge.copy(
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        onEvent(CalendarEvent.ToggleDialog(DIALOGS.TIME_PICKER))
                    } else {
                        onEvent(
                            CalendarEvent.ShowSnackBar(
                                message = context.getString(R.string.please_grant_the_permission_in_the_settings),
                                actionLabel = context.getString(R.string.settings),
                                duration = SnackbarDuration.Short,
                                onAction = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        val intent =
                                            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                                putExtra(
                                                    Settings.EXTRA_APP_PACKAGE,
                                                    context.packageName
                                                )
                                            }
                                        context.startActivity(intent)
                                        scope.launch {
                                            delay(1000)
                                            Toast.makeText(
                                                context,
                                                context.getString(R.string.please_allow),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            )
                        )
                    }
                } else {
                    onEvent(CalendarEvent.ToggleDialog(DIALOGS.TIME_PICKER))
                }
                keyboardController?.hide()
                focusManager.clearFocus()
            },

            ) {
            Icon(
                imageVector = Icons.Default.Timer,
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = "Close"
            )
        }

        IconButton(onClick = {
            onEvent(CalendarEvent.ToggleDialog(DIALOGS.COLOR_PICKER))
            keyboardController?.hide()
            focusManager.clearFocus()
        }) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(
                        Color(android.graphics.Color.parseColor(note?.color ?: "#FFFFFF")),
                        shape = CircleShape
                    )
            )
        }

        IconButton(onClick = {
            onEvent(CalendarEvent.ToggleFullScreen)
            keyboardController?.hide()
            focusManager.clearFocus()
        }) {
            Icon(
                imageVector = if (appState.isFullScreen) Icons.Default.CloseFullscreen else Icons.Outlined.OpenInFull,
                contentDescription = "Toggle Full Screen",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ContentTextField(content: String, onContentChange: (String) -> Unit) {
    OutlinedTextField(
        value = content,
        onValueChange = {
            onContentChange(it)
        },
        placeholder = { Text(stringResource(R.string.note)) },
        modifier = Modifier
            .fillMaxWidth(),
        textStyle = TextStyle(
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        ),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors().copy(
            focusedTextColor = MaterialTheme.colorScheme.secondary,
            unfocusedTextColor = MaterialTheme.colorScheme.secondary,
        ),
        trailingIcon = {
            IconButton(onClick = {
                onContentChange("")
            }) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Default,
            capitalization = KeyboardCapitalization.Sentences
        ),
    )
}

@Preview()
@Composable
fun AddNotePreview() {
    AddEditNoteDialog(
        appState = AppState(
            selectedDate = LocalDate.now(),
            notes = mapOf(
                LocalDate.now() to listOf(
                    NoteEntity(
                        id = 1,
                        date = LocalDate.now(),
                        reminder = LocalDateTime.now(),
                        content = "Note 1",
                        color = "#FF5733"
                    ),

                    NoteEntity(
                        id = 2,
                        date = LocalDate.now(),
                        reminder = LocalDateTime.now(),
                        content = "Note 2",
                        color = "#FF9800"
                    )
                )
            )
        ),
        onEvent = {}
    )
}
