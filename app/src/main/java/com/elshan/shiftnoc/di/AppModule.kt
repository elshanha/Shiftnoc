package com.elshan.shiftnoc.di

import android.app.AlarmManager
import android.content.Context
import androidx.room.Room
import com.elshan.shiftnoc.data.local.NoteDataBase
import com.elshan.shiftnoc.data.repository.MainRepositoryImpl
import com.elshan.shiftnoc.notification.local.NotificationsService
import com.elshan.shiftnoc.notification.local.NotificationsServiceImpl
import com.elshan.shiftnoc.presentation.datastore.UserPreferencesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideUserPreferencesRepository(
        @ApplicationContext context: Context
    ): UserPreferencesRepository = UserPreferencesRepository(context)

    @Provides
    @Singleton
    fun provideMainRepository(
        noteDataBase: NoteDataBase
    ) = MainRepositoryImpl(noteDataBase)

    @Provides
    @Singleton
    fun provideNoteDatabase(
        @ApplicationContext context: Context
    ): NoteDataBase {
        return Room.databaseBuilder(
            context,
            NoteDataBase::class.java,
            "note_db"
        )
            .build()
    }

    @Provides
    @Singleton
    fun provideNotificationManager(
        @ApplicationContext context: Context
    ): NotificationsService = NotificationsServiceImpl(context = context)

    @Provides
    fun provideAlarmManager(@ApplicationContext context: Context): AlarmManager {
        return context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }
}