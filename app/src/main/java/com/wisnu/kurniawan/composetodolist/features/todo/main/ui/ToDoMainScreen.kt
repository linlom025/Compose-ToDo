package com.wisnu.kurniawan.composetodolist.features.todo.main.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.wisnu.kurniawan.composetodolist.R
import com.wisnu.kurniawan.composetodolist.features.todo.main.data.QuadrantTask
import com.wisnu.kurniawan.composetodolist.foundation.extension.toDisplayableDate
import com.wisnu.kurniawan.composetodolist.foundation.extension.toDisplayableDateCompact
import com.wisnu.kurniawan.composetodolist.foundation.extension.toDisplayableTime
import com.wisnu.kurniawan.composetodolist.foundation.theme.ListBlue
import com.wisnu.kurniawan.composetodolist.foundation.theme.ListGreen
import com.wisnu.kurniawan.composetodolist.foundation.theme.ListOrange
import com.wisnu.kurniawan.composetodolist.foundation.theme.ListRed
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgButton
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgConfirmDeleteDialog
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgEmpty
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgIcon
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgIconButton
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgIconButtonSize
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgIconButtonVariant
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgSecondaryButton
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgDatePickerDialog
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgTimePickerDialog
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgToDoItemCell
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgQuadrantSelectorChip
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.itemInfoDisplayable
import com.wisnu.kurniawan.composetodolist.model.QuadrantDisplayNames
import com.wisnu.kurniawan.composetodolist.model.TaskQuadrant
import com.wisnu.kurniawan.composetodolist.model.ToDoStatus
import com.wisnu.kurniawan.composetodolist.model.ToDoTask
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.delay

@Composable
fun ToDoMainScreen(
    state: ToDoMainState,
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
    onTaskClick: (QuadrantTask) -> Unit,
    onTaskStatusClick: (ToDoTask) -> Unit,
    onTaskSwipeToDelete: (ToDoTask) -> Unit,
    onConfirmDeleteTask: () -> Unit,
    onDismissDeleteTask: () -> Unit,
    onQuadrantTitleLongClick: (TaskQuadrant) -> Unit,
    onConfirmClipboardHint: () -> Unit,
    onDismissClipboardHint: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 0.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            QuadrantCard(
                modifier = Modifier.weight(1f),
                quadrant = TaskQuadrant.Q1,
                quadrantTitle = state.quadrantDisplayNames.titleOf(TaskQuadrant.Q1),
                tasks = state.quadrants[TaskQuadrant.Q1].orEmpty(),
                onOpenCreateDialog = onOpenCreateDialog,
                onTaskClick = onTaskClick,
                onTaskStatusClick = onTaskStatusClick,
                onTaskSwipeToDelete = onTaskSwipeToDelete,
                onQuadrantTitleLongClick = onQuadrantTitleLongClick
            )

            QuadrantCard(
                modifier = Modifier.weight(1f),
                quadrant = TaskQuadrant.Q2,
                quadrantTitle = state.quadrantDisplayNames.titleOf(TaskQuadrant.Q2),
                tasks = state.quadrants[TaskQuadrant.Q2].orEmpty(),
                onOpenCreateDialog = onOpenCreateDialog,
                onTaskClick = onTaskClick,
                onTaskStatusClick = onTaskStatusClick,
                onTaskSwipeToDelete = onTaskSwipeToDelete,
                onQuadrantTitleLongClick = onQuadrantTitleLongClick
            )
        }

        Spacer(Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            QuadrantCard(
                modifier = Modifier.weight(1f),
                quadrant = TaskQuadrant.Q3,
                quadrantTitle = state.quadrantDisplayNames.titleOf(TaskQuadrant.Q3),
                tasks = state.quadrants[TaskQuadrant.Q3].orEmpty(),
                onOpenCreateDialog = onOpenCreateDialog,
                onTaskClick = onTaskClick,
                onTaskStatusClick = onTaskStatusClick,
                onTaskSwipeToDelete = onTaskSwipeToDelete,
                onQuadrantTitleLongClick = onQuadrantTitleLongClick
            )

            QuadrantCard(
                modifier = Modifier.weight(1f),
                quadrant = TaskQuadrant.Q4,
                quadrantTitle = state.quadrantDisplayNames.titleOf(TaskQuadrant.Q4),
                tasks = state.quadrants[TaskQuadrant.Q4].orEmpty(),
                onOpenCreateDialog = onOpenCreateDialog,
                onTaskClick = onTaskClick,
                onTaskStatusClick = onTaskStatusClick,
                onTaskSwipeToDelete = onTaskSwipeToDelete,
                onQuadrantTitleLongClick = onQuadrantTitleLongClick
            )
        }
    }

    if (state.isCreateDialogVisible) {
        CreateTaskDialog(
            state = state,
            onNameChange = onCreateTaskNameChange,
            onQuadrantChange = onCreateQuadrantChange,
            onDueEnabledChange = onCreateDueEnabledChange,
            onOpenDueDatePicker = onOpenCreateDueDatePicker,
            onDismissDueDatePicker = onDismissCreateDueDatePicker,
            onSelectDueDate = onSelectCreateDueDate,
            onOpenDueTimePicker = onOpenCreateDueTimePicker,
            onDismissDueTimePicker = onDismissCreateDueTimePicker,
            onSelectDueTime = onSelectCreateDueTime,
            onNoteChange = onCreateNoteChange,
            onDismiss = onDismissCreateDialog,
            onConfirm = onConfirmCreate,
            quadrantDisplayNames = state.quadrantDisplayNames
        )
    }

    if (state.showDeleteTaskConfirmDialog) {
        PgConfirmDeleteDialog(
            onConfirm = onConfirmDeleteTask,
            onDismiss = onDismissDeleteTask
        )
    }

    if (state.showClipboardSoftImportHint) {
        ClipboardImportHintOverlay(
            title = state.pendingSoftClipboardCandidate?.title.orEmpty(),
            durationSeconds = state.quickFillHintDurationSeconds,
            onConfirm = onConfirmClipboardHint,
            onDismiss = onDismissClipboardHint
        )
    }
}

