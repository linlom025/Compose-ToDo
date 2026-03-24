package com.wisnu.kurniawan.composetodolist.features.dashboard.data

import com.wisnu.foundation.coreloggr.Loggr
import com.wisnu.kurniawan.composetodolist.features.todo.taskreminder.data.TaskAlarmManager
import com.wisnu.kurniawan.composetodolist.features.todo.taskreminder.data.TaskNotificationManager
import com.wisnu.kurniawan.composetodolist.foundation.datasource.local.provider.ToDoTaskProvider
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.provider.AppDisplayNameProvider
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.provider.ReminderPreferenceProvider
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.provider.ThemeProvider
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.provider.UserProvider
import com.wisnu.kurniawan.composetodolist.features.todo.taskreminder.data.buildReminderSchedules
import com.wisnu.kurniawan.composetodolist.foundation.wrapper.DateTimeProvider
import com.wisnu.kurniawan.composetodolist.model.AppDisplayNameConfig
import com.wisnu.kurniawan.composetodolist.model.Theme
import com.wisnu.kurniawan.composetodolist.model.ToDoTask
import com.wisnu.kurniawan.composetodolist.model.ToDoTaskDiff
import com.wisnu.kurniawan.composetodolist.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class DashboardEnvironment @Inject constructor(
    private val dateTimeProvider: DateTimeProvider,
    private val userProvider: UserProvider,
    private val themeProvider: ThemeProvider,
    private val reminderPreferenceProvider: ReminderPreferenceProvider,
    private val appDisplayNameProvider: AppDisplayNameProvider,
    private val toDoTaskProvider: ToDoTaskProvider,
    private val taskAlarmManager: TaskAlarmManager,
    private val notificationManager: TaskNotificationManager
) : IDashboardEnvironment {

    override fun getUser(): Flow<User> {
        return userProvider.getUser()
    }

    override fun getTheme(): Flow<Theme> {
        return themeProvider.getTheme()
    }

    override suspend fun setTheme(theme: Theme) {
        themeProvider.setTheme(theme)
    }

    override suspend fun rescheduleAllReminders() {
        val scheduledTasks = toDoTaskProvider.getScheduledTasks().first()
        val leadMinutes = reminderPreferenceProvider.getReminderLeadMinutes().first()
        val now = dateTimeProvider.now()
        scheduledTasks.forEach { task ->
            val schedules = task.buildReminderSchedules(now = now, customLeadMinutes = leadMinutes)
            Loggr.debug("AlarmFlow") { "Bootstrap reschedule taskId=${task.id}, schedules=$schedules" }
            taskAlarmManager.cancelTaskAlarms(task)
            taskAlarmManager.scheduleTaskAlarms(task, schedules)
        }
    }

    // TODO e2e
    override fun listenToDoTaskDiff(): Flow<ToDoTaskDiff> {
        var tasks: Map<String, ToDoTask> = mapOf()
        return toDoTaskProvider.getScheduledTasks()
            .distinctUntilChangedBy { newTasks -> newTasks.map { Triple(it.dueDate, it.repeat, it.status) } } // Consume when due date, repeat, and status have changes only
            .map { newTasks -> newTasks.associateBy({ it.id }, { it }) }
            .map { newTasks ->
                ToDoTaskDiff(
                    addedTask = newTasks - tasks.keys,
                    deletedTask = tasks - newTasks.keys,
                    modifiedTask = newTasks.filter { (key, value) -> key in tasks.keys && value != tasks[key] }
                )
                    .apply {
                        tasks = newTasks
                    }
            }
            .drop(1) // Skip initial value
            .onEach { todoTaskDiff ->
                val leadMinutes = reminderPreferenceProvider.getReminderLeadMinutes().first()
                val now = dateTimeProvider.now()
                todoTaskDiff.addedTask.forEach {
                    Loggr.debug("AlarmFlow") { "Added task $it" }

                    taskAlarmManager.cancelTaskAlarms(it.value)
                    taskAlarmManager.scheduleTaskAlarms(
                        it.value,
                        it.value.buildReminderSchedules(now = now, customLeadMinutes = leadMinutes)
                    )
                }

                todoTaskDiff.modifiedTask.forEach {
                    Loggr.debug("AlarmFlow") { "Changed task $it" }

                    taskAlarmManager.cancelTaskAlarms(it.value)
                    taskAlarmManager.scheduleTaskAlarms(
                        it.value,
                        it.value.buildReminderSchedules(now = now, customLeadMinutes = leadMinutes)
                    )
                }

                todoTaskDiff.deletedTask.forEach {
                    Loggr.debug("AlarmFlow") { "Deleted task $it" }

                    taskAlarmManager.cancelTaskAlarms(it.value)
                    notificationManager.dismiss(it.value)
                }
            }
    }

    override fun getDisplayNameConfig(): Flow<AppDisplayNameConfig> {
        return appDisplayNameProvider.getDisplayNameConfig()
    }

    override suspend fun setDisplayNameConfig(config: AppDisplayNameConfig) {
        appDisplayNameProvider.setDisplayNameConfig(config)
    }

    override suspend fun resetDisplayNameConfig() {
        appDisplayNameProvider.resetDefault()
    }

}
