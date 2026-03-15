package com.wisnu.kurniawan.composetodolist.features.todo.main.ui

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewModelScope
import com.wisnu.foundation.coreviewmodel.StatefulViewModel
import com.wisnu.kurniawan.composetodolist.features.todo.main.data.IToDoMainEnvironment
import com.wisnu.kurniawan.composetodolist.model.TaskQuadrant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class ToDoMainViewModel @Inject constructor(
    todoMainEnvironment: IToDoMainEnvironment
) : StatefulViewModel<ToDoMainState, Unit, ToDoMainAction, IToDoMainEnvironment>(ToDoMainState(), todoMainEnvironment) {

    init {
        initQuadrants()
    }

    override fun dispatch(action: ToDoMainAction) {
        when (action) {
            ToDoMainAction.ToggleShowCompleted -> {
                viewModelScope.launch {
                    setState { copy(showCompleted = !showCompleted) }
                }
            }

            is ToDoMainAction.OpenCreateDialog -> {
                viewModelScope.launch {
                    val now = environment.dateTimeProvider.now()
                    setState {
                        copy(
                            isCreateDialogVisible = true,
                            createQuadrant = action.quadrant,
                            createTaskName = TextFieldValue(),
                            createDueEnabled = false,
                            createDueDate = now.toLocalDate(),
                            createDueTime = now.toLocalTime().withSecond(0).withNano(0),
                            createNote = TextFieldValue(),
                            showCreateDueDatePicker = false,
                            showCreateDueTimePicker = false
                        )
                    }
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

    private fun initQuadrants() {
        viewModelScope.launch {
            environment.ensureQuadrantSystemLists()
            environment.migrateUncompletedTaskListIdToQuadrantList()
            environment.loadQuadrantTasks().collect { tasks ->
                setState { copy(tasks = tasks) }
            }
        }
    }

    private fun resetCreateDialogState() {
        viewModelScope.launch {
            setState {
                copy(
                    isCreateDialogVisible = false,
                    createQuadrant = TaskQuadrant.fromDbDefault(),
                    createTaskName = TextFieldValue(),
                    createDueEnabled = false,
                    createNote = TextFieldValue(),
                    showCreateDueDatePicker = false,
                    showCreateDueTimePicker = false
                )
            }
        }
    }
}
