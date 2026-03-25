package com.wisnu.kurniawan.composetodolist.features.todo.main.ui

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewModelScope
import com.wisnu.foundation.coreviewmodel.StatefulViewModel
import com.wisnu.kurniawan.composetodolist.features.todo.main.data.IToDoMainEnvironment
import com.wisnu.kurniawan.composetodolist.foundation.share.ClipboardDecisionLevel
import com.wisnu.kurniawan.composetodolist.foundation.share.SharedTaskDraft
import com.wisnu.kurniawan.composetodolist.foundation.share.SharedTaskDraftRepository
import com.wisnu.kurniawan.composetodolist.foundation.share.SharedTextTaskParser
import com.wisnu.kurniawan.composetodolist.model.TaskQuadrant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class ToDoMainViewModel @Inject constructor(
    todoMainEnvironment: IToDoMainEnvironment
) : StatefulViewModel<ToDoMainState, Unit, ToDoMainAction, IToDoMainEnvironment>(ToDoMainState(), todoMainEnvironment) {

    init {
        initShowCompleted()
        initQuadrants()
        initQuadrantDisplayNames()
        initQuickFillHintDuration()
        initClipboardAdaptiveBias()
        initSharedTaskDraft()
        initClipboardTaskCandidate()
    }

    override fun dispatch(action: ToDoMainAction) {
        when (action) {
            ToDoMainAction.ToggleShowCompleted -> {
                viewModelScope.launch {
                    val next = !state.value.showCompleted
                    setState { copy(showCompleted = next) }
                    environment.setShowCompleted(next)
                }
            }

            is ToDoMainAction.OpenCreateDialog -> {
                viewModelScope.launch {
                    openCreateDialogWithPrefill(
                        quadrant = action.quadrant,
                        taskName = "",
                        note = "",
                        isQuadrantLocked = true
                    )
                }
            }

            is ToDoMainAction.ChangeCreateQuadrant -> {
                viewModelScope.launch {
                    if (state.value.isCreateQuadrantLocked) return@launch
                    setState { copy(createQuadrant = action.quadrant) }
                }
            }

            is ToDoMainAction.ChangeCreateTaskName -> {
                viewModelScope.launch {
                    setState { copy(createTaskName = action.value) }
                }
            }

            is ToDoMainAction.ChangeCreateDueEnabled -> {
                viewModelScope.launch {
                    setState { copy(createDueEnabled = action.enabled) }
                }
            }

            ToDoMainAction.OpenCreateDueDatePicker -> {
                viewModelScope.launch {
                    setState { copy(showCreateDueDatePicker = true) }
                }
            }

            ToDoMainAction.DismissCreateDueDatePicker -> {
                viewModelScope.launch {
                    setState { copy(showCreateDueDatePicker = false) }
                }
            }

            is ToDoMainAction.SelectCreateDueDate -> {
                viewModelScope.launch {
                    action.date ?: return@launch
                    setState {
                        copy(
                            createDueDate = action.date,
                            showCreateDueDatePicker = false,
                            createDueEnabled = true
                        )
                    }
                }
            }

            ToDoMainAction.OpenCreateDueTimePicker -> {
                viewModelScope.launch {
                    setState { copy(showCreateDueTimePicker = true) }
                }
            }

            ToDoMainAction.DismissCreateDueTimePicker -> {
                viewModelScope.launch {
                    setState { copy(showCreateDueTimePicker = false) }
                }
            }

            is ToDoMainAction.SelectCreateDueTime -> {
                viewModelScope.launch {
                    setState {
                        copy(
                            createDueTime = action.time,
                            showCreateDueTimePicker = false,
                            createDueEnabled = true
                        )
                    }
                }
            }

            is ToDoMainAction.ChangeCreateNote -> {
                viewModelScope.launch {
                    setState { copy(createNote = action.value) }
                }
            }

            ToDoMainAction.ConfirmCreate -> {
                viewModelScope.launch {
                    val taskName = state.value.createTaskName.text.trim()
                    if (taskName.isBlank()) return@launch
                    val note = state.value.createNote.text.trim()
                    val dueDate = if (state.value.createDueEnabled) {
                        LocalDateTime.of(state.value.createDueDate, state.value.createDueTime)
                    } else {
                        null
                    }

                    environment.createTaskInQuadrant(
                        taskName = taskName,
                        quadrant = state.value.createQuadrant,
                        dueDate = dueDate,
                        isDueDateTimeSet = state.value.createDueEnabled,
                        note = note
                    )

                    resetCreateDialogState()
                }
            }

            ToDoMainAction.DismissCreateDialog -> {
                resetCreateDialogState()
            }

            ToDoMainAction.ConfirmImportClipboardHint -> {
                viewModelScope.launch {
                    val candidate = state.value.pendingSoftClipboardCandidate ?: return@launch
                    openCreateDialogWithPrefill(
                        quadrant = TaskQuadrant.fromDbDefault(),
                        taskName = candidate.title,
                        note = candidate.note,
                        isQuadrantLocked = false
                    )
                    consumeClipboardCandidate(candidate = candidate, positive = true)
                }
            }

            ToDoMainAction.DismissImportClipboardHint -> {
                viewModelScope.launch {
                    val candidate = state.value.pendingSoftClipboardCandidate ?: return@launch
                    consumeClipboardCandidate(candidate = candidate, positive = false)
                }
            }

            is ToDoMainAction.ToggleTaskStatus -> {
                viewModelScope.launch {
                    environment.toggleTaskStatus(action.task)
                }
            }

            is ToDoMainAction.RequestDeleteTask -> {
                viewModelScope.launch {
                    setState {
                        copy(
                            pendingDeleteTask = action.task,
                            showDeleteTaskConfirmDialog = true
                        )
                    }
                }
            }

            ToDoMainAction.ConfirmDeleteTask -> {
                viewModelScope.launch {
                    state.value.pendingDeleteTask?.let { task ->
                        environment.deleteTask(task)
                    }
                    setState {
                        copy(
                            pendingDeleteTask = null,
                            showDeleteTaskConfirmDialog = false
                        )
                    }
                }
            }

            ToDoMainAction.DismissDeleteTask -> {
                viewModelScope.launch {
                    setState {
                        copy(
                            pendingDeleteTask = null,
                            showDeleteTaskConfirmDialog = false
                        )
                    }
                }
            }
        }
    }

    private fun initClipboardAdaptiveBias() {
        viewModelScope.launch {
            environment.getClipboardAdaptiveBias().collect { bias ->
                SharedTextTaskParser.updateAdaptiveBias(bias)
            }
        }
    }

    private fun initSharedTaskDraft() {
        viewModelScope.launch {
            SharedTaskDraftRepository.pendingDraft.collect { draft ->
                draft ?: return@collect

                openCreateDialogWithPrefill(
                    quadrant = TaskQuadrant.fromDbDefault(),
                    taskName = draft.title,
                    note = draft.note,
                    isQuadrantLocked = false
                )
                SharedTaskDraftRepository.consume(draft.id)
            }
        }
    }

    private fun initClipboardTaskCandidate() {
        viewModelScope.launch {
            SharedTaskDraftRepository.pendingClipboardCandidate.collect { candidate ->
                candidate ?: return@collect

                val lastHandledFingerprint = environment.getLastHandledClipboardFingerprint().first()
                val handledFingerprint = candidate.contentFingerprint.ifBlank { candidate.fingerprint }
                if (
                    handledFingerprint == lastHandledFingerprint ||
                    candidate.fingerprint == lastHandledFingerprint
                ) {
                    SharedTaskDraftRepository.consumeClipboardCandidate(candidate.id)
                    return@collect
                }

                when (candidate.clipboardDecisionLevel) {
                    ClipboardDecisionLevel.ACCEPT,
                    ClipboardDecisionLevel.SOFT -> {
                        setState {
                            copy(
                                showClipboardSoftImportHint = true,
                                pendingSoftClipboardCandidate = candidate
                            )
                        }
                    }

                    ClipboardDecisionLevel.REJECT -> {
                        SharedTaskDraftRepository.consumeClipboardCandidate(candidate.id)
                    }
                }
            }
        }
    }

    private fun initQuickFillHintDuration() {
        viewModelScope.launch {
            environment.getQuickFillHintDurationSeconds().collect { seconds ->
                setState { copy(quickFillHintDurationSeconds = seconds) }
            }
        }
    }

    private fun openCreateDialogWithPrefill(
        quadrant: TaskQuadrant,
        taskName: String,
        note: String,
        isQuadrantLocked: Boolean,
    ) {
        val now = environment.dateTimeProvider.now()
        setState {
            copy(
                isCreateDialogVisible = true,
                createQuadrant = quadrant,
                isCreateQuadrantLocked = isQuadrantLocked,
                createTaskName = TextFieldValue(taskName),
                createDueEnabled = false,
                createDueDate = now.toLocalDate(),
                createDueTime = now.toLocalTime().withSecond(0).withNano(0),
                createNote = TextFieldValue(note),
                showCreateDueDatePicker = false,
                showCreateDueTimePicker = false
            )
        }
    }

    private fun initShowCompleted() {
        viewModelScope.launch {
            environment.getShowCompleted()
                .collect { showCompleted ->
                    setState { copy(showCompleted = showCompleted) }
                }
        }
    }

    private fun initQuadrants() {
        viewModelScope.launch {
            environment.ensureQuadrantSystemLists()
            environment.migrateUncompletedTaskListIdToQuadrantList()
            environment.loadQuadrantTasks().collect { tasks ->
                setState { copy(tasks = tasks) }
            }
        }
    }

    private fun initQuadrantDisplayNames() {
        viewModelScope.launch {
            environment.getQuadrantDisplayNames().collect { displayNames ->
                setState { copy(quadrantDisplayNames = displayNames) }
            }
        }
    }

    private fun resetCreateDialogState() {
        viewModelScope.launch {
            setState {
                copy(
                    isCreateDialogVisible = false,
                    createQuadrant = TaskQuadrant.fromDbDefault(),
                    isCreateQuadrantLocked = false,
                    createTaskName = TextFieldValue(),
                    createDueEnabled = false,
                    createNote = TextFieldValue(),
                    showCreateDueDatePicker = false,
                    showCreateDueTimePicker = false
                )
            }
        }
    }

    private suspend fun consumeClipboardCandidate(candidate: SharedTaskDraft, positive: Boolean) {
        val handledFingerprint = candidate.contentFingerprint.ifBlank { candidate.fingerprint }
        if (handledFingerprint.isNotBlank()) {
            environment.setLastHandledClipboardFingerprint(handledFingerprint)
        }
        environment.recordClipboardPatternFeedback(candidate.patternKey, positive)
        SharedTaskDraftRepository.consumeClipboardCandidate(candidate.id)
        setState {
            copy(
                showClipboardSoftImportHint = false,
                pendingSoftClipboardCandidate = null
            )
        }
    }
}
