package com.wisnu.kurniawan.composetodolist.features.auth.ui

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.wisnu.kurniawan.composetodolist.R
import com.wisnu.kurniawan.composetodolist.features.auth.data.AuthEnvironment
import com.wisnu.kurniawan.composetodolist.foundation.security.AppAuthGate
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgButton
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgPageLayout
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgSecondaryButton
import com.wisnu.kurniawan.composetodolist.foundation.viewmodel.HandleEffect
import com.wisnu.kurniawan.composetodolist.runtime.navigation.HomeFlow
import com.wisnu.kurniawan.composetodolist.runtime.navigation.MainFlow

@Composable
fun AuthGateScreen(
    navController: NavController,
    viewModel: AuthGateViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = remember(context) { context.findFragmentActivity() }
    val authSession by AppAuthGate.session.collectAsStateWithLifecycle()
    val latestViewModel by rememberUpdatedState(viewModel)

    val biometricPrompt = remember(activity, context) {
        activity?.let { hostActivity ->
            BiometricPrompt(
                hostActivity,
                ContextCompat.getMainExecutor(context),
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        latestViewModel.dispatch(AuthAction.AuthenticationSucceeded)
                    }

                    override fun onAuthenticationFailed() {
                        latestViewModel.dispatch(AuthAction.AuthenticationFailed)
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        latestViewModel.dispatch(AuthAction.AuthenticationError(errorCode))
                    }
                }
            )
        }
    }

    LaunchedEffect(authSession.lockVersion, authSession.isLocked) {
        if (authSession.isLocked) {
            viewModel.dispatch(AuthAction.RequestAuthentication)
        }
    }

    BackHandler(enabled = true) {
    }

    PgPageLayout(verticalArrangement = Arrangement.Center) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.auth_gate_title),
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.size(8.dp))

            Text(
                text = stringResource(R.string.auth_gate_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (state.error != AuthError.None) {
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = state.error.toDisplayText(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.size(16.dp))

            PgButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { viewModel.dispatch(AuthAction.Retry) }
            ) {
                Text(text = stringResource(R.string.auth_gate_retry))
            }

            if (state.canOpenSecuritySettings) {
                Spacer(modifier = Modifier.size(10.dp))
                PgSecondaryButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { viewModel.dispatch(AuthAction.OpenSecuritySettings) }
                ) {
                    Text(
                        text = stringResource(R.string.auth_gate_open_security_settings),
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
        }
    }

    HandleEffect(viewModel) { effect ->
        when (effect) {
            AuthEffect.LaunchPrompt -> {
                if (biometricPrompt == null) {
                    viewModel.dispatch(AuthAction.AuthenticationError(BiometricPrompt.ERROR_HW_UNAVAILABLE))
                    return@HandleEffect
                }

                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle(context.getString(R.string.auth_gate_prompt_title))
                    .setSubtitle(context.getString(R.string.auth_gate_prompt_subtitle))
                    .setAllowedAuthenticators(AuthEnvironment.ALLOWED_AUTHENTICATORS)
                    .build()
                biometricPrompt.authenticate(promptInfo)
            }

            AuthEffect.OpenSecuritySettings -> {
                val intent = Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }

            AuthEffect.NavigateToHome -> {
                val popped = navController.popBackStack()
                if (!popped) {
                    navController.navigate(HomeFlow.Root.route) {
                        popUpTo(MainFlow.AuthGate.route) {
                            inclusive = true
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthError.toDisplayText(): String {
    return when (this) {
        AuthError.None -> ""
        AuthError.Canceled -> stringResource(R.string.auth_gate_error_canceled)
        AuthError.Failed -> stringResource(R.string.auth_gate_error_failed)
        AuthError.NoneEnrolled -> stringResource(R.string.auth_gate_error_none_enrolled)
        AuthError.NoHardware -> stringResource(R.string.auth_gate_error_no_hardware)
        AuthError.HardwareUnavailable -> stringResource(R.string.auth_gate_error_hardware_unavailable)
        AuthError.Unsupported -> stringResource(R.string.auth_gate_error_unsupported)
        AuthError.Unknown -> stringResource(R.string.auth_gate_error_unknown)
    }
}

private tailrec fun Context.findFragmentActivity(): FragmentActivity? {
    return when (this) {
        is FragmentActivity -> this
        is ContextWrapper -> baseContext.findFragmentActivity()
        else -> null
    }
}
