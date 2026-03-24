package com.wisnu.kurniawan.composetodolist.features.auth.data

import android.content.Context
import androidx.biometric.BiometricManager
import com.wisnu.kurniawan.composetodolist.foundation.security.AppAuthGate
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AuthEnvironment @Inject constructor(
    @ApplicationContext private val context: Context
) : IAuthEnvironment {

    override fun checkAvailability(): AuthAvailability {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(ALLOWED_AUTHENTICATORS)) {
            BiometricManager.BIOMETRIC_SUCCESS -> AuthAvailability.Available
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> AuthAvailability.NoneEnrolled
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> AuthAvailability.NoHardware
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> AuthAvailability.HardwareUnavailable
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> AuthAvailability.Unsupported
            else -> AuthAvailability.Unknown
        }
    }

    override fun unlockApp() {
        AppAuthGate.unlock()
    }

    companion object {
        const val ALLOWED_AUTHENTICATORS =
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
    }
}
