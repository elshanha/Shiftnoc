package com.elshan.shiftnoc.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate = LocalDate.now(),
    val content: String,
    val reminder: LocalDateTime? = null,
    val color: String = "#FFFFFF",
)

