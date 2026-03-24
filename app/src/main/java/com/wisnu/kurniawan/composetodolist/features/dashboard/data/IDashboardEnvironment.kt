package com.wisnu.kurniawan.composetodolist.features.dashboard.data

import com.wisnu.kurniawan.composetodolist.model.Theme
import com.wisnu.kurniawan.composetodolist.model.AppDisplayNameConfig
import com.wisnu.kurniawan.composetodolist.model.ToDoTaskDiff
import com.wisnu.kurniawan.composetodolist.model.User
import kotlinx.coroutines.flow.Flow

interface IDashboardEnvironment {
    fun getUser(): Flow<User>
    fun getTheme(): Flow<Theme>
    suspend fun setTheme(theme: Theme)
    suspend fun rescheduleAllReminders()
    fun listenToDoTaskDiff(): Flow<ToDoTaskDiff>
    fun getDisplayNameConfig(): Flow<AppDisplayNameConfig>
    suspend fun setDisplayNameConfig(config: AppDisplayNameConfig)
    suspend fun resetDisplayNameConfig()
}
