package com.wisnu.kurniawan.composetodolist.features.auth.ui

sealed class AuthEffect {
    object LaunchPrompt : AuthEffect()
    object OpenSecuritySettings : AuthEffect()
    object NavigateToHome : AuthEffect()
}
