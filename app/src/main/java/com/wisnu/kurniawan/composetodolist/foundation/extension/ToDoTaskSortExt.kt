package com.wisnu.kurniawan.composetodolist.foundation.extension

import com.wisnu.kurniawan.composetodolist.model.ToDoStatus
import com.wisnu.kurniawan.composetodolist.model.ToDoTask
import java.time.LocalDateTime

private fun ToDoTask.displayGroup(): Int {
    return if (status == ToDoStatus.IN_PROGRESS) 0 else 1
}

private fun ToDoTask.completedSortTime(): LocalDateTime {
    return completedAt ?: updatedAt
}

private fun compareToDoTaskForDisplay(left: ToDoTask, right: ToDoTask): Int {
    val groupCompare = left.displayGroup().compareTo(right.displayGroup())
    if (groupCompare != 0) return groupCompare

    if (left.status == ToDoStatus.COMPLETE && right.status == ToDoStatus.COMPLETE) {
        return right.completedSortTime().compareTo(left.completedSortTime())
    }

    // Keep in-progress items in their original order.
    return 0
}

fun List<ToDoTask>.sortedForDisplay(): List<ToDoTask> {
    return sortedWith(::compareToDoTaskForDisplay)
}

fun <T> List<T>.sortedByTaskForDisplay(taskSelector: (T) -> ToDoTask): List<T> {
    return sortedWith { left, right ->
        compareToDoTaskForDisplay(taskSelector(left), taskSelector(right))
    }
}
