package com.wisnu.kurniawan.composetodolist.features.calendar.data

import com.wisnu.kurniawan.composetodolist.foundation.wrapper.DateTimeProvider
import com.wisnu.kurniawan.composetodolist.model.ToDoTask
import kotlinx.coroutines.flow.Flow

interface ICalendarEnvironment {
    val dateTimeProvider: DateTimeProvider
    fun loadTasksByCreatedDate(): Flow<List<CalendarTaskItem>>
    fun loadTasksByCompletedDate(): Flow<List<CalendarTaskItem>>
    suspend fun toggleTaskStatus(task: ToDoTask)
    suspend fun deleteTask(task: ToDoTask)
}
