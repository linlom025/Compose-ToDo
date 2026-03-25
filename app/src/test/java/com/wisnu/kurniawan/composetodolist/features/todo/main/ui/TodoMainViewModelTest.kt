package com.wisnu.kurniawan.composetodolist.features.todo.main.ui

import androidx.compose.ui.text.input.TextFieldValue
import com.wisnu.kurniawan.composetodolist.BaseViewModelTest
import com.wisnu.kurniawan.composetodolist.DateFactory
import com.wisnu.kurniawan.composetodolist.features.todo.main.data.IToDoMainEnvironment
import com.wisnu.kurniawan.composetodolist.features.todo.main.data.QuadrantTask
import com.wisnu.kurniawan.composetodolist.foundation.share.SharedTaskDraftRepository
import com.wisnu.kurniawan.composetodolist.foundation.wrapper.DateTimeProvider
import com.wisnu.kurniawan.composetodolist.foundation.wrapper.DateTimeProviderImpl
import com.wisnu.kurniawan.composetodolist.foundation.wrapper.IdProvider
import com.wisnu.kurniawan.composetodolist.foundation.wrapper.IdProviderImpl
import com.wisnu.kurniawan.composetodolist.model.QuadrantDisplayNames
import com.wisnu.kurniawan.composetodolist.model.TaskQuadrant
import com.wisnu.kurniawan.composetodolist.model.ToDoTask
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import kotlin.time.ExperimentalTime

@ExperimentalTime
@ExperimentalCoroutinesApi
class ToDoMainViewModelTest : BaseViewModelTest() {

