package com.wisnu.kurniawan.composetodolist.features.todo.step.ui

import androidx.compose.ui.text.input.TextFieldValue
import com.wisnu.kurniawan.composetodolist.model.TaskQuadrant
import com.wisnu.kurniawan.composetodolist.model.ToDoStep
import java.time.LocalDate
import java.time.LocalTime

enum class NoteExitTarget {
    EDIT,
    SCREEN
}

sealed class StepAction {
    sealed class TaskAction : StepAction() {
        object ClickSave : TaskAction()
        object OnShow : TaskAction()
        object OnToggleStatus : TaskAction()
        object RequestDelete : TaskAction()
        object ConfirmDelete : TaskAction()
        object DismissDelete : TaskAction()
        data class ChangeTaskName(val name: TextFieldValue) : TaskAction()
        data class SelectRepeat(val repeatItem: ToDoRepeatItem) : TaskAction()
        data class SelectDueDate(val date: LocalDate?) : TaskAction()
        data class ChangeDueDate(val selected: Boolean) : TaskAction()
        object EditDueDate : TaskAction()
        object DismissDueDatePicker : TaskAction()
        data class ChangeDueTime(val selected: Boolean) : TaskAction()
        object EditDueTime : TaskAction()
        object DismissDueTimePicker : TaskAction()
        data class SelectDueTime(val time: LocalTime) : TaskAction()
        object OpenQuadrantDialog : TaskAction()
        object DismissQuadrantDialog : TaskAction()
        data class SelectQuadrant(val quadrant: TaskQuadrant) : TaskAction()
    }

    sealed class StepItemAction : StepAction() {
        sealed class Edit : StepItemAction() {
            data class ClickSave(val stepId: String) : Edit()
            data class OnShow(val stepId: String) : Edit()
            data class ChangeStepName(val name: TextFieldValue) : Edit()

            data class OnToggleStatus(val step: ToDoStep) : Edit()
            data class Delete(val step: ToDoStep) : Edit()
        }

        sealed class Create : StepItemAction() {
            object ClickSubmit : Create()
            object ClickImeDone : Create()
            object OnShow : Create()
            data class ChangeStepName(val name: TextFieldValue) : Create()
        }
    }

    sealed class NoteAction : StepAction() {
        object StartEdit : NoteAction()
        data class ChangeNote(val note: TextFieldValue) : NoteAction()
        object ClickDone : NoteAction()
        object ClickCancel : NoteAction()
        data class RequestExitWithUnsaved(val target: NoteExitTarget) : NoteAction()
        object ConfirmSaveAndExit : NoteAction()
        object DiscardAndExit : NoteAction()
        object ContinueEdit : NoteAction()
    }
}
