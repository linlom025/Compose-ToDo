package com.wisnu.kurniawan.composetodolist.features.dashboard.data

import com.wisnu.kurniawan.composetodolist.model.Theme
import com.wisnu.kurniawan.composetodolist.model.ToDoTaskDiff
import com.wisnu.kurniawan.composetodolist.model.User
import kotlinx.coroutines.flow.Flow

interface IDashboardEnvironment {
    fun getUser(): Flow<User>
    fun getTheme(): Flow<Theme>
    suspend fun setTheme(theme: Theme)
    fun listenToDoTaskDiff(): Flow<ToDoTaskDiff>
}
