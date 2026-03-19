package com.wisnu.kurniawan.composetodolist.features.dashboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wisnu.kurniawan.composetodolist.R
import com.wisnu.kurniawan.composetodolist.features.todo.main.ui.ToDoMainAction
import com.wisnu.kurniawan.composetodolist.features.todo.main.ui.ToDoMainScreen
import com.wisnu.kurniawan.composetodolist.features.todo.main.ui.ToDoMainState
import com.wisnu.kurniawan.composetodolist.features.todo.main.ui.ToDoMainViewModel
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgIcon
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgIconButton
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgIconButtonSize
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgIconButtonVariant
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgPageLayout
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
    onTaskItemClick: (String, String) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val todoMainState by toDoMainViewModel.state.collectAsStateWithLifecycle()

    DashboardContent(
        theme = state.theme,
        showCompleted = todoMainState.showCompleted,
        todoMainState = todoMainState,
        onCalendarClick = onCalendarClick,
        onToggleTheme = { viewModel.dispatch(DashboardAction.ToggleTheme) },
        onToggleShowCompleted = {
            toDoMainViewModel.dispatch(ToDoMainAction.ToggleShowCompleted)
        },
        onTaskItemClick = onTaskItemClick,
        onOpenCreateDialog = { toDoMainViewModel.dispatch(ToDoMainAction.OpenCreateDialog(it)) },
        onCreateTaskNameChange = { toDoMainViewModel.dispatch(ToDoMainAction.ChangeCreateTaskName(it)) },
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
    )
}

@Composable
private fun DashboardContent(
    theme: Theme,
    showCompleted: Boolean,
    todoMainState: ToDoMainState,
    onCalendarClick: () -> Unit,
    onToggleTheme: () -> Unit,
    onToggleShowCompleted: () -> Unit,
    onTaskItemClick: (String, String) -> Unit,
    onOpenCreateDialog: (TaskQuadrant) -> Unit,
    onCreateTaskNameChange: (TextFieldValue) -> Unit,
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
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleMedium
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
            onDismissDeleteTask = onDismissDeleteTask
        )
    }
}
