package com.wisnu.kurniawan.composetodolist.features.auth.ui

import androidx.biometric.BiometricPrompt
import com.wisnu.foundation.coreviewmodel.StatefulViewModel
import com.wisnu.kurniawan.composetodolist.features.auth.data.AuthAvailability
import com.wisnu.kurniawan.composetodolist.features.auth.data.IAuthEnvironment
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthGateViewModel @Inject constructor(
    authEnvironment: IAuthEnvironment
) : StatefulViewModel<AuthState, AuthEffect, AuthAction, IAuthEnvironment>(
    AuthState(),
    authEnvironment
) {
    override fun dispatch(action: AuthAction) {
        when (action) {
            AuthAction.RequestAuthentication,
            AuthAction.Retry -> requestAuthentication()

            AuthAction.AuthenticationSucceeded -> {
                environment.unlockApp()
                setState {
                    copy(
                        status = AuthStatus.Unlocked,
                        error = AuthError.None,
                        canOpenSecuritySettings = false
                    )
                }
                setEffect(AuthEffect.NavigateToHome)
            }

            AuthAction.AuthenticationFailed -> {
                setState {
                    copy(
                        status = AuthStatus.Locked,
                        error = AuthError.Failed,
                        canOpenSecuritySettings = false
                    )
                }
            }

            is AuthAction.AuthenticationError -> {
                val error = when (action.code) {
                    BiometricPrompt.ERROR_USER_CANCELED,
                    BiometricPrompt.ERROR_CANCELED,
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON -> AuthError.Canceled

                    else -> AuthError.Failed
                }
                setState {
                    copy(
                        status = AuthStatus.Locked,
                        error = error,
                        canOpenSecuritySettings = false
                    )
                }
            }

            AuthAction.OpenSecuritySettings -> {
                setEffect(AuthEffect.OpenSecuritySettings)
            }
        }
    }

    private fun requestAuthentication() {
        if (state.value.status == AuthStatus.Unlocking) return

        when (environment.checkAvailability()) {
            AuthAvailability.Available -> {
                setState {
                    copy(
                        status = AuthStatus.Unlocking,
                        error = AuthError.None,
                        canOpenSecuritySettings = false
                    )
                }
                setEffect(AuthEffect.LaunchPrompt)
            }

            AuthAvailability.NoneEnrolled -> {
                setState {
                    copy(
                        status = AuthStatus.Locked,
                        error = AuthError.NoneEnrolled,
                        canOpenSecuritySettings = true
                    )
                }
            }

            AuthAvailability.NoHardware -> {
                setState {
                    copy(
                        status = AuthStatus.Locked,
                        error = AuthError.NoHardware,
                        canOpenSecuritySettings = true
                    )
                }
            }

            AuthAvailability.HardwareUnavailable -> {
                setState {
                    copy(
                        status = AuthStatus.Locked,
                        error = AuthError.HardwareUnavailable,
                        canOpenSecuritySettings = true
                    )
                }
            }

            AuthAvailability.Unsupported -> {
                setState {
                    copy(
                        status = AuthStatus.Locked,
                        error = AuthError.Unsupported,
                        canOpenSecuritySettings = true
                    )
                }
            }

            AuthAvailability.Unknown -> {
                setState {
                    copy(
                        status = AuthStatus.Locked,
                        error = AuthError.Unknown,
                        canOpenSecuritySettings = false
                    )
                }
            }
        }
    }
}
