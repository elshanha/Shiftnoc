package com.elshan.shiftnoc.di

import com.elshan.shiftnoc.data.repository.MainRepositoryImpl
import com.elshan.shiftnoc.domain.repository.MainRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindCalendarRepository(
        mainRepositoryImpl: MainRepositoryImpl
    ): MainRepository

}