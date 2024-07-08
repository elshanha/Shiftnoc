package com.elshan.shiftnoc.presentation.calendar

import com.elshan.shiftnoc.data.local.NoteEntity
import java.time.LocalDate
import java.time.LocalDateTime

data class NoteState(
    val note: NoteEntity?,
    val date: LocalDate,
    val content: String = note?.content ?: "",
    val reminder: LocalDateTime? = note?.reminder,
    val color: String = note?.color ?: "#FFFFFF"
) {
    val isNew: Boolean
        get() = note == null


    fun copyFrom(note: NoteEntity): NoteState {
        return copy(
            note = note,
            content = note.content,
            reminder = note.reminder,
            color = note.color
        )
    }

    fun toNoteEntity(): NoteEntity {
        return note?.copy(content = content, reminder = reminder, date = date, color = color)
            ?: NoteEntity(date = date, reminder = reminder, content = content, color = color)
    }
}