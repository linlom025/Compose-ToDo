package com.wisnu.kurniawan.composetodolist.features.todo.taskreminder.ui

import com.wisnu.kurniawan.composetodolist.features.todo.taskreminder.data.ReminderKind

sealed class TaskReminderAction {
    data class AlarmShow(
        val taskId: String,
        val reminderKind: ReminderKind,
        val leadMinutes: Int?
    ) : TaskReminderAction()
    data class NotificationCompleted(val taskId: String) : TaskReminderAction()
    data class NotificationSnooze(val taskId: String) : TaskReminderAction()
    object AppBootCompleted : TaskReminderAction()
}
