package com.wisnu.kurniawan.composetodolist.features.dashboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wisnu.kurniawan.composetodolist.R
import com.wisnu.kurniawan.composetodolist.features.todo.main.ui.ToDoMainAction
import com.wisnu.kurniawan.composetodolist.features.todo.main.ui.ToDoMainScreen
import com.wisnu.kurniawan.composetodolist.features.todo.main.ui.ToDoMainState
import com.wisnu.kurniawan.composetodolist.features.todo.main.ui.ToDoMainViewModel
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgButton
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgIcon
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgIconButton
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgIconButtonSize
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgIconButtonVariant
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgPageLayout
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgSecondaryButton
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgTextField
import com.wisnu.kurniawan.composetodolist.model.TaskQuadrant
import com.wisnu.kurniawan.composetodolist.model.Theme
import com.wisnu.kurniawan.composetodolist.model.ToDoTask
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    toDoMainViewModel: ToDoMainViewModel,
    onCalendarClick: () -> Unit,
    onSettingClick: () -> Unit,
    onTaskItemClick: (String, String) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val todoMainState by toDoMainViewModel.state.collectAsStateWithLifecycle()

    DashboardContent(
        theme = state.theme,
        showCompleted = todoMainState.showCompleted,
        todoMainState = todoMainState,
        appDisplayTitle = state.displayNameConfig.appTitle,
        showDisplayNameDialog = state.showDisplayNameDialog,
        displayNameEditTarget = state.displayNameEditTarget,
        displayNameDraft = state.displayNameDraft,
        isDisplayNameDraftValid = state.isDraftValid,
        onCalendarClick = onCalendarClick,
        onSettingClick = onSettingClick,
        onToggleTheme = { viewModel.dispatch(DashboardAction.ToggleTheme) },
        onOpenDisplayNameDialog = { viewModel.dispatch(DashboardAction.OpenDisplayNameDialog(it)) },
        onDismissDisplayNameDialog = { viewModel.dispatch(DashboardAction.DismissDisplayNameDialog) },
        onChangeDisplayNameDraft = { viewModel.dispatch(DashboardAction.ChangeDisplayNameDraft(it)) },
        onSaveDisplayNames = { viewModel.dispatch(DashboardAction.SaveCurrentDisplayName) },
        onResetDisplayNamesDefault = { viewModel.dispatch(DashboardAction.ResetCurrentDisplayNameDefault) },
        onToggleShowCompleted = {
            toDoMainViewModel.dispatch(ToDoMainAction.ToggleShowCompleted)
        },
        onTaskItemClick = onTaskItemClick,
        onOpenCreateDialog = { toDoMainViewModel.dispatch(ToDoMainAction.OpenCreateDialog(it)) },
        onCreateTaskNameChange = { toDoMainViewModel.dispatch(ToDoMainAction.ChangeCreateTaskName(it)) },
        onCreateQuadrantChange = { toDoMainViewModel.dispatch(ToDoMainAction.ChangeCreateQuadrant(it)) },
        onCreateDueEnabledChange = { toDoMainViewModel.dispatch(ToDoMainAction.ChangeCreateDueEnabled(it)) },
        onOpenCreateDueDatePicker = { toDoMainViewModel.dispatch(ToDoMainAction.OpenCreateDueDatePicker) },
        onDismissCreateDueDatePicker = { toDoMainViewModel.dispatch(ToDoMainAction.DismissCreateDueDatePicker) },
        onSelectCreateDueDate = { toDoMainViewModel.dispatch(ToDoMainAction.SelectCreateDueDate(it)) },
        onOpenCreateDueTimePicker = { toDoMainViewModel.dispatch(ToDoMainAction.OpenCreateDueTimePicker) },
        onDismissCreateDueTimePicker = { toDoMainViewModel.dispatch(ToDoMainAction.DismissCreateDueTimePicker) },
        onSelectCreateDueTime = { toDoMainViewModel.dispatch(ToDoMainAction.SelectCreateDueTime(it)) },
        onCreateNoteChange = { toDoMainViewModel.dispatch(ToDoMainAction.ChangeCreateNote(it)) },
        onConfirmCreate = { toDoMainViewModel.dispatch(ToDoMainAction.ConfirmCreate) },
        onDismissCreateDialog = { toDoMainViewModel.dispatch(ToDoMainAction.DismissCreateDialog) },
        onTaskStatusItemClick = {
            toDoMainViewModel.dispatch(ToDoMainAction.ToggleTaskStatus(it))
        },
        onTaskSwipeToDelete = {
            toDoMainViewModel.dispatch(ToDoMainAction.RequestDeleteTask(it))
        },
        onConfirmDeleteTask = { toDoMainViewModel.dispatch(ToDoMainAction.ConfirmDeleteTask) },
        onDismissDeleteTask = { toDoMainViewModel.dispatch(ToDoMainAction.DismissDeleteTask) },
        onConfirmClipboardHint = { toDoMainViewModel.dispatch(ToDoMainAction.ConfirmImportClipboardHint) },
        onDismissClipboardHint = { toDoMainViewModel.dispatch(ToDoMainAction.DismissImportClipboardHint) },
    )
}

