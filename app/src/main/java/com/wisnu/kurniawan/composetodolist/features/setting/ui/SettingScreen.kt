package com.wisnu.kurniawan.composetodolist.features.setting.ui

import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wisnu.kurniawan.composetodolist.R
import com.wisnu.kurniawan.composetodolist.features.auth.data.AuthEnvironment
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgButton
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgIcon
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgIconButton
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgIconButtonSize
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgIconButtonVariant
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgPageLayout
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgSecondaryButton
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgTextField
import com.wisnu.kurniawan.composetodolist.foundation.viewmodel.HandleEffect
import com.wisnu.kurniawan.composetodolist.model.Theme

private val selectableThemes = listOf(
    Theme.SYSTEM,
    Theme.LIGHT,
    Theme.TWILIGHT,
    Theme.NIGHT,
    Theme.SUNRISE,
    Theme.AURORA
)

@Composable
fun SettingScreen(
    viewModel: SettingViewModel,
    onBackClick: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = remember(context) { context.findFragmentActivity() }
    val latestViewModel by rememberUpdatedState(viewModel)
    val biometricPrompt = remember(activity, context) {
        activity?.let { hostActivity ->
            BiometricPrompt(
                hostActivity,
                ContextCompat.getMainExecutor(context),
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        latestViewModel.dispatch(SettingAction.AuthEnableVerificationSucceeded)
                    }

                    override fun onAuthenticationFailed() {
                        // Keep waiting for final callback. Non-match does not mean prompt session ended.
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        latestViewModel.dispatch(SettingAction.AuthEnableVerificationFailed)
                    }
                }
            )
        }
    }

    HandleEffect(viewModel) { effect ->
        when (effect) {
            SettingEffect.NavigateBack -> onBackClick()
            is SettingEffect.ShowMessageRes -> {
                Toast.makeText(context, context.getString(effect.messageResId), Toast.LENGTH_SHORT).show()
            }
            SettingEffect.LaunchAuthEnableVerification -> {
                if (biometricPrompt == null) {
                    viewModel.dispatch(SettingAction.AuthEnableVerificationFailed)
                    return@HandleEffect
                }
                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle(context.getString(R.string.auth_gate_prompt_title))
                    .setSubtitle(context.getString(R.string.auth_gate_prompt_subtitle))
                    .setAllowedAuthenticators(AuthEnvironment.ALLOWED_AUTHENTICATORS)
                    .build()
                biometricPrompt.authenticate(promptInfo)
            }
        }
    }

    SettingContent(
        state = state,
        onRequestBack = { viewModel.dispatch(SettingAction.RequestBack) },
        onSendTestNotification = { viewModel.dispatch(SettingAction.ClickSendTestNotification) },
        onOpenThemeDialog = { viewModel.dispatch(SettingAction.OpenThemeDialog) },
        onCloseThemeDialog = { viewModel.dispatch(SettingAction.CloseThemeDialog) },
        onSelectTheme = { viewModel.dispatch(SettingAction.SelectTheme(it)) },
        onToggleAuthGate = { viewModel.dispatch(SettingAction.ToggleAuthGate(it)) },
        onConfirmDisableAuthGate = { viewModel.dispatch(SettingAction.ConfirmDisableAuthGate) },
        onCancelDisableAuthGate = { viewModel.dispatch(SettingAction.CancelDisableAuthGate) },
        onOpenFontDialog = { viewModel.dispatch(SettingAction.OpenFontDialog) },
        onCloseFontDialog = { viewModel.dispatch(SettingAction.CloseFontDialog) },
        onChangeFontDraftPercent = { viewModel.dispatch(SettingAction.ChangeFontDraftPercent(it)) },
        onSaveFontScale = { viewModel.dispatch(SettingAction.SaveFontScale) },
        onResetFontScaleDefault = { viewModel.dispatch(SettingAction.ResetFontScaleDefault) },
        onConfirmSaveFontAndClose = { viewModel.dispatch(SettingAction.ConfirmSaveFontAndClose) },
        onDiscardFontAndClose = { viewModel.dispatch(SettingAction.DiscardFontAndClose) },
        onContinueFontEditing = { viewModel.dispatch(SettingAction.ContinueFontEditing) },
        onOpenReminderDialog = { viewModel.dispatch(SettingAction.OpenReminderDialog) },
        onCloseReminderDialog = { viewModel.dispatch(SettingAction.CloseReminderDialog) },
        onChangeReminderDraftMinutes = { viewModel.dispatch(SettingAction.ChangeReminderDraftMinutes(it)) },
        onSaveReminderLeadMinutes = { viewModel.dispatch(SettingAction.SaveReminderLeadMinutes) },
        onResetReminderLeadMinutesDefault = { viewModel.dispatch(SettingAction.ResetReminderLeadMinutesDefault) },
        onToggleQuickFill = { viewModel.dispatch(SettingAction.ToggleQuickFill(it)) },
        onOpenQuickFillDurationDialog = { viewModel.dispatch(SettingAction.OpenQuickFillDurationDialog) },
        onCloseQuickFillDurationDialog = { viewModel.dispatch(SettingAction.CloseQuickFillDurationDialog) },
        onChangeQuickFillDurationDraftSeconds = { viewModel.dispatch(SettingAction.ChangeQuickFillDurationDraftSeconds(it)) },
        onSaveQuickFillDuration = { viewModel.dispatch(SettingAction.SaveQuickFillDuration) },
        onResetQuickFillDurationDefault = { viewModel.dispatch(SettingAction.ResetQuickFillDurationDefault) },
        onConfirmSaveReminderAndClose = { viewModel.dispatch(SettingAction.ConfirmSaveReminderAndClose) },
        onDiscardReminderAndClose = { viewModel.dispatch(SettingAction.DiscardReminderAndClose) },
        onContinueReminderEditing = { viewModel.dispatch(SettingAction.ContinueReminderEditing) }
    )
}

