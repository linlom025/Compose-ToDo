package com.wisnu.kurniawan.composetodolist.features.setting.ui

import com.wisnu.kurniawan.composetodolist.model.Theme

sealed class SettingAction {
    object RequestBack : SettingAction()
    object ClickSendTestNotification : SettingAction()
    object OpenThemeDialog : SettingAction()
    object CloseThemeDialog : SettingAction()
    data class SelectTheme(val theme: Theme) : SettingAction()
    data class ToggleAuthGate(val enabled: Boolean) : SettingAction()
    object ConfirmDisableAuthGate : SettingAction()
    object CancelDisableAuthGate : SettingAction()
    object AuthEnableVerificationSucceeded : SettingAction()
    object AuthEnableVerificationFailed : SettingAction()

    object OpenFontDialog : SettingAction()
    object CloseFontDialog : SettingAction()
    data class ChangeFontDraftPercent(val value: String) : SettingAction()
    object SaveFontScale : SettingAction()
    object ResetFontScaleDefault : SettingAction()
    object ConfirmSaveFontAndClose : SettingAction()
    object DiscardFontAndClose : SettingAction()
    object ContinueFontEditing : SettingAction()

    object OpenReminderDialog : SettingAction()
    object CloseReminderDialog : SettingAction()
    data class ChangeReminderDraftMinutes(val value: String) : SettingAction()
    object SaveReminderLeadMinutes : SettingAction()
    object ResetReminderLeadMinutesDefault : SettingAction()
    data class ToggleQuickFill(val enabled: Boolean) : SettingAction()
    object OpenQuickFillDurationDialog : SettingAction()
    object CloseQuickFillDurationDialog : SettingAction()
    data class ChangeQuickFillDurationDraftSeconds(val value: String) : SettingAction()
    object SaveQuickFillDuration : SettingAction()
    object ResetQuickFillDurationDefault : SettingAction()
    object ConfirmSaveReminderAndClose : SettingAction()
    object DiscardReminderAndClose : SettingAction()
    object ContinueReminderEditing : SettingAction()
}
