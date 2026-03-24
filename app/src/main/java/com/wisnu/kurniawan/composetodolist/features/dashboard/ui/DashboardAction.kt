package com.wisnu.kurniawan.composetodolist.features.dashboard.ui

sealed class DashboardAction {
    object ToggleTheme : DashboardAction()
    data class OpenDisplayNameDialog(val target: DisplayNameEditTarget) : DashboardAction()
    object DismissDisplayNameDialog : DashboardAction()
    data class ChangeDisplayNameDraft(val value: String) : DashboardAction()
    object SaveCurrentDisplayName : DashboardAction()
    object ResetCurrentDisplayNameDefault : DashboardAction()
}
