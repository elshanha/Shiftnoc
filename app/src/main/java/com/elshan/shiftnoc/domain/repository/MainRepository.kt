package com.elshan.shiftnoc.domain.repository

import com.elshan.shiftnoc.data.local.NoteEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface MainRepository {
    suspend fun getNotesForDate(date: LocalDate): Flow<List<NoteEntity>>
    suspend fun upsertNote(note: NoteEntity)
    suspend fun deleteNote(note: NoteEntity)
    suspend fun deleteAllNotes()
    suspend fun getAllNotes(): Flow<List<NoteEntity>>


}