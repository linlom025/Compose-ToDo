package com.wisnu.kurniawan.composetodolist.features.setting.ui

import app.cash.turbine.test
import com.wisnu.kurniawan.composetodolist.BaseViewModelTest
import com.wisnu.kurniawan.composetodolist.R
import com.wisnu.kurniawan.composetodolist.features.setting.data.ISettingEnvironment
import com.wisnu.kurniawan.composetodolist.features.todo.taskreminder.data.TaskNotificationSendResult
import com.wisnu.kurniawan.composetodolist.model.Theme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import kotlin.time.ExperimentalTime

@ExperimentalTime
@ExperimentalCoroutinesApi
class SettingViewModelTest : BaseViewModelTest() {

    @Test
    fun initSyncAppliedValues() = runTest {
        val fakeEnvironment = FakeSettingEnvironment(
            initialTheme = Theme.SUNRISE,
            initialFontPercent = 130,
            initialReminderMinutes = 25,
            initialQuickFillEnabled = true
        )
        val viewModel = SettingViewModel(fakeEnvironment)

        advanceUntilIdle()

        Assert.assertEquals(Theme.SUNRISE, viewModel.state.value.appliedTheme)
        Assert.assertEquals(130, viewModel.state.value.appliedPercent)
        Assert.assertEquals("130", viewModel.state.value.fontDraftPercentText)
        Assert.assertEquals(25, viewModel.state.value.appliedReminderLeadMinutes)
        Assert.assertEquals("25", viewModel.state.value.reminderDraftMinutesText)
        Assert.assertTrue(viewModel.state.value.appliedQuickFillEnabled)
    }

    @Test
    fun saveValidFontInputCloseDialog() = runTest {
        val fakeEnvironment = FakeSettingEnvironment()
        val viewModel = SettingViewModel(fakeEnvironment)
        advanceUntilIdle()
        viewModel.dispatch(SettingAction.OpenFontDialog)

        viewModel.dispatch(SettingAction.ChangeFontDraftPercent("137"))
        viewModel.dispatch(SettingAction.SaveFontScale)
        advanceUntilIdle()

        Assert.assertEquals(137, fakeEnvironment.fontScaleFlow.value)
        Assert.assertEquals(137, viewModel.state.value.appliedPercent)
        Assert.assertFalse(viewModel.state.value.showFontDialog)
    }

    @Test
    fun saveValidReminderInputCloseDialogAndReschedule() = runTest {
        val fakeEnvironment = FakeSettingEnvironment()
        val viewModel = SettingViewModel(fakeEnvironment)
        advanceUntilIdle()
        viewModel.dispatch(SettingAction.OpenReminderDialog)

        viewModel.dispatch(SettingAction.ChangeReminderDraftMinutes("10"))
        viewModel.dispatch(SettingAction.SaveReminderLeadMinutes)
        advanceUntilIdle()

        Assert.assertEquals(10, fakeEnvironment.reminderLeadFlow.value)
        Assert.assertEquals(10, viewModel.state.value.appliedReminderLeadMinutes)
        Assert.assertFalse(viewModel.state.value.showReminderDialog)
        Assert.assertEquals(1, fakeEnvironment.rescheduleCalledCount)
    }

    @Test
    fun closeReminderDialogWithUnsavedShowConfirmDialog() = runTest {
        val fakeEnvironment = FakeSettingEnvironment(initialReminderMinutes = 15)
        val viewModel = SettingViewModel(fakeEnvironment)
        advanceUntilIdle()
        viewModel.dispatch(SettingAction.OpenReminderDialog)

        viewModel.dispatch(SettingAction.ChangeReminderDraftMinutes("20"))
        viewModel.dispatch(SettingAction.CloseReminderDialog)
        advanceUntilIdle()

        Assert.assertTrue(viewModel.state.value.showReminderUnsavedDialog)
        Assert.assertTrue(viewModel.state.value.showReminderDialog)
    }

