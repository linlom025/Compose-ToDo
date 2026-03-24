package com.wisnu.kurniawan.composetodolist.features.auth.ui

import androidx.compose.runtime.Immutable

@Immutable
data class AuthState(
    val status: AuthStatus = AuthStatus.Locked,
    val error: AuthError = AuthError.None,
    val canOpenSecuritySettings: Boolean = false,
)

enum class AuthStatus {
    Locked,
    Unlocking,
    Unlocked,
}

enum class AuthError {
    None,
    Canceled,
    Failed,
    NoneEnrolled,
    NoHardware,
    HardwareUnavailable,
    Unsupported,
    Unknown,
}
