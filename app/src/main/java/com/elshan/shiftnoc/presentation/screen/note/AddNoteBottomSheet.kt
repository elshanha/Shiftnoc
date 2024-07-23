package com.elshan.shiftnoc.presentation.screen.note

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elshan.shiftnoc.presentation.calendar.AppState
import com.elshan.shiftnoc.presentation.calendar.CalendarEvent
import com.elshan.shiftnoc.ui.theme.ShiftnocTheme
import com.elshan.shiftnoc.util.DIALOGS
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AddNoteBottomSheet(
    onEvent: (CalendarEvent) -> Unit,
    appState: AppState
) {

    var selectedNote by remember { mutableStateOf(appState.noteEntity) }
    var content by remember { mutableStateOf(selectedNote?.content ?: "") }
    val date = appState.selectedDate ?: LocalDate.now()
    var reminder by remember { mutableStateOf(selectedNote?.reminder) }
    var color by remember { mutableStateOf((selectedNote?.color) ?: "#FFFFFF") }

    val existingNotes = appState.notes[date] ?: emptyList()
    val maxNotesReached = existingNotes.size >= 3

    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        containerColor = MaterialTheme.colorScheme.onPrimary,
        onDismissRequest = {
            onEvent(CalendarEvent.HideDialog(DIALOGS.SHOW_BOTTOM_SHEET))
        },
        sheetState = sheetState,
        dragHandle = {
            BottomSheetDefaults.DragHandle()
        }
    ) {

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
    }
}


@Preview
@Composable
fun AddNoteBottomSheetPreview() {
    ShiftnocTheme {
        AddNoteBottomSheet(
            onEvent = {},
            appState = AppState()
        )
    }
}