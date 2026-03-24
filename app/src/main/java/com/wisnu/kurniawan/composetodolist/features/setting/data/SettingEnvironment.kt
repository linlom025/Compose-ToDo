package com.wisnu.kurniawan.composetodolist.features.setting.data

import com.wisnu.kurniawan.composetodolist.features.todo.taskreminder.data.TaskNotificationManager
import com.wisnu.kurniawan.composetodolist.features.todo.taskreminder.data.TaskNotificationSendResult
import com.wisnu.kurniawan.composetodolist.features.todo.taskreminder.data.TaskAlarmManager
import com.wisnu.kurniawan.composetodolist.features.todo.taskreminder.data.buildReminderSchedules
import com.wisnu.kurniawan.composetodolist.foundation.datasource.local.provider.ToDoTaskProvider
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.provider.ThemeProvider
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.provider.FontScaleProvider
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.provider.AuthGatePreferenceProvider
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.provider.ClipboardImportPreferenceProvider
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.provider.ReminderPreferenceProvider
import com.wisnu.kurniawan.composetodolist.foundation.wrapper.DateTimeProvider
import com.wisnu.kurniawan.composetodolist.model.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SettingEnvironment @Inject constructor(
    private val themeProvider: ThemeProvider,
    private val fontScaleProvider: FontScaleProvider,
    private val authGatePreferenceProvider: AuthGatePreferenceProvider,
    private val clipboardImportPreferenceProvider: ClipboardImportPreferenceProvider,
    private val reminderPreferenceProvider: ReminderPreferenceProvider,
    private val dateTimeProvider: DateTimeProvider,
    private val toDoTaskProvider: ToDoTaskProvider,
    private val taskAlarmManager: TaskAlarmManager,
    private val taskNotificationManager: TaskNotificationManager,
) : ISettingEnvironment {

    override fun getTheme(): Flow<Theme> {
        return themeProvider.getTheme()
    }

    override suspend fun setTheme(theme: Theme) {
        themeProvider.setTheme(theme)
    }

    override fun getFontScalePercent(): Flow<Int> {
        return fontScaleProvider.getFontScalePercent()
    }

    override suspend fun setFontScalePercent(percent: Int) {
        fontScaleProvider.setFontScalePercent(percent)
    }

    override fun getAuthGateEnabled(): Flow<Boolean> {
        return authGatePreferenceProvider.getAuthGateEnabled()
    }

    override suspend fun setAuthGateEnabled(enabled: Boolean) {
        authGatePreferenceProvider.setAuthGateEnabled(enabled)
    }

    override fun getReminderLeadMinutes(): Flow<Int> {
        return reminderPreferenceProvider.getReminderLeadMinutes()
    }

    override suspend fun setReminderLeadMinutes(minutes: Int) {
        reminderPreferenceProvider.setReminderLeadMinutes(minutes)
    }

    override fun getQuickFillEnabled(): Flow<Boolean> {
        return clipboardImportPreferenceProvider.getQuickFillEnabled()
    }

    override suspend fun setQuickFillEnabled(enabled: Boolean) {
        clipboardImportPreferenceProvider.setQuickFillEnabled(enabled)
    }

    override suspend fun rescheduleAllReminders() {
        val now = dateTimeProvider.now()
        val leadMinutes = reminderPreferenceProvider.getReminderLeadMinutes().first()
        val tasks = toDoTaskProvider.getScheduledTasks().first()
        tasks.forEach { task ->
            taskAlarmManager.cancelTaskAlarms(task)
            taskAlarmManager.scheduleTaskAlarms(
                task = task,
                schedules = task.buildReminderSchedules(
                    now = now,
                    customLeadMinutes = leadMinutes
                )
            )
        }
    }

    override fun sendTestNotification(): TaskNotificationSendResult {
        return taskNotificationManager.showTestNotification()
    }
}
