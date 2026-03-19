package com.wisnu.kurniawan.composetodolist.features.todo.main.ui

import com.wisnu.kurniawan.composetodolist.features.todo.main.data.QuadrantTask
import com.wisnu.kurniawan.composetodolist.model.TaskQuadrant
import com.wisnu.kurniawan.composetodolist.model.ToDoStatus
import com.wisnu.kurniawan.composetodolist.model.ToDoTask
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class ToDoMainStateTest {

    @Test
    fun quadrants_whenShowCompletedTrue_sortInProgressFirstAndCompletedByCompletedAtDesc() {
        val base = LocalDateTime.of(2026, 3, 19, 10, 0)
        val taskInProgressA = task("ip-a", ToDoStatus.IN_PROGRESS, base.plusMinutes(1))
        val taskCompleteOld = task(
            "c-old",
            ToDoStatus.COMPLETE,
            base.plusMinutes(2),
            completedAt = base.plusMinutes(20)
        )
        val taskInProgressB = task("ip-b", ToDoStatus.IN_PROGRESS, base.plusMinutes(3))
        val taskCompleteNew = task(
            "c-new",
            ToDoStatus.COMPLETE,
            base.plusMinutes(4),
            completedAt = base.plusMinutes(40)
        )

        val state = ToDoMainState(
            tasks = listOf(
                QuadrantTask(taskInProgressB, "q1"),
                QuadrantTask(taskCompleteOld, "q1"),
                QuadrantTask(taskInProgressA, "q1"),
                QuadrantTask(taskCompleteNew, "q1")
            ),
            showCompleted = true
        )

        val taskIds = state.quadrants[TaskQuadrant.Q1].orEmpty().map { it.task.id }
        assertEquals(listOf("ip-b", "ip-a", "c-new", "c-old"), taskIds)
    }

    @Test
    fun quadrants_whenShowCompletedFalse_keepOnlyInProgressAndKeepOriginalOrder() {
        val base = LocalDateTime.of(2026, 3, 19, 10, 0)
        val taskInProgressA = task("ip-a", ToDoStatus.IN_PROGRESS, base.plusMinutes(1))
        val taskComplete = task(
            "c-1",
            ToDoStatus.COMPLETE,
            base.plusMinutes(2),
            completedAt = base.plusMinutes(20)
        )
        val taskInProgressB = task("ip-b", ToDoStatus.IN_PROGRESS, base.plusMinutes(3))

        val state = ToDoMainState(
            tasks = listOf(
                QuadrantTask(taskInProgressB, "q1"),
                QuadrantTask(taskComplete, "q1"),
                QuadrantTask(taskInProgressA, "q1")
            ),
            showCompleted = false
        )

        val taskIds = state.quadrants[TaskQuadrant.Q1].orEmpty().map { it.task.id }
        assertEquals(listOf("ip-b", "ip-a"), taskIds)
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
            quadrant = TaskQuadrant.Q1,
            completedAt = completedAt,
            createdAt = createdAt,
            updatedAt = createdAt
        )
    }
}
