package com.wisnu.kurniawan.composetodolist.features.calendar.di

import com.wisnu.kurniawan.composetodolist.features.calendar.data.CalendarEnvironment
import com.wisnu.kurniawan.composetodolist.features.calendar.data.ICalendarEnvironment
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class CalendarModule {

    @Binds
    abstract fun provideEnvironment(
        environment: CalendarEnvironment
    ): ICalendarEnvironment
}