@Composable
private fun QuadrantCard(
    modifier: Modifier,
    quadrant: TaskQuadrant,
    quadrantTitle: String,
    tasks: List<QuadrantTask>,
    onOpenCreateDialog: (TaskQuadrant) -> Unit,
    onTaskClick: (QuadrantTask) -> Unit,
    onTaskStatusClick: (ToDoTask) -> Unit,
    onTaskSwipeToDelete: (ToDoTask) -> Unit,
    onQuadrantTitleLongClick: (TaskQuadrant) -> Unit,
) {
    val resources = LocalContext.current.resources
    val titleColor = quadrant.toColor()

    Surface(
        modifier = modifier.fillMaxSize(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = quadrantTitle,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                    color = titleColor,
                    modifier = Modifier.pointerInput(quadrant) {
                        detectTapGestures(
                            onLongPress = { onQuadrantTitleLongClick(quadrant) }
                        )
                    }
                )
                TinyAddButton(onClick = { onOpenCreateDialog(quadrant) })
            }

            Spacer(Modifier.height(8.dp))

            if (tasks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize()) {
                    PgEmpty(
                        text = stringResource(R.string.todo_no_task),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tasks, key = { it.task.id }) { item ->
                        val task = item.task
                        PgToDoItemCell(
                            name = task.name,
                            color = titleColor,
                            contentPaddingValues = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            leftIcon = if (task.status == ToDoStatus.COMPLETE) {
                                Icons.Rounded.CheckCircle
                            } else {
                                Icons.Rounded.RadioButtonUnchecked
                            },
                            textDecoration = if (task.status == ToDoStatus.COMPLETE) {
                                TextDecoration.LineThrough
                            } else {
                                TextDecoration.None
                            },
                            onClick = { onTaskClick(item) },
                            onSwipeToDelete = { onTaskSwipeToDelete(task) },
                            onStatusClick = { onTaskStatusClick(task) },
                            info = task.itemInfoDisplayable(resources, MaterialTheme.colorScheme.error),
                            undoEnabled = false,
                            onRequestDelete = { onTaskSwipeToDelete(task) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TinyAddButton(
    onClick: () -> Unit
) {
    PgIconButton(
        onClick = onClick,
        modifier = Modifier.size(20.dp),
        size = PgIconButtonSize.Small,
        enforceMinSize = false,
        variant = PgIconButtonVariant.FilledSoft,
        color = MaterialTheme.colorScheme.secondary
    ) {
        PgIcon(imageVector = Icons.Rounded.Add, modifier = Modifier.size(14.dp))
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun CreateTaskDialog(
    state: ToDoMainState,
    onNameChange: (TextFieldValue) -> Unit,
    onQuadrantChange: (TaskQuadrant) -> Unit,
    onDueEnabledChange: (Boolean) -> Unit,
    onOpenDueDatePicker: () -> Unit,
    onDismissDueDatePicker: () -> Unit,
    onSelectDueDate: (LocalDate?) -> Unit,
    onOpenDueTimePicker: () -> Unit,
    onDismissDueTimePicker: () -> Unit,
    onSelectDueTime: (LocalTime) -> Unit,
    onNoteChange: (TextFieldValue) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    quadrantDisplayNames: QuadrantDisplayNames,
) {
    if (state.showCreateDueDatePicker) {
        PgDatePickerDialog(
            initialDate = state.createDueDate,
            onDismiss = onDismissDueDatePicker,
            onConfirm = { date ->
                onSelectDueDate(date)
            }
        )
    }

    if (state.showCreateDueTimePicker) {
        val timeState = androidx.compose.material3.rememberTimePickerState(
            initialHour = state.createDueTime.hour,
            initialMinute = state.createDueTime.minute,
            is24Hour = true
        )
        PgTimePickerDialog(
            onCancel = onDismissDueTimePicker,
            onConfirm = { onSelectDueTime(LocalTime.of(timeState.hour, timeState.minute)) }
        ) {
            androidx.compose.material3.TimePicker(state = timeState)
        }
    }

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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = state.createQuadrant.toTitle(quadrantDisplayNames),
                    style = MaterialTheme.typography.titleSmall
                )

                if (!state.isCreateQuadrantLocked) {
                    CreateQuadrantSelector(
                        selectedQuadrant = state.createQuadrant,
                        displayNames = quadrantDisplayNames,
                        onSelectQuadrant = onQuadrantChange
                    )
                }

                OutlinedTextField(
                    value = state.createTaskName,
                    onValueChange = onNameChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 56.dp),
                    maxLines = 2,
                    singleLine = false,
                    placeholder = { Text(text = stringResource(R.string.todo_add_task)) },
                    shape = MaterialTheme.shapes.medium,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.secondary,
                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.todo_add_due_date_task),
                        style = MaterialTheme.typography.titleSmall
                    )
                    Switch(
                        checked = state.createDueEnabled,
                        onCheckedChange = onDueEnabledChange
                    )
                }

                if (state.createDueEnabled) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CompactMetaButton(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.Event,
                            text = state.createDueDate.toDisplayableDateCompact(),
                            onClick = onOpenDueDatePicker
                        )

                        CompactMetaButton(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.Schedule,
                            text = state.createDueTime.toDisplayableTime(),
                            onClick = onOpenDueTimePicker
                        )
                    }
                }

                OutlinedTextField(
                    value = state.createNote,
                    onValueChange = onNoteChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 110.dp),
                    maxLines = 4,
                    placeholder = { Text(text = stringResource(R.string.todo_add_note)) },
                    shape = MaterialTheme.shapes.medium,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.secondary,
                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PgSecondaryButton(
                        modifier = Modifier.weight(1f),
                        onClick = onDismiss
                    ) {
                        Text(
                            text = stringResource(R.string.todo_cancel),
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    }

                    PgButton(
                        modifier = Modifier.weight(1f),
                        onClick = onConfirm,
                        enabled = state.validCreateTaskName
                    ) {
                        Text(
                            text = stringResource(R.string.todo_add),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateQuadrantSelector(
    selectedQuadrant: TaskQuadrant,
    displayNames: QuadrantDisplayNames,
    onSelectQuadrant: (TaskQuadrant) -> Unit,
) {
    val quadrants = listOf(
        TaskQuadrant.Q1,
        TaskQuadrant.Q2,
        TaskQuadrant.Q3,
        TaskQuadrant.Q4
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.todo_quadrant_selector_title),
            style = MaterialTheme.typography.titleSmall
        )
        quadrants.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { quadrant ->
                    PgQuadrantSelectorChip(
                        modifier = Modifier.weight(1f),
                        selected = selectedQuadrant == quadrant,
                        onClick = { onSelectQuadrant(quadrant) },
                        text = quadrant.toTitle(displayNames)
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactMetaButton(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
    height: Dp = 44.dp
) {
    PgSecondaryButton(
        onClick = onClick,
        modifier = modifier.height(height)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            PgIcon(imageVector = icon, modifier = Modifier.size(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun TaskQuadrant.toTitle(displayNames: QuadrantDisplayNames): String {
    return displayNames.titleOf(this)
}

private fun TaskQuadrant.toColor(): Color {
    return when (this) {
        TaskQuadrant.Q1 -> ListRed
        TaskQuadrant.Q2 -> ListBlue
        TaskQuadrant.Q3 -> ListOrange
        TaskQuadrant.Q4 -> ListGreen
    }
}

@Composable
private fun ClipboardImportHintOverlay(
    title: String,
    durationSeconds: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val safeDurationSeconds = durationSeconds.coerceIn(3, 15)
    val cardModifier = remember {
        Modifier.pointerInput(Unit) {
            detectTapGestures(onTap = {})
        }
    }

    LaunchedEffect(title, safeDurationSeconds) {
        if (title.isBlank()) return@LaunchedEffect
        delay(safeDurationSeconds * 1_000L)
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(title) {
                detectTapGestures(onTap = { onDismiss() })
            }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = cardModifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.todo_clipboard_soft_import_message, title),
                    style = MaterialTheme.typography.bodyMedium
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PgSecondaryButton(
                        modifier = Modifier.weight(1f),
                        onClick = onDismiss
                    ) {
                        Text(
                            text = stringResource(R.string.todo_clipboard_soft_import_ignore),
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    }

                    PgButton(
                        modifier = Modifier.weight(1f),
                        onClick = onConfirm
                    ) {
                        Text(text = stringResource(R.string.todo_clipboard_soft_import_action))
                    }
                }
            }
        }
    }
}
