package com.wisnu.kurniawan.composetodolist.features.todo.taskreminder.data

import com.wisnu.kurniawan.composetodolist.model.TaskQuadrant
import com.wisnu.kurniawan.composetodolist.model.ToDoStatus
import com.wisnu.kurniawan.composetodolist.model.ToDoTask
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class ReminderScheduleCalculatorTest {

    @Test
    fun buildReminderSchedules_shouldGenerateTwoSchedulesWhenAllValid() {
        val now = LocalDateTime.of(2026, 3, 20, 10, 0)
        val due = now.plusHours(2)
        val task = buildTask(dueDate = due)

        val schedules = task.buildReminderSchedules(now = now, customLeadMinutes = 10)

        assertEquals(2, schedules.size)
        assertEquals(ReminderKind.CUSTOM_LEAD, schedules[0].kind)
        assertEquals(ReminderKind.DUE_NOW, schedules[1].kind)
    }

    @Test
    fun buildReminderSchedules_shouldDeduplicateWhenCustomLeadEqualsDueTime() {
        val now = LocalDateTime.of(2026, 3, 20, 10, 0)
        val due = now.plusHours(1)
        val task = buildTask(dueDate = due)

        val schedules = task.buildReminderSchedules(now = now, customLeadMinutes = 0)

        assertEquals(1, schedules.size)
        assertTrue(schedules.any { it.kind == ReminderKind.DUE_NOW })
    }

    @Test
    fun buildReminderSchedules_shouldSkipPastLeadRemindersAndKeepDueNow() {
        val now = LocalDateTime.of(2026, 3, 20, 10, 0)
        val due = now.minusMinutes(5)
        val task = buildTask(dueDate = due)

        val schedules = task.buildReminderSchedules(now = now, customLeadMinutes = 10)

        assertEquals(1, schedules.size)
        assertEquals(ReminderKind.DUE_NOW, schedules.first().kind)
        assertTrue(schedules.first().triggerAt.isAfter(now) || schedules.first().triggerAt == now)
    }

    private fun buildTask(dueDate: LocalDateTime?): ToDoTask {
        val created = LocalDateTime.of(2026, 3, 20, 9, 0)
        return ToDoTask(
            id = "task-1",
            name = "Task",
            status = ToDoStatus.IN_PROGRESS,
            quadrant = TaskQuadrant.Q1,
            dueDate = dueDate,
            isDueDateTimeSet = true,
            createdAt = created,
            updatedAt = created
        )
    }
}
