package com.wisnu.kurniawan.composetodolist.features.todo.detail.ui

import com.wisnu.kurniawan.composetodolist.model.ToDoList
import com.wisnu.kurniawan.composetodolist.model.ToDoStatus
import com.wisnu.kurniawan.composetodolist.model.ToDoTask
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class ListDetailSortTest {

    @Test
    fun toToDoListState_insertCompleteHeaderAndSortCompletedByCompletedAtDesc() {
        val base = LocalDateTime.of(2026, 3, 19, 10, 0)
        val inProgressA = task("ip-a", ToDoStatus.IN_PROGRESS, base.plusMinutes(1))
        val completeOld = task(
            "c-old",
            ToDoStatus.COMPLETE,
            base.plusMinutes(2),
            completedAt = base.plusMinutes(20)
        )
        val inProgressB = task("ip-b", ToDoStatus.IN_PROGRESS, base.plusMinutes(3))
        val completeNew = task(
            "c-new",
            ToDoStatus.COMPLETE,
            base.plusMinutes(4),
            completedAt = base.plusMinutes(40)
        )

        val list = ToDoList(
            id = "list-1",
            name = "list",
            tasks = listOf(inProgressB, completeOld, inProgressA, completeNew),
            createdAt = base,
            updatedAt = base
        )

        val display = list.toToDoListState().tasks
        val names = display.mapNotNull {
            when (it) {
                is ToDoTaskItem.InProgress -> it.toDoTask.id
                is ToDoTaskItem.Complete -> it.toDoTask.id
                is ToDoTaskItem.CompleteHeader -> "header"
            }
        }

        assertEquals(listOf("ip-b", "ip-a", "header", "c-new", "c-old"), names)
    }

    private fun task(
        id: String,
        status: ToDoStatus,
        createdAt: LocalDateTime,
        completedAt: LocalDateTime? = null
    ): ToDoTask {
        return ToDoTask(
            id = id,
            name = id,
            status = status,
            completedAt = completedAt,
            createdAt = createdAt,
            updatedAt = createdAt
        )
    }
}
