package com.wisnu.kurniawan.composetodolist.features.setting.ui

sealed class SettingEffect {
    object NavigateBack : SettingEffect()
    data class ShowMessageRes(val messageResId: Int) : SettingEffect()
    object LaunchAuthEnableVerification : SettingEffect()
}
