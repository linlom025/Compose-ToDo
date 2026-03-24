package com.wisnu.kurniawan.composetodolist.features.dashboard.ui

import androidx.lifecycle.viewModelScope
import com.wisnu.foundation.coreviewmodel.StatefulViewModel
import com.wisnu.kurniawan.composetodolist.features.dashboard.data.IDashboardEnvironment
import com.wisnu.kurniawan.composetodolist.model.AppDisplayNameConfig
import com.wisnu.kurniawan.composetodolist.model.Theme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    dashboardEnvironment: IDashboardEnvironment,
) :
    StatefulViewModel<DashboardState, Unit, DashboardAction, IDashboardEnvironment>(DashboardState(), dashboardEnvironment) {

    init {
        initUser()
        initTheme()
        initDisplayNames()
        initReminderBootstrap()
        initToDoTaskDiff()
    }

    private fun initUser() {
        viewModelScope.launch {
            environment.getUser()
                .collect { setState { copy(user = it) } }
        }
    }

    private fun initToDoTaskDiff() {
        viewModelScope.launch {
            environment.listenToDoTaskDiff()
                .collect()
        }
    }

    private fun initReminderBootstrap() {
        viewModelScope.launch {
            environment.rescheduleAllReminders()
        }
    }

    private fun initTheme() {
        viewModelScope.launch {
            environment.getTheme()
                .collect { setState { copy(theme = it) } }
        }
    }

    private fun initDisplayNames() {
        viewModelScope.launch {
            environment.getDisplayNameConfig()
                .collect { config ->
                    setState {
                        copy(
                            displayNameConfig = config
                        )
                    }
                }
        }
    }

    override fun dispatch(action: DashboardAction) {
        when (action) {
            DashboardAction.ToggleTheme -> {
                viewModelScope.launch {
                    val target = if (state.value.theme == Theme.NIGHT) {
                        Theme.LIGHT
                    } else {
                        Theme.NIGHT
                    }
                    environment.setTheme(target)
                }
            }
            is DashboardAction.OpenDisplayNameDialog -> {
                setState {
                    copy(
                        showDisplayNameDialog = true,
                        displayNameEditTarget = action.target,
                        displayNameDraft = displayNameConfig.titleOf(action.target)
                    )
                }
            }
            DashboardAction.DismissDisplayNameDialog -> {
                setState {
                    copy(
                        showDisplayNameDialog = false,
                        displayNameEditTarget = null,
                        displayNameDraft = ""
                    )
                }
            }
            is DashboardAction.ChangeDisplayNameDraft -> {
                setState { copy(displayNameDraft = action.value) }
            }
            DashboardAction.SaveCurrentDisplayName -> {
                if (!state.value.isDraftValid) return
                viewModelScope.launch {
                    val editTarget = state.value.displayNameEditTarget ?: return@launch
                    val updatedConfig = state.value.displayNameConfig.withTitle(
                        target = editTarget,
                        value = state.value.displayNameDraft.trim()
                    )
                    environment.setDisplayNameConfig(
                        updatedConfig
                    )
                    setState {
                        copy(
                            showDisplayNameDialog = false,
                            displayNameEditTarget = null,
                            displayNameDraft = ""
                        )
                    }
                }
            }
            DashboardAction.ResetCurrentDisplayNameDefault -> {
                viewModelScope.launch {
                    val editTarget = state.value.displayNameEditTarget ?: return@launch
                    val defaultConfig = state.value.displayNameConfig.defaultCurrent(editTarget)
                    environment.setDisplayNameConfig(defaultConfig)
                    setState {
                        copy(
                            showDisplayNameDialog = false,
                            displayNameEditTarget = null,
                            displayNameDraft = ""
                        )
                    }
                }
            }
        }
    }
}

private fun AppDisplayNameConfig.titleOf(target: DisplayNameEditTarget): String {
    return when (target) {
        DisplayNameEditTarget.AppTitle -> appTitle
        DisplayNameEditTarget.Q1 -> quadrantTitles.q1
        DisplayNameEditTarget.Q2 -> quadrantTitles.q2
        DisplayNameEditTarget.Q3 -> quadrantTitles.q3
        DisplayNameEditTarget.Q4 -> quadrantTitles.q4
    }
}

private fun AppDisplayNameConfig.withTitle(target: DisplayNameEditTarget, value: String): AppDisplayNameConfig {
    return when (target) {
        DisplayNameEditTarget.AppTitle -> copy(appTitle = value)
        DisplayNameEditTarget.Q1 -> copy(quadrantTitles = quadrantTitles.copy(q1 = value))
        DisplayNameEditTarget.Q2 -> copy(quadrantTitles = quadrantTitles.copy(q2 = value))
        DisplayNameEditTarget.Q3 -> copy(quadrantTitles = quadrantTitles.copy(q3 = value))
        DisplayNameEditTarget.Q4 -> copy(quadrantTitles = quadrantTitles.copy(q4 = value))
    }
}

private fun AppDisplayNameConfig.defaultCurrent(target: DisplayNameEditTarget): AppDisplayNameConfig {
    val defaultConfig = AppDisplayNameConfig.default()
    return when (target) {
        DisplayNameEditTarget.AppTitle -> copy(appTitle = defaultConfig.appTitle)
        DisplayNameEditTarget.Q1 -> copy(quadrantTitles = quadrantTitles.copy(q1 = defaultConfig.quadrantTitles.q1))
        DisplayNameEditTarget.Q2 -> copy(quadrantTitles = quadrantTitles.copy(q2 = defaultConfig.quadrantTitles.q2))
        DisplayNameEditTarget.Q3 -> copy(quadrantTitles = quadrantTitles.copy(q3 = defaultConfig.quadrantTitles.q3))
        DisplayNameEditTarget.Q4 -> copy(quadrantTitles = quadrantTitles.copy(q4 = defaultConfig.quadrantTitles.q4))
    }
}
