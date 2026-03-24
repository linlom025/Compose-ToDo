package com.wisnu.kurniawan.composetodolist.features.auth.data

interface IAuthEnvironment {
    fun checkAvailability(): AuthAvailability
    fun unlockApp()
}

enum class AuthAvailability {
    Available,
    NoneEnrolled,
    NoHardware,
    HardwareUnavailable,
    Unsupported,
    Unknown,
}
