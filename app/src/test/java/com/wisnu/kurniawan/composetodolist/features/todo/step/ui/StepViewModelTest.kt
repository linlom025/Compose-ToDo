package com.wisnu.kurniawan.composetodolist.features.todo.step.ui

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.wisnu.kurniawan.composetodolist.BaseViewModelTest
import com.wisnu.kurniawan.composetodolist.DateFactory
import com.wisnu.kurniawan.composetodolist.features.todo.step.data.IStepEnvironment
import com.wisnu.kurniawan.composetodolist.foundation.wrapper.DateTimeProvider
import com.wisnu.kurniawan.composetodolist.foundation.wrapper.IdProvider
import com.wisnu.kurniawan.composetodolist.foundation.wrapper.IdProviderImpl
import com.wisnu.kurniawan.composetodolist.model.QuadrantDisplayNames
import com.wisnu.kurniawan.composetodolist.model.TaskQuadrant
import com.wisnu.kurniawan.composetodolist.model.ToDoColor
import com.wisnu.kurniawan.composetodolist.model.ToDoRepeat
import com.wisnu.kurniawan.composetodolist.model.ToDoStatus
import com.wisnu.kurniawan.composetodolist.model.ToDoStep
import com.wisnu.kurniawan.composetodolist.model.ToDoTask
import com.wisnu.kurniawan.composetodolist.runtime.navigation.ARG_LIST_ID
import com.wisnu.kurniawan.composetodolist.runtime.navigation.ARG_TASK_ID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import kotlin.time.ExperimentalTime

@ExperimentalTime
@ExperimentalCoroutinesApi
class StepViewModelTest : BaseViewModelTest() {

    @Test
    fun clickDone_whenChanged_updatesAndStopsEditing() = runTest {
        val environment = FakeStepEnvironment(task(note = "old"))
        val viewModel = buildViewModel(environment)
        advanceUntilIdle()

        viewModel.dispatch(StepAction.NoteAction.StartEdit)
        viewModel.dispatch(StepAction.NoteAction.ChangeNote(TextFieldValue("new note")))
        viewModel.dispatch(StepAction.NoteAction.ClickDone)
        advanceUntilIdle()

        assertEquals(listOf("new note"), environment.updatedNotes)
        assertFalse(viewModel.state.value.isEditingNote)
        assertEquals("new note", viewModel.state.value.noteOriginal)
    }

    @Test
    fun clickDone_whenNoChange_doesNotUpdate() = runTest {
        val environment = FakeStepEnvironment(task(note = "same"))
        val viewModel = buildViewModel(environment)
        advanceUntilIdle()

        viewModel.dispatch(StepAction.NoteAction.StartEdit)
        viewModel.dispatch(StepAction.NoteAction.ClickDone)
        advanceUntilIdle()

        assertTrue(environment.updatedNotes.isEmpty())
        assertFalse(viewModel.state.value.isEditingNote)
    }

