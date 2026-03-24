package com.wisnu.kurniawan.composetodolist.features.auth.ui

sealed class AuthAction {
    object RequestAuthentication : AuthAction()
    object Retry : AuthAction()
    object AuthenticationSucceeded : AuthAction()
    object AuthenticationFailed : AuthAction()
    data class AuthenticationError(val code: Int) : AuthAction()
    object OpenSecuritySettings : AuthAction()
}