@Composable
private fun SettingContent(
    state: SettingState,
    onRequestBack: () -> Unit,
    onSendTestNotification: () -> Unit,
    onOpenThemeDialog: () -> Unit,
    onCloseThemeDialog: () -> Unit,
    onSelectTheme: (Theme) -> Unit,
    onToggleAuthGate: (Boolean) -> Unit,
    onConfirmDisableAuthGate: () -> Unit,
    onCancelDisableAuthGate: () -> Unit,
    onOpenFontDialog: () -> Unit,
    onCloseFontDialog: () -> Unit,
    onChangeFontDraftPercent: (String) -> Unit,
    onSaveFontScale: () -> Unit,
    onResetFontScaleDefault: () -> Unit,
    onConfirmSaveFontAndClose: () -> Unit,
    onDiscardFontAndClose: () -> Unit,
    onContinueFontEditing: () -> Unit,
    onOpenReminderDialog: () -> Unit,
    onCloseReminderDialog: () -> Unit,
    onChangeReminderDraftMinutes: (String) -> Unit,
    onSaveReminderLeadMinutes: () -> Unit,
    onResetReminderLeadMinutesDefault: () -> Unit,
    onToggleQuickFill: (Boolean) -> Unit,
    onOpenQuickFillDurationDialog: () -> Unit,
    onCloseQuickFillDurationDialog: () -> Unit,
    onChangeQuickFillDurationDraftSeconds: (String) -> Unit,
    onSaveQuickFillDuration: () -> Unit,
    onResetQuickFillDurationDefault: () -> Unit,
    onConfirmSaveReminderAndClose: () -> Unit,
    onDiscardReminderAndClose: () -> Unit,
    onContinueReminderEditing: () -> Unit,
) {
    BackHandler(onBack = onRequestBack)

    PgPageLayout(horizontalPadding = 2.dp, topContentPadding = 2.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PgIconButton(
                onClick = onRequestBack,
                color = MaterialTheme.colorScheme.secondary,
                size = PgIconButtonSize.Small,
                variant = PgIconButtonVariant.FilledSoft
            ) {
                PgIcon(imageVector = Icons.Rounded.ChevronLeft)
            }

            Spacer(modifier = Modifier.size(8.dp))

            Text(
                text = stringResource(R.string.setting_page_title),
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.size(8.dp))

        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                SettingItemRow(
                    title = stringResource(R.string.setting_auth_gate_title),
                    value = if (state.appliedAuthGateEnabled) {
                        stringResource(R.string.setting_auth_gate_subtitle_on)
                    } else {
                        stringResource(R.string.setting_auth_gate_subtitle_off)
                    },
                    trailing = {
                        Switch(
                            checked = state.appliedAuthGateEnabled,
                            onCheckedChange = onToggleAuthGate
                        )
                    }
                )
                SettingItemRow(
                    title = stringResource(R.string.setting_theme),
                    value = state.appliedTheme.toThemeDisplayText(),
                    onClick = onOpenThemeDialog
                )
                SettingItemRow(
                    title = stringResource(R.string.setting_font_size),
                    value = stringResource(R.string.setting_font_size_current, state.appliedPercent),
                    onClick = onOpenFontDialog
                )
                SettingItemRow(
                    title = stringResource(R.string.setting_reminder_lead_time),
                    value = stringResource(
                        R.string.setting_reminder_lead_time_current,
                        state.appliedReminderLeadMinutes
                    ),
                    onClick = onOpenReminderDialog
                )
                SettingItemRow(
                    title = stringResource(R.string.setting_quick_fill_title),
                    value = if (state.appliedQuickFillEnabled) {
                        stringResource(R.string.setting_quick_fill_subtitle_on)
                    } else {
                        stringResource(R.string.setting_quick_fill_subtitle_off)
                    },
                    trailing = {
                        Switch(
                            checked = state.appliedQuickFillEnabled,
                            onCheckedChange = onToggleQuickFill
                        )
                    }
                )
                if (state.appliedQuickFillEnabled) {
                    SettingItemRow(
                        title = stringResource(R.string.setting_quick_fill_hint_duration_title),
                        value = stringResource(
                            R.string.setting_quick_fill_hint_duration_current,
                            state.appliedQuickFillHintDurationSeconds
                        ),
                        onClick = onOpenQuickFillDurationDialog
                    )
                }
                SettingItemRow(
                    title = stringResource(R.string.setting_test_notification_title),
                    value = stringResource(R.string.setting_test_notification_subtitle),
                    onClick = onSendTestNotification
                )
            }
        }
    }

    if (state.showThemeDialog) {
        SettingThemeDialog(
            selectedTheme = state.appliedTheme,
            onDismiss = onCloseThemeDialog,
            onSelectTheme = onSelectTheme
        )
    }

    if (state.showFontDialog) {
        SettingFontScaleDialog(
            state = state,
            onDismiss = onCloseFontDialog,
            onChangeFontDraftPercent = onChangeFontDraftPercent,
            onSave = onSaveFontScale,
            onResetDefault = onResetFontScaleDefault
        )
    }

    if (state.showFontUnsavedDialog) {
        SettingUnsavedDialog(
            title = stringResource(R.string.setting_font_unsaved_title),
            message = stringResource(R.string.setting_font_unsaved_message),
            onSaveAndClose = onConfirmSaveFontAndClose,
            onDiscard = onDiscardFontAndClose,
            onContinueEdit = onContinueFontEditing
        )
    }

    if (state.showReminderDialog) {
        SettingReminderDialog(
            state = state,
            onDismiss = onCloseReminderDialog,
            onChangeReminderDraftMinutes = onChangeReminderDraftMinutes,
            onSave = onSaveReminderLeadMinutes,
            onResetDefault = onResetReminderLeadMinutesDefault
        )
    }

    if (state.showReminderUnsavedDialog) {
        SettingUnsavedDialog(
            title = stringResource(R.string.setting_reminder_unsaved_title),
            message = stringResource(R.string.setting_reminder_unsaved_message),
            onSaveAndClose = onConfirmSaveReminderAndClose,
            onDiscard = onDiscardReminderAndClose,
            onContinueEdit = onContinueReminderEditing
        )
    }

    if (state.showQuickFillDurationDialog) {
        SettingQuickFillDurationDialog(
            state = state,
            onDismiss = onCloseQuickFillDurationDialog,
            onChangeDraftSeconds = onChangeQuickFillDurationDraftSeconds,
            onSave = onSaveQuickFillDuration,
            onResetDefault = onResetQuickFillDurationDefault
        )
    }

    if (state.showAuthDisableConfirmDialog) {
        Dialog(onDismissRequest = onCancelDisableAuthGate) {
            Surface(
                shape = MaterialTheme.shapes.large,
                tonalElevation = 6.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.setting_auth_gate_disable_confirm_title),
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = stringResource(R.string.setting_auth_gate_disable_confirm_message),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        PgSecondaryButton(
                            modifier = Modifier.weight(1f),
                            onClick = onCancelDisableAuthGate
                        ) {
                            Text(
                                text = stringResource(R.string.todo_cancel),
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                        PgButton(
                            modifier = Modifier.weight(1f),
                            onClick = onConfirmDisableAuthGate
                        ) {
                            Text(text = stringResource(R.string.setting_auth_gate_disable_confirm_positive))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingThemeDialog(
    selectedTheme: Theme,
    onDismiss: () -> Unit,
    onSelectTheme: (Theme) -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = stringResource(R.string.setting_theme),
                    style = MaterialTheme.typography.titleSmall
                )

                selectableThemes.forEach { theme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectTheme(theme) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedTheme == theme,
                            onClick = { onSelectTheme(theme) }
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = theme.toThemeDisplayText(),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingItemRow(
    title: String,
    value: String,
    trailing: @Composable (() -> Unit)? = null,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (trailing == null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (trailing == null) {
            PgIcon(
                imageVector = Icons.Rounded.ChevronRight,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            trailing()
        }
    }
}

@Composable
private fun SettingFontScaleDialog(
    state: SettingState,
    onDismiss: () -> Unit,
    onChangeFontDraftPercent: (String) -> Unit,
    onSave: () -> Unit,
    onResetDefault: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.setting_font_size),
                    style = MaterialTheme.typography.titleSmall
                )

                Text(
                    text = stringResource(R.string.setting_font_size_current, state.appliedPercent),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                PgTextField(
                    value = state.fontDraftPercentText,
                    onValueChange = onChangeFontDraftPercent,
                    placeholderValue = stringResource(R.string.setting_font_size_input_placeholder),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    errorLabel = {
                        Text(
                            text = state.fontValidationError.toText(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    },
                    isError = state.fontValidationError != FontValidationError.None
                )

                Text(
                    text = stringResource(R.string.setting_font_size_input_placeholder),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                PgSecondaryButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onResetDefault
                ) {
                    Text(
                        text = stringResource(R.string.setting_font_size_reset_default),
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }

                PgButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onSave,
                    enabled = state.fontValidationError == FontValidationError.None
                ) {
                    Text(text = stringResource(R.string.todo_rename_list_positive))
                }
            }
        }
    }
}

@Composable
private fun SettingReminderDialog(
    state: SettingState,
    onDismiss: () -> Unit,
    onChangeReminderDraftMinutes: (String) -> Unit,
    onSave: () -> Unit,
    onResetDefault: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.setting_reminder_lead_time),
                    style = MaterialTheme.typography.titleSmall
                )

                Text(
                    text = stringResource(
                        R.string.setting_reminder_lead_time_current,
                        state.appliedReminderLeadMinutes
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                PgTextField(
                    value = state.reminderDraftMinutesText,
                    onValueChange = onChangeReminderDraftMinutes,
                    placeholderValue = stringResource(R.string.setting_reminder_lead_time_input_placeholder),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    errorLabel = {
                        Text(
                            text = state.reminderValidationError.toText(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    },
                    isError = state.reminderValidationError != ReminderValidationError.None
                )

                Text(
                    text = stringResource(R.string.setting_reminder_lead_time_input_placeholder),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                PgSecondaryButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onResetDefault
                ) {
                    Text(
                        text = stringResource(R.string.setting_reminder_lead_time_reset_default),
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }

                PgButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onSave,
                    enabled = state.reminderValidationError == ReminderValidationError.None
                ) {
                    Text(text = stringResource(R.string.todo_rename_list_positive))
                }
            }
        }
    }
}

@Composable
private fun SettingQuickFillDurationDialog(
    state: SettingState,
    onDismiss: () -> Unit,
    onChangeDraftSeconds: (String) -> Unit,
    onSave: () -> Unit,
    onResetDefault: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.setting_quick_fill_hint_duration_title),
                    style = MaterialTheme.typography.titleSmall
                )

                Text(
                    text = stringResource(
                        R.string.setting_quick_fill_hint_duration_current,
                        state.appliedQuickFillHintDurationSeconds
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                PgTextField(
                    value = state.quickFillDurationDraftSecondsText,
                    onValueChange = onChangeDraftSeconds,
                    placeholderValue = stringResource(R.string.setting_quick_fill_hint_duration_input_placeholder),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    errorLabel = {
                        Text(
                            text = state.quickFillDurationValidationError.toText(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    },
                    isError = state.quickFillDurationValidationError != QuickFillDurationValidationError.None
                )

                Text(
                    text = stringResource(R.string.setting_quick_fill_hint_duration_input_placeholder),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                PgSecondaryButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onResetDefault
                ) {
                    Text(
                        text = stringResource(R.string.setting_quick_fill_hint_duration_reset_default),
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }

                PgButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onSave,
                    enabled = state.quickFillDurationValidationError == QuickFillDurationValidationError.None
                ) {
                    Text(text = stringResource(R.string.todo_rename_list_positive))
                }
            }
        }
    }
}

@Composable
private fun FontValidationError.toText(): String {
    return when (this) {
        FontValidationError.None -> ""
        FontValidationError.Empty -> stringResource(R.string.setting_font_size_error_empty)
        FontValidationError.InvalidNumber -> stringResource(R.string.setting_font_size_error_invalid)
        FontValidationError.OutOfRange -> stringResource(R.string.setting_font_size_error_range)
    }
}

@Composable
private fun ReminderValidationError.toText(): String {
    return when (this) {
        ReminderValidationError.None -> ""
        ReminderValidationError.Empty -> stringResource(R.string.setting_reminder_lead_time_error_empty)
        ReminderValidationError.InvalidNumber -> stringResource(R.string.setting_reminder_lead_time_error_invalid)
        ReminderValidationError.OutOfRange -> stringResource(R.string.setting_reminder_lead_time_error_range)
    }
}

@Composable
private fun QuickFillDurationValidationError.toText(): String {
    return when (this) {
        QuickFillDurationValidationError.None -> ""
        QuickFillDurationValidationError.Empty -> stringResource(R.string.setting_quick_fill_hint_duration_error_empty)
        QuickFillDurationValidationError.InvalidNumber -> stringResource(R.string.setting_quick_fill_hint_duration_error_invalid)
        QuickFillDurationValidationError.OutOfRange -> stringResource(R.string.setting_quick_fill_hint_duration_error_range)
    }
}

@Composable
private fun Theme.toThemeDisplayText(): String {
    return when (this) {
        Theme.SYSTEM -> stringResource(R.string.setting_theme_automatic)
        Theme.LIGHT -> stringResource(R.string.setting_theme_light)
        Theme.TWILIGHT -> stringResource(R.string.setting_theme_twilight)
        Theme.NIGHT -> stringResource(R.string.setting_theme_night)
        Theme.SUNRISE -> stringResource(R.string.setting_theme_sunrise)
        Theme.AURORA -> stringResource(R.string.setting_theme_aurora)
    }
}

private tailrec fun Context.findFragmentActivity(): FragmentActivity? {
    return when (this) {
        is FragmentActivity -> this
        is ContextWrapper -> baseContext.findFragmentActivity()
        else -> null
    }
}

@Composable
private fun SettingUnsavedDialog(
    title: String,
    message: String,
    onSaveAndClose: () -> Unit,
    onDiscard: () -> Unit,
    onContinueEdit: () -> Unit,
) {
    Dialog(onDismissRequest = onContinueEdit) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PgSecondaryButton(
                        modifier = Modifier.weight(1f),
                        onClick = onDiscard
                    ) {
                        Text(
                            text = stringResource(R.string.todo_note_unsaved_discard),
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    }

                    PgButton(
                        modifier = Modifier.weight(1f),
                        onClick = onSaveAndClose
                    ) {
                        Text(text = stringResource(R.string.todo_rename_list_positive))
                    }
                }

                TextButton(
                    modifier = Modifier.align(Alignment.End),
                    onClick = onContinueEdit
                ) {
                    Text(text = stringResource(R.string.todo_note_unsaved_continue))
                }
            }
        }
    }
}
