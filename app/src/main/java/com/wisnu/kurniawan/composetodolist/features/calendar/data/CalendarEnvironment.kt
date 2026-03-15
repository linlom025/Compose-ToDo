package com.wisnu.kurniawan.composetodolist.features.calendar.data

import com.wisnu.kurniawan.composetodolist.foundation.datasource.local.provider.ToDoListProvider
import com.wisnu.kurniawan.composetodolist.foundation.datasource.local.provider.ToDoTaskProvider
import com.wisnu.kurniawan.composetodolist.foundation.extension.toggleStatusHandler
import com.wisnu.kurniawan.composetodolist.foundation.wrapper.DateTimeProvider
import com.wisnu.kurniawan.composetodolist.model.ToDoTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CalendarEnvironment @Inject constructor(
    override val dateTimeProvider: DateTimeProvider,
    private val toDoListProvider: ToDoListProvider,
    private val toDoTaskProvider: ToDoTaskProvider,
) : ICalendarEnvironment {

    override fun loadTasksByCreatedDate(): Flow<List<CalendarTaskItem>> {
        return toDoListProvider.getListWithTasks()
            .map { lists ->
                lists.flatMap { list ->
                    list.tasks.map { task ->
                        CalendarTaskItem(
                            task = task,
                            listId = list.id
                        )
                    }
                }
            }
    }

    override suspend fun toggleTaskStatus(task: ToDoTask) {
        val now = dateTimeProvider.now()
        task.toggleStatusHandler(
            currentDate = now,
            onUpdateStatus = { completedAt, status ->
                toDoTaskProvider.updateTaskStatus(task.id, status, completedAt, now)
            },
            onUpdateDueDate = { nextDueDate ->
                toDoTaskProvider.updateTaskDueDate(task.id, nextDueDate, task.isDueDateTimeSet, now)
            }
        )
    }

    override suspend fun deleteTask(task: ToDoTask) {
        toDoTaskProvider.deleteTaskById(task.id)
    }
}