@Composable
private fun DashboardContent(
    theme: Theme,
    showCompleted: Boolean,
    todoMainState: ToDoMainState,
    appDisplayTitle: String,
    showDisplayNameDialog: Boolean,
    displayNameEditTarget: DisplayNameEditTarget?,
    displayNameDraft: String,
    isDisplayNameDraftValid: Boolean,
    onCalendarClick: () -> Unit,
    onSettingClick: () -> Unit,
    onToggleTheme: () -> Unit,
    onOpenDisplayNameDialog: (DisplayNameEditTarget) -> Unit,
    onDismissDisplayNameDialog: () -> Unit,
    onChangeDisplayNameDraft: (String) -> Unit,
    onSaveDisplayNames: () -> Unit,
    onResetDisplayNamesDefault: () -> Unit,
    onToggleShowCompleted: () -> Unit,
    onTaskItemClick: (String, String) -> Unit,
    onOpenCreateDialog: (TaskQuadrant) -> Unit,
    onCreateTaskNameChange: (TextFieldValue) -> Unit,
    onCreateQuadrantChange: (TaskQuadrant) -> Unit,
    onCreateDueEnabledChange: (Boolean) -> Unit,
    onOpenCreateDueDatePicker: () -> Unit,
    onDismissCreateDueDatePicker: () -> Unit,
    onSelectCreateDueDate: (LocalDate?) -> Unit,
    onOpenCreateDueTimePicker: () -> Unit,
    onDismissCreateDueTimePicker: () -> Unit,
    onSelectCreateDueTime: (LocalTime) -> Unit,
    onCreateNoteChange: (TextFieldValue) -> Unit,
    onConfirmCreate: () -> Unit,
    onDismissCreateDialog: () -> Unit,
    onTaskStatusItemClick: (ToDoTask) -> Unit,
    onTaskSwipeToDelete: (ToDoTask) -> Unit,
    onConfirmDeleteTask: () -> Unit,
    onDismissDeleteTask: () -> Unit,
    onConfirmClipboardHint: () -> Unit,
    onDismissClipboardHint: () -> Unit,
) {
    PgPageLayout(horizontalPadding = 2.dp, topContentPadding = 2.dp) {
        Row(
            modifier = Modifier
                .padding(horizontal = 2.dp, vertical = 2.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = appDisplayTitle,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.pointerInput(Unit) {
                    detectTapGestures(onLongPress = { onOpenDisplayNameDialog(DisplayNameEditTarget.AppTitle) })
                }
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                PgIconButton(
                    onClick = onCalendarClick,
                    color = MaterialTheme.colorScheme.secondary,
                    size = PgIconButtonSize.Small,
                    variant = PgIconButtonVariant.FilledSoft
                ) {
                    PgIcon(imageVector = Icons.Rounded.CalendarMonth)
                }

                Spacer(Modifier.width(6.dp))

                PgIconButton(
                    onClick = onSettingClick,
                    color = MaterialTheme.colorScheme.secondary,
                    size = PgIconButtonSize.Small,
                    variant = PgIconButtonVariant.FilledSoft
                ) {
                    PgIcon(imageVector = Icons.Rounded.Settings)
                }

                Spacer(Modifier.width(6.dp))

                PgIconButton(
                    onClick = onToggleShowCompleted,
                    color = MaterialTheme.colorScheme.secondary,
                    size = PgIconButtonSize.Small,
                    variant = PgIconButtonVariant.FilledSoft
                ) {
                    PgIcon(
                        imageVector = if (showCompleted) {
                            Icons.Filled.Visibility
                        } else {
                            Icons.Filled.VisibilityOff
                        }
                    )
                }

                Spacer(Modifier.width(6.dp))

                PgIconButton(
                    onClick = onToggleTheme,
                    color = MaterialTheme.colorScheme.secondary,
                    size = PgIconButtonSize.Small,
                    variant = PgIconButtonVariant.FilledSoft
                ) {
                    PgIcon(
                        imageVector = if (theme == Theme.NIGHT) {
                            Icons.Rounded.WbSunny
                        } else {
                            Icons.Rounded.DarkMode
                        }
                    )
                }
            }
        }

        ToDoMainScreen(
            state = todoMainState,
            onOpenCreateDialog = onOpenCreateDialog,
            onCreateTaskNameChange = onCreateTaskNameChange,
            onCreateQuadrantChange = onCreateQuadrantChange,
            onCreateDueEnabledChange = onCreateDueEnabledChange,
            onOpenCreateDueDatePicker = onOpenCreateDueDatePicker,
            onDismissCreateDueDatePicker = onDismissCreateDueDatePicker,
            onSelectCreateDueDate = onSelectCreateDueDate,
            onOpenCreateDueTimePicker = onOpenCreateDueTimePicker,
            onDismissCreateDueTimePicker = onDismissCreateDueTimePicker,
            onSelectCreateDueTime = onSelectCreateDueTime,
            onCreateNoteChange = onCreateNoteChange,
            onConfirmCreate = onConfirmCreate,
            onDismissCreateDialog = onDismissCreateDialog,
            onTaskClick = { onTaskItemClick(it.task.id, it.listId) },
            onTaskStatusClick = onTaskStatusItemClick,
            onTaskSwipeToDelete = onTaskSwipeToDelete,
            onConfirmDeleteTask = onConfirmDeleteTask,
            onDismissDeleteTask = onDismissDeleteTask,
            onConfirmClipboardHint = onConfirmClipboardHint,
            onDismissClipboardHint = onDismissClipboardHint,
            onQuadrantTitleLongClick = { quadrant ->
                onOpenDisplayNameDialog(quadrant.toEditTarget())
            }
        )
    }

    if (showDisplayNameDialog) {
        DisplayNameDialog(
            target = displayNameEditTarget,
            draft = displayNameDraft,
            isValid = isDisplayNameDraftValid,
            onDismiss = onDismissDisplayNameDialog,
            onChangeDraft = onChangeDisplayNameDraft,
            onSave = onSaveDisplayNames,
            onResetDefault = onResetDisplayNamesDefault
        )
    }
}

