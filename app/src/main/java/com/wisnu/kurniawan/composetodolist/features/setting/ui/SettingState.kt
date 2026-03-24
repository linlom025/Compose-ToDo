package com.wisnu.kurniawan.composetodolist.features.setting.ui

import androidx.compose.runtime.Immutable
import com.wisnu.kurniawan.composetodolist.model.Theme

@Immutable
data class SettingState(
    val appliedTheme: Theme = Theme.SYSTEM,
    val showThemeDialog: Boolean = false,
    val appliedPercent: Int = 100,
    val appliedAuthGateEnabled: Boolean = false,
    val showAuthDisableConfirmDialog: Boolean = false,
    val awaitingAuthEnableVerification: Boolean = false,
    val showFontDialog: Boolean = false,
    val fontDraftPercentText: String = "100",
    val fontValidationError: FontValidationError = FontValidationError.None,
    val showFontUnsavedDialog: Boolean = false,

    val appliedReminderLeadMinutes: Int = 15,
    val appliedQuickFillEnabled: Boolean = false,
    val showReminderDialog: Boolean = false,
    val reminderDraftMinutesText: String = "15",
    val reminderValidationError: ReminderValidationError = ReminderValidationError.None,
    val showReminderUnsavedDialog: Boolean = false,
)

sealed class FontValidationError {
    object None : FontValidationError()
    object Empty : FontValidationError()
    object InvalidNumber : FontValidationError()
    object OutOfRange : FontValidationError()
}

sealed class ReminderValidationError {
    object None : ReminderValidationError()
    object Empty : ReminderValidationError()
    object InvalidNumber : ReminderValidationError()
    object OutOfRange : ReminderValidationError()
}
