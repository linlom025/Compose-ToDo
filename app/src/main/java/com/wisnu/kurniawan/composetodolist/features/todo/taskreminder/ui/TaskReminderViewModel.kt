package com.wisnu.kurniawan.composetodolist.features.todo.taskreminder.ui

import android.util.Log
import com.wisnu.kurniawan.composetodolist.features.todo.taskreminder.data.ITaskReminderEnvironment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskReminderViewModel @Inject constructor(
    private val environment: ITaskReminderEnvironment,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun dispatch(action: TaskReminderAction) {
        when (action) {
            is TaskReminderAction.AlarmShow -> {
                launchSafely("AlarmShow") {
                    environment.notifyNotification(
                        taskId = action.taskId,
                        reminderKind = action.reminderKind,
                        leadMinutes = action.leadMinutes
                    )
                        .collect()
                }
            }
            TaskReminderAction.AppBootCompleted -> {
                launchSafely("AppBootCompleted") {
                    environment.restartAllReminder()
                        .collect()
                }
            }
            is TaskReminderAction.NotificationCompleted -> {
                launchSafely("NotificationCompleted") {
                    environment.completeReminder(action.taskId)
                        .collect()
                }
            }
            is TaskReminderAction.NotificationSnooze -> {
                launchSafely("NotificationSnooze") {
                    environment.snoozeReminder(action.taskId)
                        .collect()
                }
            }
        }
    }

    private fun launchSafely(tag: String, block: suspend () -> Unit) {
        scope.launch {
            runCatching {
                block()
            }.onFailure { throwable ->
                Log.e("AlarmFlow", "提醒处理失败：$tag", throwable)
            }
        }
    }
}
