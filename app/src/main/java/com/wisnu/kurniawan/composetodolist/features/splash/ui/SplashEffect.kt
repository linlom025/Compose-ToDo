package com.wisnu.kurniawan.composetodolist.features.splash.ui

sealed class SplashEffect {
    object NavigateToAuthGate : SplashEffect()
    object NavigateToHome : SplashEffect()
}
