package com.wisnu.kurniawan.composetodolist.features.dashboard.ui

import com.wisnu.kurniawan.composetodolist.BaseViewModelTest
import com.wisnu.kurniawan.composetodolist.features.dashboard.data.IDashboardEnvironment
import com.wisnu.kurniawan.composetodolist.model.Theme
import com.wisnu.kurniawan.composetodolist.model.ToDoTaskDiff
import com.wisnu.kurniawan.composetodolist.model.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import kotlin.time.ExperimentalTime

@ExperimentalTime
@ExperimentalCoroutinesApi
class DashboardViewModelTest : BaseViewModelTest() {

    @Test
    fun init() = runTest {
        val fakeDashboardEnvironment = FakeDashboardEnvironment(
            user = User("wisnu@dev.id"),
            theme = Theme.SUNRISE
        )
        val dashboardViewModel = DashboardViewModel(fakeDashboardEnvironment)

        advanceUntilIdle()

        Assert.assertEquals(User("wisnu@dev.id"), dashboardViewModel.state.value.user)
        Assert.assertEquals(Theme.SUNRISE, dashboardViewModel.state.value.theme)
    }

    @Test
    fun toggleTheme() = runTest {
        val fakeDashboardEnvironment = FakeDashboardEnvironment(
            user = User("wisnu@dev.id"),
            theme = Theme.NIGHT
        )
        val dashboardViewModel = DashboardViewModel(fakeDashboardEnvironment)

        advanceUntilIdle()
        Assert.assertEquals(Theme.NIGHT, dashboardViewModel.state.value.theme)

        dashboardViewModel.dispatch(DashboardAction.ToggleTheme)
        advanceUntilIdle()
        Assert.assertEquals(Theme.LIGHT, dashboardViewModel.state.value.theme)

        dashboardViewModel.dispatch(DashboardAction.ToggleTheme)
        advanceUntilIdle()
        Assert.assertEquals(Theme.NIGHT, dashboardViewModel.state.value.theme)
    }

    private class FakeDashboardEnvironment(
        private val user: User,
        theme: Theme
    ) : IDashboardEnvironment {
        private val themeFlow = MutableStateFlow(theme)

        override fun getUser(): Flow<User> {
            return flow { emit(user) }
        }

        override fun getTheme(): Flow<Theme> {
            return themeFlow
        }

        override suspend fun setTheme(theme: Theme) {
            themeFlow.emit(theme)
        }

        override fun listenToDoTaskDiff(): Flow<ToDoTaskDiff> {
            return flow { emit(ToDoTaskDiff(mapOf(), mapOf(), mapOf())) }
        }
    }
}
