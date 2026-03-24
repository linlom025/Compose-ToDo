package com.wisnu.kurniawan.composetodolist.features.todo.main.ui

import androidx.compose.ui.text.input.TextFieldValue
import com.wisnu.kurniawan.composetodolist.model.TaskQuadrant
import com.wisnu.kurniawan.composetodolist.model.ToDoTask
import java.time.LocalDate
import java.time.LocalTime

sealed class ToDoMainAction {
    object ToggleShowCompleted : ToDoMainAction()
    data class OpenCreateDialog(val quadrant: TaskQuadrant) : ToDoMainAction()
    data class ChangeCreateQuadrant(val quadrant: TaskQuadrant) : ToDoMainAction()
    data class ChangeCreateTaskName(val value: TextFieldValue) : ToDoMainAction()
    data class ChangeCreateDueEnabled(val enabled: Boolean) : ToDoMainAction()
    object OpenCreateDueDatePicker : ToDoMainAction()
    object DismissCreateDueDatePicker : ToDoMainAction()
    data class SelectCreateDueDate(val date: LocalDate?) : ToDoMainAction()
    object OpenCreateDueTimePicker : ToDoMainAction()
    object DismissCreateDueTimePicker : ToDoMainAction()
    data class SelectCreateDueTime(val time: LocalTime) : ToDoMainAction()
    data class ChangeCreateNote(val value: TextFieldValue) : ToDoMainAction()
    object ConfirmCreate : ToDoMainAction()
    object DismissCreateDialog : ToDoMainAction()
    object ConfirmImportClipboardCandidate : ToDoMainAction()
    object DismissImportClipboardCandidate : ToDoMainAction()
    object ConfirmImportClipboardSoftCandidate : ToDoMainAction()
    object DismissImportClipboardSoftCandidate : ToDoMainAction()
    data class ToggleTaskStatus(val task: ToDoTask) : ToDoMainAction()
    data class RequestDeleteTask(val task: ToDoTask) : ToDoMainAction()
    object ConfirmDeleteTask : ToDoMainAction()
    object DismissDeleteTask : ToDoMainAction()
}