    @Test
    fun init() = runTest {
        SharedTaskDraftRepository.clear()
        val task = ToDoTask(
            id = "task-id",
            name = "task-name",
            quadrant = TaskQuadrant.Q1,
            createdAt = DateFactory.constantDate,
            updatedAt = DateFactory.constantDate
        )
        val environment = FakeToDoMainEnvironment(
            tasks = listOf(QuadrantTask(task, "system_quadrant_q1"))
        )
        val viewModel = ToDoMainViewModel(environment)

        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.tasks.size)
        assertEquals("task-id", viewModel.state.value.tasks.first().task.id)
    }

    @Test
    fun openCreateDialog() = runTest {
        SharedTaskDraftRepository.clear()
        val viewModel = ToDoMainViewModel(FakeToDoMainEnvironment())

        viewModel.dispatch(ToDoMainAction.OpenCreateDialog(TaskQuadrant.Q4))
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isCreateDialogVisible)
        assertEquals(TaskQuadrant.Q4, viewModel.state.value.createQuadrant)
        assertTrue(viewModel.state.value.isCreateQuadrantLocked)
    }

    @Test
    fun confirmCreate_whenNameBlank_notCreateTask() = runTest {
        SharedTaskDraftRepository.clear()
        val environment = FakeToDoMainEnvironment()
        val viewModel = ToDoMainViewModel(environment)

        viewModel.dispatch(ToDoMainAction.OpenCreateDialog(TaskQuadrant.Q2))
        viewModel.dispatch(ToDoMainAction.ConfirmCreate)
        advanceUntilIdle()

        assertEquals(0, environment.createdTasks.size)
        assertTrue(viewModel.state.value.isCreateDialogVisible)
    }

    @Test
    fun confirmCreate_whenNameValid_createTaskAndResetState() = runTest {
        SharedTaskDraftRepository.clear()
        val environment = FakeToDoMainEnvironment()
        val viewModel = ToDoMainViewModel(environment)

        viewModel.dispatch(ToDoMainAction.OpenCreateDialog(TaskQuadrant.Q3))
        viewModel.dispatch(ToDoMainAction.ChangeCreateTaskName(TextFieldValue("  test task  ")))
        viewModel.dispatch(ToDoMainAction.ConfirmCreate)
        advanceUntilIdle()

        assertEquals(1, environment.createdTasks.size)
        assertEquals("test task", environment.createdTasks.first().first)
        assertEquals(TaskQuadrant.Q3, environment.createdTasks.first().second)
        assertFalse(viewModel.state.value.isCreateDialogVisible)
        assertEquals("", viewModel.state.value.createTaskName.text)
    }

    @Test
    fun dismissCreateDialog_clearStateWithoutCreateTask() = runTest {
        SharedTaskDraftRepository.clear()
        val environment = FakeToDoMainEnvironment()
        val viewModel = ToDoMainViewModel(environment)

        viewModel.dispatch(ToDoMainAction.OpenCreateDialog(TaskQuadrant.Q1))
        viewModel.dispatch(ToDoMainAction.ChangeCreateTaskName(TextFieldValue("will cancel")))
        viewModel.dispatch(ToDoMainAction.DismissCreateDialog)
        advanceUntilIdle()

        assertEquals(0, environment.createdTasks.size)
        assertFalse(viewModel.state.value.isCreateDialogVisible)
        assertEquals("", viewModel.state.value.createTaskName.text)
        assertEquals(TaskQuadrant.fromDbDefault(), viewModel.state.value.createQuadrant)
        assertFalse(viewModel.state.value.isCreateQuadrantLocked)
    }

    @Test
    fun init_readShowCompletedFromEnvironment() = runTest {
        SharedTaskDraftRepository.clear()
        val environment = FakeToDoMainEnvironment(showCompleted = true)
        val viewModel = ToDoMainViewModel(environment)

        advanceUntilIdle()

        assertTrue(viewModel.state.value.showCompleted)
    }

    @Test
    fun toggleShowCompleted_updateStateAndPersist() = runTest {
        SharedTaskDraftRepository.clear()
        val environment = FakeToDoMainEnvironment(showCompleted = false)
        val viewModel = ToDoMainViewModel(environment)
        advanceUntilIdle()

        viewModel.dispatch(ToDoMainAction.ToggleShowCompleted)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.showCompleted)
        assertEquals(listOf(true), environment.savedShowCompletedValues)
    }

    @Test
    fun changeCreateQuadrant_whenLocked_ignoreAction() = runTest {
        SharedTaskDraftRepository.clear()
        val viewModel = ToDoMainViewModel(FakeToDoMainEnvironment())
        viewModel.dispatch(ToDoMainAction.OpenCreateDialog(TaskQuadrant.Q1))
        advanceUntilIdle()

        viewModel.dispatch(ToDoMainAction.ChangeCreateQuadrant(TaskQuadrant.Q4))
        advanceUntilIdle()

        assertEquals(TaskQuadrant.Q1, viewModel.state.value.createQuadrant)
    }

    @Test
    fun consumeClipboardCandidate_accept_showHintOnly() = runTest {
        SharedTaskDraftRepository.clear()
        val viewModel = ToDoMainViewModel(FakeToDoMainEnvironment())
        advanceUntilIdle()

        SharedTaskDraftRepository.publishFromClipboardText("今天下班前提交周报")
        advanceUntilIdle()

        assertTrue(viewModel.state.value.showClipboardSoftImportHint)
        assertEquals("今天下班前提交周报", viewModel.state.value.pendingSoftClipboardCandidate?.title)
    }

    @Test
    fun consumeClipboardCandidate_soft_showHintOnly() = runTest {
        SharedTaskDraftRepository.clear()
        val viewModel = ToDoMainViewModel(FakeToDoMainEnvironment())
        advanceUntilIdle()

        SharedTaskDraftRepository.publishFromClipboardText("今天下班前整理一下资料")
        advanceUntilIdle()

        assertTrue(viewModel.state.value.showClipboardSoftImportHint)
        assertEquals("今天下班前整理一下资料", viewModel.state.value.pendingSoftClipboardCandidate?.title)
    }

    @Test
    fun confirmClipboardCandidate_openCreateAndPersistFingerprint() = runTest {
        SharedTaskDraftRepository.clear()
        val environment = FakeToDoMainEnvironment()
        val viewModel = ToDoMainViewModel(environment)
        advanceUntilIdle()

        val candidate = SharedTaskDraftRepository.publishFromClipboardText("finish report\nbefore 6pm")
        advanceUntilIdle()
        viewModel.dispatch(ToDoMainAction.ConfirmImportClipboardHint)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isCreateDialogVisible)
        assertFalse(viewModel.state.value.showClipboardSoftImportHint)
        assertEquals("finish report\nbefore 6pm", viewModel.state.value.createTaskName.text)
        assertEquals("", viewModel.state.value.createNote.text)
        assertFalse(viewModel.state.value.isCreateQuadrantLocked)
        viewModel.dispatch(ToDoMainAction.ChangeCreateQuadrant(TaskQuadrant.Q4))
        advanceUntilIdle()
        assertEquals(TaskQuadrant.Q4, viewModel.state.value.createQuadrant)
        assertEquals(listOf(candidate?.contentFingerprint), environment.savedClipboardFingerprints)
        assertEquals(listOf(candidate?.patternKey to true), environment.savedPatternFeedbacks)
        assertNull(SharedTaskDraftRepository.pendingClipboardCandidate.value)
    }

    @Test
    fun dismissClipboardSoftCandidate_recordNegativeFeedback() = runTest {
        SharedTaskDraftRepository.clear()
        val environment = FakeToDoMainEnvironment()
        val viewModel = ToDoMainViewModel(environment)
        advanceUntilIdle()

        val candidate = SharedTaskDraftRepository.publishFromClipboardText("整理一下资料")
        advanceUntilIdle()
        viewModel.dispatch(ToDoMainAction.DismissImportClipboardHint)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.showClipboardSoftImportHint)
        assertEquals(listOf(candidate?.contentFingerprint), environment.savedClipboardFingerprints)
        assertEquals(listOf(candidate?.patternKey to false), environment.savedPatternFeedbacks)
        assertNull(SharedTaskDraftRepository.pendingClipboardCandidate.value)
    }

    @Test
    fun consumeClipboardCandidate_whenFingerprintAlreadyHandled_ignorePrompt() = runTest {
        SharedTaskDraftRepository.clear()
        val existing = SharedTaskDraftRepository.publishFromClipboardText("same title\nsame note")
        val environment = FakeToDoMainEnvironment(
            lastHandledClipboardFingerprint = existing?.contentFingerprint.orEmpty()
        )
        val viewModel = ToDoMainViewModel(environment)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.showClipboardSoftImportHint)
        assertNull(viewModel.state.value.pendingSoftClipboardCandidate)
        assertNull(SharedTaskDraftRepository.pendingClipboardCandidate.value)
    }

    private class FakeToDoMainEnvironment(
        tasks: List<QuadrantTask> = listOf(),
        showCompleted: Boolean = false,
        lastHandledClipboardFingerprint: String = "",
    ) : IToDoMainEnvironment {
        private val initialTasks = tasks
        private val showCompletedFlow = MutableStateFlow(showCompleted)
        private val lastHandledClipboardFingerprintFlow = MutableStateFlow(lastHandledClipboardFingerprint)
        private val adaptiveBiasFlow = MutableStateFlow<Map<String, Int>>(emptyMap())

        override val idProvider: IdProvider = IdProviderImpl()
        override val dateTimeProvider: DateTimeProvider = DateTimeProviderImpl()

        val createdTasks = mutableListOf<Pair<String, TaskQuadrant>>()
        val savedShowCompletedValues = mutableListOf<Boolean>()
        val savedClipboardFingerprints = mutableListOf<String?>()
        val savedPatternFeedbacks = mutableListOf<Pair<String?, Boolean>>()

        override fun getShowCompleted(): Flow<Boolean> = showCompletedFlow

        override fun getLastHandledClipboardFingerprint(): Flow<String> = lastHandledClipboardFingerprintFlow

        override fun getClipboardAdaptiveBias(): Flow<Map<String, Int>> = adaptiveBiasFlow

        override fun getQuickFillHintDurationSeconds(): Flow<Int> = MutableStateFlow(5)

        override suspend fun setShowCompleted(showCompleted: Boolean) {
            savedShowCompletedValues.add(showCompleted)
            showCompletedFlow.emit(showCompleted)
        }

        override suspend fun setLastHandledClipboardFingerprint(fingerprint: String) {
            savedClipboardFingerprints.add(fingerprint)
            lastHandledClipboardFingerprintFlow.emit(fingerprint)
        }

        override suspend fun recordClipboardPatternFeedback(patternKey: String, positive: Boolean) {
            savedPatternFeedbacks.add(patternKey to positive)
        }

        override fun loadQuadrantTasks(): Flow<List<QuadrantTask>> = MutableStateFlow(initialTasks)

        override fun getQuadrantDisplayNames(): Flow<QuadrantDisplayNames> =
            MutableStateFlow(QuadrantDisplayNames.default())

        override suspend fun ensureQuadrantSystemLists() = Unit

        override suspend fun migrateUncompletedTaskListIdToQuadrantList() = Unit

        override suspend fun createTaskInQuadrant(
            taskName: String,
            quadrant: TaskQuadrant,
            dueDate: LocalDateTime?,
            isDueDateTimeSet: Boolean,
            note: String
        ) {
            createdTasks.add(taskName to quadrant)
        }

        override suspend fun toggleTaskStatus(task: ToDoTask) = Unit

        override suspend fun deleteTask(task: ToDoTask) = Unit
    }
}