    @Test
    fun clickSendTestNotification_showSuccessMessage() = runTest {
        val fakeEnvironment = FakeSettingEnvironment(notificationResult = TaskNotificationSendResult.SENT)
        val viewModel = SettingViewModel(fakeEnvironment)
        advanceUntilIdle()

        viewModel.dispatch(SettingAction.ClickSendTestNotification)

        viewModel.effect.test {
            Assert.assertEquals(SettingEffect.ShowMessageRes(R.string.setting_test_notification_sent), awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun clickSendTestNotification_whenPermissionDenied_showErrorMessage() = runTest {
        val fakeEnvironment = FakeSettingEnvironment(notificationResult = TaskNotificationSendResult.PERMISSION_DENIED)
        val viewModel = SettingViewModel(fakeEnvironment)
        advanceUntilIdle()

        viewModel.dispatch(SettingAction.ClickSendTestNotification)

        viewModel.effect.test {
            Assert.assertEquals(
                SettingEffect.ShowMessageRes(R.string.setting_test_notification_permission_denied),
                awaitItem()
            )
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun toggleAuthGateOn_thenLaunchVerification_andSuccessPersistEnabled() = runTest {
        val fakeEnvironment = FakeSettingEnvironment(initialAuthGateEnabled = false)
        val viewModel = SettingViewModel(fakeEnvironment)
        advanceUntilIdle()

        viewModel.dispatch(SettingAction.ToggleAuthGate(true))
        Assert.assertTrue(viewModel.state.value.awaitingAuthEnableVerification)
        Assert.assertTrue(viewModel.state.value.appliedAuthGateEnabled)

        viewModel.dispatch(SettingAction.AuthEnableVerificationSucceeded)
        advanceUntilIdle()

        Assert.assertTrue(fakeEnvironment.authGateFlow.value)
        Assert.assertTrue(viewModel.state.value.appliedAuthGateEnabled)
        Assert.assertFalse(viewModel.state.value.awaitingAuthEnableVerification)
    }

    @Test
    fun toggleAuthGateOn_thenVerificationFailed_revertDisabledAndShowMessage() = runTest {
        val fakeEnvironment = FakeSettingEnvironment(initialAuthGateEnabled = false)
        val viewModel = SettingViewModel(fakeEnvironment)
        advanceUntilIdle()

        viewModel.dispatch(SettingAction.ToggleAuthGate(true))

        viewModel.effect.test {
            Assert.assertEquals(SettingEffect.LaunchAuthEnableVerification, awaitItem())
            viewModel.dispatch(SettingAction.AuthEnableVerificationFailed)
            Assert.assertEquals(
                SettingEffect.ShowMessageRes(R.string.setting_auth_gate_enable_failed),
                awaitItem()
            )
            cancelAndConsumeRemainingEvents()
        }
        advanceUntilIdle()

        Assert.assertFalse(fakeEnvironment.authGateFlow.value)
        Assert.assertFalse(viewModel.state.value.appliedAuthGateEnabled)
        Assert.assertFalse(viewModel.state.value.awaitingAuthEnableVerification)
    }

    @Test
    fun toggleAuthGateOff_requiresConfirm_andConfirmDisables() = runTest {
        val fakeEnvironment = FakeSettingEnvironment(initialAuthGateEnabled = true)
        val viewModel = SettingViewModel(fakeEnvironment)
        advanceUntilIdle()

        viewModel.dispatch(SettingAction.ToggleAuthGate(false))
        Assert.assertTrue(viewModel.state.value.showAuthDisableConfirmDialog)

        viewModel.dispatch(SettingAction.ConfirmDisableAuthGate)
        advanceUntilIdle()

        Assert.assertFalse(fakeEnvironment.authGateFlow.value)
        Assert.assertFalse(viewModel.state.value.appliedAuthGateEnabled)
        Assert.assertFalse(viewModel.state.value.showAuthDisableConfirmDialog)
    }

    @Test
    fun selectTheme_applyAndCloseDialog() = runTest {
        val fakeEnvironment = FakeSettingEnvironment(initialTheme = Theme.LIGHT)
        val viewModel = SettingViewModel(fakeEnvironment)
        advanceUntilIdle()

        viewModel.dispatch(SettingAction.OpenThemeDialog)
        Assert.assertTrue(viewModel.state.value.showThemeDialog)

        viewModel.dispatch(SettingAction.SelectTheme(Theme.AURORA))
        advanceUntilIdle()

        Assert.assertEquals(Theme.AURORA, fakeEnvironment.themeFlow.value)
        Assert.assertEquals(Theme.AURORA, viewModel.state.value.appliedTheme)
        Assert.assertFalse(viewModel.state.value.showThemeDialog)
    }

    @Test
    fun toggleQuickFill_updateStateAndPersist() = runTest {
        val fakeEnvironment = FakeSettingEnvironment(initialQuickFillEnabled = false)
        val viewModel = SettingViewModel(fakeEnvironment)
        advanceUntilIdle()

        viewModel.dispatch(SettingAction.ToggleQuickFill(true))
        advanceUntilIdle()

        Assert.assertTrue(fakeEnvironment.quickFillFlow.value)
        Assert.assertTrue(viewModel.state.value.appliedQuickFillEnabled)

        viewModel.dispatch(SettingAction.ToggleQuickFill(false))
        advanceUntilIdle()

        Assert.assertFalse(fakeEnvironment.quickFillFlow.value)
        Assert.assertFalse(viewModel.state.value.appliedQuickFillEnabled)
    }

    private class FakeSettingEnvironment(
        initialTheme: Theme = Theme.SYSTEM,
        initialFontPercent: Int = 100,
        initialAuthGateEnabled: Boolean = false,
        initialReminderMinutes: Int = 15,
        initialQuickFillEnabled: Boolean = false,
        private val notificationResult: TaskNotificationSendResult = TaskNotificationSendResult.SENT,
    ) : ISettingEnvironment {
        val themeFlow = MutableStateFlow(initialTheme)
        val fontScaleFlow = MutableStateFlow(initialFontPercent)
        val authGateFlow = MutableStateFlow(initialAuthGateEnabled)
        val reminderLeadFlow = MutableStateFlow(initialReminderMinutes)
        val quickFillFlow = MutableStateFlow(initialQuickFillEnabled)
        var rescheduleCalledCount = 0

        override fun getTheme(): Flow<Theme> = themeFlow

        override suspend fun setTheme(theme: Theme) {
            themeFlow.emit(theme)
        }

        override fun getFontScalePercent(): Flow<Int> = fontScaleFlow

        override suspend fun setFontScalePercent(percent: Int) {
            fontScaleFlow.emit(percent)
        }

        override fun getAuthGateEnabled(): Flow<Boolean> = authGateFlow

        override suspend fun setAuthGateEnabled(enabled: Boolean) {
            authGateFlow.emit(enabled)
        }

        override fun getReminderLeadMinutes(): Flow<Int> = reminderLeadFlow

        override suspend fun setReminderLeadMinutes(minutes: Int) {
            reminderLeadFlow.emit(minutes)
        }

        override fun getQuickFillEnabled(): Flow<Boolean> = quickFillFlow

        override suspend fun setQuickFillEnabled(enabled: Boolean) {
            quickFillFlow.emit(enabled)
        }

        override suspend fun rescheduleAllReminders() {
            rescheduleCalledCount += 1
        }

        override fun sendTestNotification(): TaskNotificationSendResult = notificationResult
    }
}
