package com.wisnu.kurniawan.composetodolist.features.todo.detail.data

import com.wisnu.kurniawan.composetodolist.foundation.wrapper.DateTimeProvider
import com.wisnu.kurniawan.composetodolist.foundation.wrapper.IdProvider
import com.wisnu.kurniawan.composetodolist.model.QuadrantDisplayNames
import com.wisnu.kurniawan.composetodolist.model.TaskQuadrant
import com.wisnu.kurniawan.composetodolist.model.ToDoList
import com.wisnu.kurniawan.composetodolist.model.ToDoTask
import kotlinx.coroutines.flow.Flow

interface IListDetailEnvironment {
    val idProvider: IdProvider
    val dateTimeProvider: DateTimeProvider
    fun getQuadrantDisplayNames(): Flow<QuadrantDisplayNames>
    fun getListWithTasksById(listId: String): Flow<ToDoList>
    suspend fun createList(list: ToDoList): Flow<ToDoList>
    suspend fun updateList(list: ToDoList): Flow<Any>
    suspend fun createTask(taskName: String, listId: String, quadrant: TaskQuadrant)
    suspend fun toggleTaskStatus(toDoTask: ToDoTask)
    suspend fun deleteTask(task: ToDoTask)

    fun trackSaveListButtonClicked()
}
