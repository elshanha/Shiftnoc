package com.elshan.shiftnoc.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes")
    suspend fun getAll(): List<NoteEntity>

    @Upsert
    suspend fun upsertNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()

    @Query("SELECT * FROM notes WHERE date = :date")
    suspend fun getNotesForDate(date: LocalDate): List<NoteEntity>
}


class Converters {
    @TypeConverter
    fun fromString(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }

    @TypeConverter
    fun toString(value: LocalDate?): String? {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return value?.format(formatter)
    }

    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): Long? {
        return dateTime?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
    }

    @TypeConverter
    fun toLocalDateTime(dateTimeLong: Long?): LocalDateTime? {
        return dateTimeLong?.let {
            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDateTime()
        }
    }
}