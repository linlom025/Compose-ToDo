package com.wisnu.kurniawan.composetodolist.features.setting.data

import com.wisnu.kurniawan.composetodolist.features.todo.taskreminder.data.TaskNotificationSendResult
import com.wisnu.kurniawan.composetodolist.model.Theme
import kotlinx.coroutines.flow.Flow

interface ISettingEnvironment {
    fun getTheme(): Flow<Theme>
    suspend fun setTheme(theme: Theme)
    fun getFontScalePercent(): Flow<Int>
    suspend fun setFontScalePercent(percent: Int)
    fun getAuthGateEnabled(): Flow<Boolean>
    suspend fun setAuthGateEnabled(enabled: Boolean)
    fun getReminderLeadMinutes(): Flow<Int>
    suspend fun setReminderLeadMinutes(minutes: Int)
    fun getQuickFillEnabled(): Flow<Boolean>
    suspend fun setQuickFillEnabled(enabled: Boolean)
    suspend fun rescheduleAllReminders()
    fun sendTestNotification(): TaskNotificationSendResult
}
