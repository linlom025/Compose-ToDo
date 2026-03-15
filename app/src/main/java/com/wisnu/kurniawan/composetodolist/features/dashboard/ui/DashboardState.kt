package com.wisnu.kurniawan.composetodolist.features.dashboard.ui

import androidx.compose.runtime.Immutable
import com.wisnu.kurniawan.composetodolist.model.Theme
import com.wisnu.kurniawan.composetodolist.model.User

@Immutable
data class DashboardState(
    val user: User = User(),
    val theme: Theme = Theme.LIGHT
)