@Composable
private fun DisplayNameDialog(
    target: DisplayNameEditTarget?,
    draft: String,
    isValid: Boolean,
    onDismiss: () -> Unit,
    onChangeDraft: (String) -> Unit,
    onSave: () -> Unit,
    onResetDefault: () -> Unit,
) {
    val currentTarget = target ?: return
    Dialog(onDismissRequest = onDismiss) {
        androidx.compose.material3.Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(currentTarget.toLabelRes()),
                    style = MaterialTheme.typography.titleSmall
                )

                PgTextField(
                    value = draft,
                    onValueChange = onChangeDraft,
                    placeholderValue = stringResource(currentTarget.toLabelRes())
                )

                if (!isValid) {
                    Text(
                        text = stringResource(R.string.dashboard_display_name_error_blank),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                PgSecondaryButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onResetDefault
                ) {
                    Text(
                        text = stringResource(R.string.dashboard_display_name_reset_default),
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }

                PgButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onSave,
                    enabled = isValid
                ) {
                    Text(text = stringResource(R.string.todo_rename_list_positive))
                }
            }
        }
    }
}

private fun TaskQuadrant.toEditTarget(): DisplayNameEditTarget {
    return when (this) {
        TaskQuadrant.Q1 -> DisplayNameEditTarget.Q1
        TaskQuadrant.Q2 -> DisplayNameEditTarget.Q2
        TaskQuadrant.Q3 -> DisplayNameEditTarget.Q3
        TaskQuadrant.Q4 -> DisplayNameEditTarget.Q4
    }
}

private fun DisplayNameEditTarget.toLabelRes(): Int {
    return when (this) {
        DisplayNameEditTarget.AppTitle -> R.string.dashboard_display_name_app_title
        DisplayNameEditTarget.Q1 -> R.string.dashboard_display_name_q1
        DisplayNameEditTarget.Q2 -> R.string.dashboard_display_name_q2
        DisplayNameEditTarget.Q3 -> R.string.dashboard_display_name_q3
        DisplayNameEditTarget.Q4 -> R.string.dashboard_display_name_q4
    }
}