    @Test
    fun requestExitWithUnsaved_whenDirty_showsConfirmDialog() = runTest {
        val environment = FakeStepEnvironment(task(note = "before"))
        val viewModel = buildViewModel(environment)
        advanceUntilIdle()

        viewModel.dispatch(StepAction.NoteAction.StartEdit)
        viewModel.dispatch(StepAction.NoteAction.ChangeNote(TextFieldValue("after")))
        viewModel.dispatch(StepAction.NoteAction.RequestExitWithUnsaved(NoteExitTarget.EDIT))
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isEditingNote)
        assertTrue(viewModel.state.value.showUnsavedNoteDialog)
        assertEquals(NoteExitTarget.EDIT, viewModel.state.value.pendingNoteExitTarget)
    }

    @Test
    fun confirmSaveAndExit_fromScreen_savesAndNavigateBack() = runTest {
        val environment = FakeStepEnvironment(task(note = "before"))
        val viewModel = buildViewModel(environment)
        advanceUntilIdle()

        viewModel.effect.test {
            viewModel.dispatch(StepAction.NoteAction.StartEdit)
            viewModel.dispatch(StepAction.NoteAction.ChangeNote(TextFieldValue("after")))
            viewModel.dispatch(StepAction.NoteAction.RequestExitWithUnsaved(NoteExitTarget.SCREEN))
            viewModel.dispatch(StepAction.NoteAction.ConfirmSaveAndExit)
            advanceUntilIdle()

            awaitItem() // initial null
            assertEquals(StepEffect.NavigateBack, awaitItem())
            cancelAndConsumeRemainingEvents()
        }

        assertEquals(listOf("after"), environment.updatedNotes)
        assertFalse(viewModel.state.value.isEditingNote)
    }

    @Test
    fun discardAndExit_fromScreen_discardsAndNavigateBack() = runTest {
        val environment = FakeStepEnvironment(task(note = "before"))
        val viewModel = buildViewModel(environment)
        advanceUntilIdle()

        viewModel.effect.test {
            viewModel.dispatch(StepAction.NoteAction.StartEdit)
            viewModel.dispatch(StepAction.NoteAction.ChangeNote(TextFieldValue("after")))
            viewModel.dispatch(StepAction.NoteAction.RequestExitWithUnsaved(NoteExitTarget.SCREEN))
            viewModel.dispatch(StepAction.NoteAction.DiscardAndExit)
            advanceUntilIdle()

            awaitItem() // initial null
            assertEquals(StepEffect.NavigateBack, awaitItem())
            cancelAndConsumeRemainingEvents()
        }

        assertTrue(environment.updatedNotes.isEmpty())
        assertFalse(viewModel.state.value.isEditingNote)
        assertEquals("before", viewModel.state.value.editNote.text)
    }

    @Test
    fun clickDone_whenCleared_allowsSavingEmptyNote() = runTest {
        val environment = FakeStepEnvironment(task(note = "before"))
        val viewModel = buildViewModel(environment)
        advanceUntilIdle()

        viewModel.dispatch(StepAction.NoteAction.StartEdit)
        viewModel.dispatch(StepAction.NoteAction.ChangeNote(TextFieldValue("")))
        viewModel.dispatch(StepAction.NoteAction.ClickDone)
        advanceUntilIdle()

        assertEquals(listOf(""), environment.updatedNotes)
        assertFalse(viewModel.state.value.isEditingNote)
    }

    @Test
    fun selectQuadrant_whenDifferent_updatesTaskQuadrant() = runTest {
        val environment = FakeStepEnvironment(
            task(note = "", quadrant = TaskQuadrant.Q1)
        )
        val viewModel = buildViewModel(environment)
        advanceUntilIdle()

        viewModel.dispatch(StepAction.TaskAction.SelectQuadrant(TaskQuadrant.Q4))
        advanceUntilIdle()

        assertEquals(listOf(TaskQuadrant.Q4), environment.movedQuadrants)
    }

    @Test
    fun selectQuadrant_whenSame_noopAndCloseDialog() = runTest {
        val environment = FakeStepEnvironment(
            task(note = "", quadrant = TaskQuadrant.Q2)
        )
        val viewModel = buildViewModel(environment)
        advanceUntilIdle()

        viewModel.dispatch(StepAction.TaskAction.OpenQuadrantDialog)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.showQuadrantDialog)

        viewModel.dispatch(StepAction.TaskAction.SelectQuadrant(TaskQuadrant.Q2))
        advanceUntilIdle()

        assertTrue(environment.movedQuadrants.isEmpty())
        assertFalse(viewModel.state.value.showQuadrantDialog)
    }

    private fun buildViewModel(environment: FakeStepEnvironment): StepViewModel {
        val savedStateHandle = SavedStateHandle().apply {
            set(ARG_TASK_ID, "task-id")
            set(ARG_LIST_ID, "list-id")
        }
        return StepViewModel(savedStateHandle, environment)
    }

    private fun task(
        note: String,
        quadrant: TaskQuadrant = TaskQuadrant.fromDbDefault(),
        createdAt: LocalDateTime = DateFactory.constantDate
    ): ToDoTask {
        return ToDoTask(
            id = "task-id",
            name = "task-name",
            status = ToDoStatus.IN_PROGRESS,
            quadrant = quadrant,
            note = note,
            noteUpdatedAt = if (note.isBlank()) null else createdAt,
            createdAt = createdAt,
            updatedAt = createdAt
        )
    }

    private class FakeStepEnvironment(
        initialTask: ToDoTask
    ) : IStepEnvironment {
        override val idProvider: IdProvider = IdProviderImpl()
        override val dateTimeProvider: DateTimeProvider = object : DateTimeProvider {
            override fun now(): LocalDateTime = DateFactory.constantDate
        }

        private val taskFlow = MutableStateFlow(initialTask)
        val updatedNotes = mutableListOf<String>()
        val movedQuadrants = mutableListOf<TaskQuadrant>()

        override fun getTask(taskId: String): Flow<Pair<ToDoTask, ToDoColor>> {
            return taskFlow.map { task -> task to ToDoColor.BLUE }
        }

        override fun getQuadrantDisplayNames(): Flow<QuadrantDisplayNames> {
            return MutableStateFlow(QuadrantDisplayNames.default())
        }

        override suspend fun deleteTask(task: ToDoTask) = Unit
        override suspend fun toggleTaskStatus(task: ToDoTask) = Unit
        override suspend fun setRepeatTask(task: ToDoTask, toDoRepeat: ToDoRepeat) = Unit
        override suspend fun moveTaskToQuadrant(taskId: String, targetQuadrant: TaskQuadrant) {
            movedQuadrants.add(targetQuadrant)
            taskFlow.value = taskFlow.value.copy(quadrant = targetQuadrant)
        }
        override suspend fun toggleStepStatus(step: ToDoStep) = Unit
        override suspend fun createStep(name: String, taskId: String) = Unit
        override suspend fun deleteStep(step: ToDoStep) = Unit
        override suspend fun updateTask(name: String, taskId: String) = Unit
        override suspend fun updateStep(name: String, stepId: String) = Unit
        override suspend fun updateTaskDueDate(
            date: LocalDateTime,
            isDueDateTimeSet: Boolean,
            taskId: String
        ) = Unit

        override suspend fun updateTaskNote(note: String, taskId: String) {
            updatedNotes.add(note)
            taskFlow.value = taskFlow.value.copy(
                note = note,
                noteUpdatedAt = DateFactory.constantDate,
                updatedAt = DateFactory.constantDate
            )
        }

        override suspend fun resetTaskDueDate(taskId: String) = Unit
        override suspend fun resetTaskTime(date: LocalDateTime, taskId: String) = Unit
    }
}
