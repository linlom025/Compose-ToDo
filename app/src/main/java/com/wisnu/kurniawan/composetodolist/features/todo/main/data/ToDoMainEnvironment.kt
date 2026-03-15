package com.wisnu.kurniawan.composetodolist.features.todo.main.data

import com.wisnu.kurniawan.composetodolist.foundation.datasource.local.model.ToDoGroupDb
import com.wisnu.kurniawan.composetodolist.foundation.datasource.local.provider.ToDoListProvider
import com.wisnu.kurniawan.composetodolist.foundation.datasource.local.provider.ToDoTaskProvider
import com.wisnu.kurniawan.composetodolist.foundation.extension.toggleStatusHandler
import com.wisnu.kurniawan.composetodolist.foundation.wrapper.DateTimeProvider
import com.wisnu.kurniawan.composetodolist.foundation.wrapper.IdProvider
import com.wisnu.kurniawan.composetodolist.model.TaskQuadrant
import com.wisnu.kurniawan.composetodolist.model.ToDoList
import com.wisnu.kurniawan.composetodolist.model.ToDoStatus
import com.wisnu.kurniawan.composetodolist.model.ToDoTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject

class ToDoMainEnvironment @Inject constructor(
    override val idProvider: IdProvider,
    override val dateTimeProvider: DateTimeProvider,
    private val toDoListProvider: ToDoListProvider,
    private val toDoTaskProvider: ToDoTaskProvider,
) : IToDoMainEnvironment {

    override fun loadQuadrantTasks(): Flow<List<QuadrantTask>> {
        return toDoListProvider.getListWithTasks()
            .map { lists ->
                lists.flatMap { list ->
                    list.tasks.map { task -> QuadrantTask(task = task, listId = list.id) }
                }
            }
    }

    override suspend fun ensureQuadrantSystemLists() {
        val existingLists = toDoListProvider.getList().first()
        val now = dateTimeProvider.now()
        val toInsert = QuadrantSystemLists.all
            .filter { systemList -> existingLists.none { it.id == systemList.listId } }
            .map { systemList ->
                ToDoList(
                    id = systemList.listId,
                    name = systemList.listName,
                    color = systemList.color,
                    createdAt = now,
                    updatedAt = now
                )
            }

        if (toInsert.isNotEmpty()) {
            toDoListProvider.insertList(toInsert, ToDoGroupDb.DEFAULT_ID)
        }
    }

    override suspend fun migrateUncompletedTaskListIdToQuadrantList() {
        val now = dateTimeProvider.now()
        val listWithTasks = toDoListProvider.getListWithTasks().first()
        val updateMap = mutableMapOf<String, MutableList<String>>()

        listWithTasks.forEach { list ->
            list.tasks
                .filter { it.status != ToDoStatus.COMPLETE }
                .forEach { task ->
                    val targetListId = QuadrantSystemLists.listIdOf(task.quadrant)
                    if (targetListId != list.id) {
                        updateMap.getOrPut(targetListId) { mutableListOf() }.add(task.id)
                    }
                }
        }

        updateMap.forEach { (targetListId, taskIds) ->
            if (taskIds.isNotEmpty()) {
                toDoTaskProvider.updateTaskList(taskIds, targetListId, now)
            }
        }
    }

    override suspend fun createTaskInQuadrant(
        taskName: String,
        quadrant: TaskQuadrant,
        dueDate: LocalDateTime?,
        isDueDateTimeSet: Boolean,
        note: String,
    ) {
        val now = dateTimeProvider.now()
        toDoTaskProvider.insertTask(
            data = listOf(
                ToDoTask(
                    id = idProvider.generate(),
                    name = taskName,
                    quadrant = quadrant,
                    dueDate = dueDate,
                    isDueDateTimeSet = isDueDateTimeSet,
                    note = note,
                    noteUpdatedAt = if (note.isNotBlank()) now else null,
                    createdAt = now,
                    updatedAt = now
                )
            ),
            listId = QuadrantSystemLists.listIdOf(quadrant)
        )
    }

    override suspend fun toggleTaskStatus(task: ToDoTask) {
        val now = dateTimeProvider.now()
        task.toggleStatusHandler(
            now,
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
