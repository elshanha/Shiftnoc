package com.elshan.shiftnoc.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [NoteEntity::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class NoteDataBase : RoomDatabase() {

    abstract fun noteDao(): NoteDao

}