package com.wisnu.kurniawan.composetodolist.features.todo.taskreminder.data

import com.wisnu.foundation.coreloggr.Loggr
import com.wisnu.kurniawan.composetodolist.foundation.datasource.local.provider.ToDoTaskProvider
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.provider.ReminderPreferenceProvider
import com.wisnu.kurniawan.composetodolist.foundation.extension.toggleStatusHandler
import com.wisnu.kurniawan.composetodolist.foundation.security.StorageEncryptionMigrationCoordinator
import com.wisnu.kurniawan.composetodolist.foundation.wrapper.DateTimeProvider
import com.wisnu.kurniawan.composetodolist.model.TaskWithList
import com.wisnu.kurniawan.composetodolist.model.ToDoStatus
import com.wisnu.kurniawan.composetodolist.model.ToDoTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import javax.inject.Inject

internal fun shouldNotifyReminder(kind: ReminderKind): Boolean {
    return kind != ReminderKind.THIRTY_MIN_LEFT
}

class TaskReminderEnvironment @Inject constructor(
    private val dateTimeProvider: DateTimeProvider,
    private val toDoTaskProvider: ToDoTaskProvider,
    private val reminderPreferenceProvider: ReminderPreferenceProvider,
    private val migrationCoordinator: StorageEncryptionMigrationCoordinator,
    private val alarmManager: TaskAlarmManager,
    private val notificationManager: TaskNotificationManager
) : ITaskReminderEnvironment {

    override fun notifyNotification(
        taskId: String,
        reminderKind: ReminderKind,
        leadMinutes: Int?
    ): Flow<TaskWithList> {
        if (!shouldNotifyReminder(reminderKind)) {
            Loggr.debug("AlarmFlow") {
                "Ignore legacy reminder taskId=$taskId, kind=$reminderKind"
            }
            return emptyFlow()
        }
        return getTask(taskId)
            .onEach { (list, task) ->
                Loggr.debug("AlarmFlow") {
                    "AlarmShow taskId=${task.id}, kind=$reminderKind, leadMinutes=$leadMinutes"
                }
                notificationManager.show(task, list, reminderKind, leadMinutes)
            }
    }

    override fun snoozeReminder(taskId: String): Flow<TaskWithList> {
        return getTask(taskId)
            .onEach { task -> 
                alarmManager.scheduleTaskAlarm(
                    task = task.task,
                    time = dateTimeProvider.now().plusMinutes(15),
                    kind = ReminderKind.DUE_NOW
                )
                notificationManager.dismiss(task.task)
            }
    }

    override suspend fun completeReminder(taskId: String): Flow<TaskWithList> {
        return getTask(taskId)
            .onEach { task ->
                val currentDate = dateTimeProvider.now()
                task.task.toggleStatusHandler(
                    currentDate,
                    { completedAt, newStatus ->
                        toDoTaskProvider.updateTaskStatus(task.task.id, newStatus, completedAt, currentDate)
                    },
                    { nextDueDate ->
                        toDoTaskProvider.updateTaskDueDate(task.task.id, nextDueDate, task.task.isDueDateTimeSet, currentDate)
                    }
                )

                alarmManager.cancelTaskAlarms(task.task)
                notificationManager.dismiss(task.task)
            }
    }

    override fun restartAllReminder(): Flow<List<ToDoTask>> {
        if (!migrationCoordinator.isMigrationCompleted()) {
            Loggr.debug("AlarmFlow") { "迁移未完成，跳过提醒重建" }
            return emptyFlow()
        }

        return toDoTaskProvider.getScheduledTasks()
            .take(1)
            .onEach { tasks ->
                val leadMinutes = reminderPreferenceProvider.getReminderLeadMinutes().first()
                val now = dateTimeProvider.now()
                tasks.forEach {
                    alarmManager.cancelTaskAlarms(it)
                    alarmManager.scheduleTaskAlarms(
                        task = it,
                        schedules = it.buildReminderSchedules(
                            now = now,
                            customLeadMinutes = leadMinutes
                        )
                    )
                }
            }
    }

    private fun getTask(taskId: String): Flow<TaskWithList> {
        return toDoTaskProvider.getTaskWithListById(taskId)
            .take(1)
            .filter { task ->
                task.task.status != ToDoStatus.COMPLETE &&
                    task.task.dueDate != null
            }
    }

}
