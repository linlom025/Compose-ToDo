package com.wisnu.kurniawan.composetodolist.features.dashboard.ui

import androidx.compose.runtime.Immutable
import com.wisnu.kurniawan.composetodolist.model.AppDisplayNameConfig
import com.wisnu.kurniawan.composetodolist.model.Theme
import com.wisnu.kurniawan.composetodolist.model.User

@Immutable
data class DashboardState(
    val user: User = User(),
    val theme: Theme = Theme.LIGHT,
    val displayNameConfig: AppDisplayNameConfig = AppDisplayNameConfig.default(),
    val showDisplayNameDialog: Boolean = false,
    val displayNameEditTarget: DisplayNameEditTarget? = null,
    val displayNameDraft: String = "",
) {
    val isDraftValid: Boolean = displayNameDraft.trim().isNotBlank()
}

enum class DisplayNameEditTarget {
    AppTitle,
    Q1,
    Q2,
    Q3,
    Q4,
}
