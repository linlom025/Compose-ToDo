package com.wisnu.kurniawan.composetodolist.features.todo.taskreminder.data

import com.wisnu.kurniawan.composetodolist.model.ToDoStatus
import com.wisnu.kurniawan.composetodolist.model.ToDoTask
import java.time.LocalDateTime

data class ReminderSchedule(
    val kind: ReminderKind,
    val triggerAt: LocalDateTime,
    val leadMinutes: Int? = null,
)

fun ToDoTask.buildReminderSchedules(
    now: LocalDateTime,
    customLeadMinutes: Int,
): List<ReminderSchedule> {
    if (status == ToDoStatus.COMPLETE || dueDate == null) return emptyList()

    val dueAt = dueDate
    val rawSchedules = listOf(
        ReminderSchedule(
            kind = ReminderKind.CUSTOM_LEAD,
            triggerAt = dueAt.minusMinutes(customLeadMinutes.toLong()),
            leadMinutes = customLeadMinutes
        ),
        ReminderSchedule(
            kind = ReminderKind.DUE_NOW,
            triggerAt = if (dueAt.isBefore(now)) now.plusSeconds(5) else dueAt
        )
    )

    val valid = rawSchedules.filter { schedule ->
        when (schedule.kind) {
            ReminderKind.DUE_NOW -> true
            ReminderKind.CUSTOM_LEAD -> !schedule.triggerAt.isBefore(now)
            ReminderKind.THIRTY_MIN_LEFT -> false
        }
    }

    return valid
        .groupBy { it.triggerAt }
        .values
        .mapNotNull { schedulesAtSameTime ->
            schedulesAtSameTime.maxByOrNull { it.kind.priority() }
        }
        .sortedBy { it.triggerAt }
}

private fun ReminderKind.priority(): Int {
    return when (this) {
        ReminderKind.CUSTOM_LEAD -> 1
        ReminderKind.THIRTY_MIN_LEFT -> 0
        ReminderKind.DUE_NOW -> 3
    }
}
