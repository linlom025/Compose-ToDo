package com.wisnu.kurniawan.composetodolist.features.setting.ui

import androidx.lifecycle.viewModelScope
import com.wisnu.foundation.coreviewmodel.StatefulViewModel
import com.wisnu.kurniawan.composetodolist.R
import com.wisnu.kurniawan.composetodolist.features.setting.data.ISettingEnvironment
import com.wisnu.kurniawan.composetodolist.features.todo.taskreminder.data.TaskNotificationSendResult
import com.wisnu.kurniawan.composetodolist.foundation.security.AppAuthGate
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.provider.FontScaleProvider
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.provider.ReminderPreferenceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    settingEnvironment: ISettingEnvironment
) : StatefulViewModel<SettingState, SettingEffect, SettingAction, ISettingEnvironment>(
    SettingState(),
    settingEnvironment
) {

    init {
        initTheme()
        initAuthGate()
        initFontScale()
        initReminderLeadMinutes()
        initQuickFillEnabled()
    }

    override fun dispatch(action: SettingAction) {
        when (action) {
            SettingAction.OpenThemeDialog -> {
                setState { copy(showThemeDialog = true) }
            }

            SettingAction.CloseThemeDialog -> {
                setState { copy(showThemeDialog = false) }
            }

            is SettingAction.SelectTheme -> {
                viewModelScope.launch {
                    environment.setTheme(action.theme)
                    setState { copy(showThemeDialog = false) }
                }
            }

            is SettingAction.ChangeFontDraftPercent -> {
                val filtered = action.value.filter { it.isDigit() }.take(3)
                setState {
                    copy(
                        fontDraftPercentText = filtered,
                        fontValidationError = validateFontScale(filtered)
                    )
                }
            }

            SettingAction.OpenFontDialog -> {
                setState {
                    copy(
                        showFontDialog = true,
                        showFontUnsavedDialog = false,
                        fontDraftPercentText = appliedPercent.toString(),
                        fontValidationError = FontValidationError.None
                    )
                }
            }

            SettingAction.CloseFontDialog -> {
                if (hasUnsavedFontChanges()) {
                    setState { copy(showFontUnsavedDialog = true) }
                } else {
                    closeFontDialogWithoutSaving()
                }
            }

            SettingAction.SaveFontScale -> {
                viewModelScope.launch {
                    saveFontDraftAndCloseIfValid()
                }
            }

            SettingAction.ResetFontScaleDefault -> {
                setState {
                    copy(
                        fontDraftPercentText = FontScaleProvider.FONT_SCALE_DEFAULT_PERCENT.toString(),
                        fontValidationError = FontValidationError.None
                    )
                }
            }

            SettingAction.ConfirmSaveFontAndClose -> {
                viewModelScope.launch {
                    saveFontDraftAndCloseIfValid()
                }
            }

            SettingAction.DiscardFontAndClose -> {
                closeFontDialogWithoutSaving()
            }

            SettingAction.ContinueFontEditing -> {
                setState { copy(showFontUnsavedDialog = false) }
            }

            is SettingAction.ChangeReminderDraftMinutes -> {
                val filtered = action.value.filter { it.isDigit() }.take(4)
                setState {
                    copy(
                        reminderDraftMinutesText = filtered,
                        reminderValidationError = validateReminderLead(filtered)
                    )
                }
            }

            SettingAction.OpenReminderDialog -> {
                setState {
                    copy(
                        showReminderDialog = true,
                        showReminderUnsavedDialog = false,
                        reminderDraftMinutesText = appliedReminderLeadMinutes.toString(),
                        reminderValidationError = ReminderValidationError.None
                    )
                }
            }

            SettingAction.CloseReminderDialog -> {
                if (hasUnsavedReminderChanges()) {
                    setState { copy(showReminderUnsavedDialog = true) }
                } else {
                    closeReminderDialogWithoutSaving()
                }
            }

            SettingAction.SaveReminderLeadMinutes -> {
                viewModelScope.launch {
                    saveReminderDraftAndCloseIfValid()
                }
            }

            SettingAction.ResetReminderLeadMinutesDefault -> {
                setState {
                    copy(
                        reminderDraftMinutesText = ReminderPreferenceProvider.REMINDER_LEAD_DEFAULT_MINUTES.toString(),
                        reminderValidationError = ReminderValidationError.None
                    )
                }
            }

            is SettingAction.ToggleQuickFill -> {
                viewModelScope.launch {
                    environment.setQuickFillEnabled(action.enabled)
                    setState { copy(appliedQuickFillEnabled = action.enabled) }
                }
            }

            SettingAction.ConfirmSaveReminderAndClose -> {
                viewModelScope.launch {
                    saveReminderDraftAndCloseIfValid()
                }
            }

            SettingAction.DiscardReminderAndClose -> {
                closeReminderDialogWithoutSaving()
            }

            SettingAction.ContinueReminderEditing -> {
                setState { copy(showReminderUnsavedDialog = false) }
            }

            SettingAction.RequestBack -> {
                when {
                    state.value.showThemeDialog -> dispatch(SettingAction.CloseThemeDialog)
                    state.value.showReminderDialog -> dispatch(SettingAction.CloseReminderDialog)
                    state.value.showFontDialog -> dispatch(SettingAction.CloseFontDialog)
                    else -> setEffect(SettingEffect.NavigateBack)
                }
            }

            SettingAction.ClickSendTestNotification -> {
                viewModelScope.launch {
                    when (environment.sendTestNotification()) {
                        TaskNotificationSendResult.SENT -> {
                            setEffect(SettingEffect.ShowMessageRes(R.string.setting_test_notification_sent))
                        }
                        TaskNotificationSendResult.NOTIFICATION_DISABLED -> {
                            setEffect(SettingEffect.ShowMessageRes(R.string.setting_test_notification_disabled))
                        }
                        TaskNotificationSendResult.PERMISSION_DENIED -> {
                            setEffect(SettingEffect.ShowMessageRes(R.string.setting_test_notification_permission_denied))
                        }
                    }
                }
            }

            is SettingAction.ToggleAuthGate -> {
                if (action.enabled == state.value.appliedAuthGateEnabled) return
                if (action.enabled) {
                    setState {
                        copy(
                            appliedAuthGateEnabled = true,
                            awaitingAuthEnableVerification = true,
                            showAuthDisableConfirmDialog = false
                        )
                    }
                    setEffect(SettingEffect.LaunchAuthEnableVerification)
                } else {
                    setState { copy(showAuthDisableConfirmDialog = true) }
                }
            }

            SettingAction.ConfirmDisableAuthGate -> {
                viewModelScope.launch {
                    environment.setAuthGateEnabled(false)
                    AppAuthGate.setGateEnabled(false)
                    setState {
                        copy(
                            appliedAuthGateEnabled = false,
                            showAuthDisableConfirmDialog = false,
                            awaitingAuthEnableVerification = false
                        )
                    }
                }
            }

            SettingAction.CancelDisableAuthGate -> {
                setState {
                    copy(
                        showAuthDisableConfirmDialog = false,
                        appliedAuthGateEnabled = true
                    )
                }
            }

            SettingAction.AuthEnableVerificationSucceeded -> {
                if (!state.value.awaitingAuthEnableVerification) return
                viewModelScope.launch {
                    environment.setAuthGateEnabled(true)
                    AppAuthGate.setGateEnabled(true, lockImmediately = false)
                    setState {
                        copy(
                            appliedAuthGateEnabled = true,
                            awaitingAuthEnableVerification = false
                        )
                    }
                }
            }

            SettingAction.AuthEnableVerificationFailed -> {
                if (!state.value.awaitingAuthEnableVerification) return
                viewModelScope.launch {
                    environment.setAuthGateEnabled(false)
                    AppAuthGate.setGateEnabled(false)
                    setState {
                        copy(
                            appliedAuthGateEnabled = false,
                            awaitingAuthEnableVerification = false
                        )
                    }
                    setEffect(SettingEffect.ShowMessageRes(R.string.setting_auth_gate_enable_failed))
                }
            }
        }
    }

    private fun initTheme() {
        viewModelScope.launch {
            environment.getTheme()
                .collect { theme ->
                    setState { copy(appliedTheme = theme) }
                }
        }
    }

    private fun initAuthGate() {
        viewModelScope.launch {
            environment.getAuthGateEnabled()
                .collect { enabled ->
                    setState {
                        if (awaitingAuthEnableVerification) {
                            copy(
                                appliedAuthGateEnabled = enabled || appliedAuthGateEnabled
                            )
                        } else {
                            copy(appliedAuthGateEnabled = enabled)
                        }
                    }
                }
        }
    }

    private fun initFontScale() {
        viewModelScope.launch {
            environment.getFontScalePercent()
                .collect { percent ->
                    setState {
                        val shouldSyncDraft = !showFontDialog || fontDraftPercentText.toIntOrNull() == appliedPercent
                        copy(
                            appliedPercent = percent,
                            fontDraftPercentText = if (shouldSyncDraft) percent.toString() else fontDraftPercentText,
                            fontValidationError = if (shouldSyncDraft) FontValidationError.None else fontValidationError
                        )
                    }
                }
        }
    }

    private fun initReminderLeadMinutes() {
        viewModelScope.launch {
            environment.getReminderLeadMinutes()
                .collect { minutes ->
                    setState {
                        val shouldSyncDraft =
                            !showReminderDialog || reminderDraftMinutesText.toIntOrNull() == appliedReminderLeadMinutes
                        copy(
                            appliedReminderLeadMinutes = minutes,
                            reminderDraftMinutesText = if (shouldSyncDraft) minutes.toString() else reminderDraftMinutesText,
                            reminderValidationError = if (shouldSyncDraft) {
                                ReminderValidationError.None
                            } else {
                                reminderValidationError
                            }
                        )
                    }
                }
        }
    }

    private fun initQuickFillEnabled() {
        viewModelScope.launch {
            environment.getQuickFillEnabled()
                .collect { enabled ->
                    setState { copy(appliedQuickFillEnabled = enabled) }
                }
        }
    }

    private fun closeFontDialogWithoutSaving() {
        setState {
            copy(
                showFontDialog = false,
                showFontUnsavedDialog = false,
                fontDraftPercentText = appliedPercent.toString(),
                fontValidationError = FontValidationError.None
            )
        }
    }

    private fun closeReminderDialogWithoutSaving() {
        setState {
            copy(
                showReminderDialog = false,
                showReminderUnsavedDialog = false,
                reminderDraftMinutesText = appliedReminderLeadMinutes.toString(),
                reminderValidationError = ReminderValidationError.None
            )
        }
    }

    private suspend fun saveFontDraftAndCloseIfValid(): Boolean {
        val draftValue = state.value.fontDraftPercentText
        val error = validateFontScale(draftValue)
        if (error != FontValidationError.None) {
            setState { copy(fontValidationError = error) }
            return false
        }

        val target = draftValue.toIntOrNull() ?: return false
        environment.setFontScalePercent(target)
        setState {
            copy(
                showFontDialog = false,
                showFontUnsavedDialog = false
            )
        }
        return true
    }

    private suspend fun saveReminderDraftAndCloseIfValid(): Boolean {
        val draftValue = state.value.reminderDraftMinutesText
        val error = validateReminderLead(draftValue)
        if (error != ReminderValidationError.None) {
            setState { copy(reminderValidationError = error) }
            return false
        }

        val target = draftValue.toIntOrNull() ?: return false
        environment.setReminderLeadMinutes(target)
        environment.rescheduleAllReminders()
        setState {
            copy(
                showReminderDialog = false,
                showReminderUnsavedDialog = false
            )
        }
        return true
    }

    private fun hasUnsavedFontChanges(): Boolean {
        return state.value.fontDraftPercentText.toIntOrNull() != state.value.appliedPercent
    }

    private fun hasUnsavedReminderChanges(): Boolean {
        return state.value.reminderDraftMinutesText.toIntOrNull() != state.value.appliedReminderLeadMinutes
    }

    private fun validateFontScale(value: String): FontValidationError {
        if (value.isBlank()) return FontValidationError.Empty
        val parsed = value.toIntOrNull() ?: return FontValidationError.InvalidNumber
        if (parsed !in FontScaleProvider.FONT_SCALE_MIN_PERCENT..FontScaleProvider.FONT_SCALE_MAX_PERCENT) {
            return FontValidationError.OutOfRange
        }
        return FontValidationError.None
    }

    private fun validateReminderLead(value: String): ReminderValidationError {
        if (value.isBlank()) return ReminderValidationError.Empty
        val parsed = value.toIntOrNull() ?: return ReminderValidationError.InvalidNumber
        if (parsed !in ReminderPreferenceProvider.REMINDER_LEAD_MIN_MINUTES..ReminderPreferenceProvider.REMINDER_LEAD_MAX_MINUTES) {
            return ReminderValidationError.OutOfRange
        }
        return ReminderValidationError.None
    }
}
