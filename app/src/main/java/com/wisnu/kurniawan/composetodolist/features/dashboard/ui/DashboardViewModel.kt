package com.wisnu.kurniawan.composetodolist.features.dashboard.ui

import androidx.lifecycle.viewModelScope
import com.wisnu.foundation.coreviewmodel.StatefulViewModel
import com.wisnu.kurniawan.composetodolist.features.dashboard.data.IDashboardEnvironment
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

    private fun initTheme() {
        viewModelScope.launch {
            environment.getTheme()
                .collect { setState { copy(theme = it) } }
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
        }
    }
}
