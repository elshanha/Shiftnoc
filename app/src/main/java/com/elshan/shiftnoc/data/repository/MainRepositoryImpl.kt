package com.elshan.shiftnoc.data.repository

import com.elshan.shiftnoc.data.local.NoteDataBase
import com.elshan.shiftnoc.data.local.NoteEntity
import com.elshan.shiftnoc.domain.repository.MainRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import javax.inject.Inject

class MainRepositoryImpl(
    noteDb: NoteDataBase
) : MainRepository {

    private val noteDao = noteDb.noteDao()

    override suspend fun getNotesForDate(date: LocalDate): Flow<List<NoteEntity>> {
        return flow {
            val notes = noteDao.getNotesForDate(date)
            emit(notes)
            return@flow
        }
    }

    override suspend fun upsertNote(note: NoteEntity) {
        noteDao.upsertNote(note)
    }

    override suspend fun deleteNote(note: NoteEntity) {
        noteDao.deleteNote(note)
    }

    override suspend fun deleteAllNotes() {
        noteDao.deleteAllNotes()
    }

    override suspend fun getAllNotes(): Flow<List<NoteEntity>> {
        return flow {
            val notes = noteDao.getAll()
            emit(notes)
            return@flow
        }
    }
}