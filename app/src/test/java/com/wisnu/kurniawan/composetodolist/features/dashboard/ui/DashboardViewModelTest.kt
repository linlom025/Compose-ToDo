package com.wisnu.kurniawan.composetodolist.features.dashboard.ui

import com.wisnu.kurniawan.composetodolist.BaseViewModelTest
import com.wisnu.kurniawan.composetodolist.features.dashboard.data.IDashboardEnvironment
import com.wisnu.kurniawan.composetodolist.model.AppDisplayNameConfig
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
        Assert.assertEquals(1, fakeDashboardEnvironment.rescheduleCalledCount)
    }

    @Test
    fun toggleTheme_nightAndLightShortcut() = runTest {
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

    @Test
    fun toggleTheme_fromOtherThemeGoesNight() = runTest {
        val fakeDashboardEnvironment = FakeDashboardEnvironment(
            user = User("wisnu@dev.id"),
            theme = Theme.SUNRISE
        )
        val dashboardViewModel = DashboardViewModel(fakeDashboardEnvironment)

        advanceUntilIdle()
        dashboardViewModel.dispatch(DashboardAction.ToggleTheme)
        advanceUntilIdle()

        Assert.assertEquals(Theme.NIGHT, dashboardViewModel.state.value.theme)
    }

    @Test
    fun saveCurrentDisplayName_onlyUpdatesSelectedTarget() = runTest {
        val fakeDashboardEnvironment = FakeDashboardEnvironment(
            user = User("wisnu@dev.id"),
            theme = Theme.LIGHT
        )
        val dashboardViewModel = DashboardViewModel(fakeDashboardEnvironment)
        advanceUntilIdle()

        dashboardViewModel.dispatch(DashboardAction.OpenDisplayNameDialog(DisplayNameEditTarget.Q2))
        dashboardViewModel.dispatch(DashboardAction.ChangeDisplayNameDraft("Q2 custom"))
        dashboardViewModel.dispatch(DashboardAction.SaveCurrentDisplayName)
        advanceUntilIdle()

        Assert.assertEquals("Q2 custom", fakeDashboardEnvironment.displayNameFlow.value.quadrantTitles.q2)
        Assert.assertEquals(
            AppDisplayNameConfig.default().quadrantTitles.q1,
            fakeDashboardEnvironment.displayNameFlow.value.quadrantTitles.q1
        )
        Assert.assertFalse(dashboardViewModel.state.value.showDisplayNameDialog)
    }

    @Test
    fun resetCurrentDisplayNameDefault_onlyResetsSelectedTarget() = runTest {
        val fakeDashboardEnvironment = FakeDashboardEnvironment(
            user = User("wisnu@dev.id"),
            theme = Theme.LIGHT
        )
        val dashboardViewModel = DashboardViewModel(fakeDashboardEnvironment)
        advanceUntilIdle()

        val customized = AppDisplayNameConfig.default().copy(
            appTitle = "My Todo",
            quadrantTitles = AppDisplayNameConfig.default().quadrantTitles.copy(
                q1 = "Q1 custom",
                q2 = "Q2 custom",
                q3 = "Q3 custom",
                q4 = "Q4 custom"
            )
        )
        fakeDashboardEnvironment.setDisplayNameConfig(customized)
        advanceUntilIdle()

        dashboardViewModel.dispatch(DashboardAction.OpenDisplayNameDialog(DisplayNameEditTarget.Q3))
        dashboardViewModel.dispatch(DashboardAction.ResetCurrentDisplayNameDefault)
        advanceUntilIdle()

        Assert.assertEquals(
            AppDisplayNameConfig.default().quadrantTitles.q3,
            fakeDashboardEnvironment.displayNameFlow.value.quadrantTitles.q3
        )
        Assert.assertEquals("Q1 custom", fakeDashboardEnvironment.displayNameFlow.value.quadrantTitles.q1)
        Assert.assertEquals("My Todo", fakeDashboardEnvironment.displayNameFlow.value.appTitle)
    }

    private class FakeDashboardEnvironment(
        private val user: User,
        theme: Theme
    ) : IDashboardEnvironment {
        private val themeFlow = MutableStateFlow(theme)
        val displayNameFlow = MutableStateFlow(AppDisplayNameConfig.default())
        var rescheduleCalledCount: Int = 0

        override fun getUser(): Flow<User> {
            return flow { emit(user) }
        }

        override fun getTheme(): Flow<Theme> {
            return themeFlow
        }

        override suspend fun setTheme(theme: Theme) {
            themeFlow.emit(theme)
        }

        override suspend fun rescheduleAllReminders() {
            rescheduleCalledCount += 1
        }

        override fun listenToDoTaskDiff(): Flow<ToDoTaskDiff> {
            return flow { emit(ToDoTaskDiff(mapOf(), mapOf(), mapOf())) }
        }

        override fun getDisplayNameConfig(): Flow<AppDisplayNameConfig> {
            return displayNameFlow
        }

        override suspend fun setDisplayNameConfig(config: AppDisplayNameConfig) {
            displayNameFlow.emit(config)
        }

        override suspend fun resetDisplayNameConfig() {
            displayNameFlow.emit(AppDisplayNameConfig.default())
        }
    }
}
