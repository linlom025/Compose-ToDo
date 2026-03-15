package com.wisnu.kurniawan.composetodolist.features.todo.main.data

import com.wisnu.kurniawan.composetodolist.foundation.wrapper.DateTimeProvider
import com.wisnu.kurniawan.composetodolist.foundation.wrapper.IdProvider
import com.wisnu.kurniawan.composetodolist.model.TaskQuadrant
import com.wisnu.kurniawan.composetodolist.model.ToDoTask
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface IToDoMainEnvironment {
    val idProvider: IdProvider
    val dateTimeProvider: DateTimeProvider
    fun loadQuadrantTasks(): Flow<List<QuadrantTask>>
    suspend fun ensureQuadrantSystemLists()
    suspend fun migrateUncompletedTaskListIdToQuadrantList()
    suspend fun createTaskInQuadrant(
        taskName: String,
        quadrant: TaskQuadrant,
        dueDate: LocalDateTime?,
        isDueDateTimeSet: Boolean,
        note: String,
    )
    suspend fun toggleTaskStatus(task: ToDoTask)
    suspend fun deleteTask(task: ToDoTask)
}
