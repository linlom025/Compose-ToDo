package com.wisnu.kurniawan.composetodolist.features.todo.main.ui

import androidx.compose.ui.text.input.TextFieldValue
import com.wisnu.kurniawan.composetodolist.BaseViewModelTest
import com.wisnu.kurniawan.composetodolist.DateFactory
import com.wisnu.kurniawan.composetodolist.features.todo.main.data.IToDoMainEnvironment
import com.wisnu.kurniawan.composetodolist.features.todo.main.data.QuadrantTask
import com.wisnu.kurniawan.composetodolist.foundation.wrapper.DateTimeProvider
import com.wisnu.kurniawan.composetodolist.foundation.wrapper.DateTimeProviderImpl
import com.wisnu.kurniawan.composetodolist.foundation.wrapper.IdProvider
import com.wisnu.kurniawan.composetodolist.foundation.wrapper.IdProviderImpl
import com.wisnu.kurniawan.composetodolist.model.TaskQuadrant
import com.wisnu.kurniawan.composetodolist.model.ToDoTask
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import java.time.LocalDateTime
import kotlin.time.ExperimentalTime

@ExperimentalTime
@ExperimentalCoroutinesApi
class ToDoMainViewModelTest : BaseViewModelTest() {

    @Test
    fun init() = runTest {
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

        Assert.assertEquals(1, viewModel.state.value.tasks.size)
        Assert.assertEquals("task-id", viewModel.state.value.tasks.first().task.id)
    }

    @Test
    fun openCreateDialog() = runTest {
        val viewModel = ToDoMainViewModel(FakeToDoMainEnvironment())

        viewModel.dispatch(ToDoMainAction.OpenCreateDialog(TaskQuadrant.Q4))
        advanceUntilIdle()

        Assert.assertTrue(viewModel.state.value.isCreateDialogVisible)
        Assert.assertEquals(TaskQuadrant.Q4, viewModel.state.value.createQuadrant)
    }

    @Test
    fun confirmCreate_whenNameBlank_notCreateTask() = runTest {
        val environment = FakeToDoMainEnvironment()
        val viewModel = ToDoMainViewModel(environment)

        viewModel.dispatch(ToDoMainAction.OpenCreateDialog(TaskQuadrant.Q2))
        viewModel.dispatch(ToDoMainAction.ConfirmCreate)
        advanceUntilIdle()

        Assert.assertEquals(0, environment.createdTasks.size)
        Assert.assertTrue(viewModel.state.value.isCreateDialogVisible)
    }

    @Test
    fun confirmCreate_whenNameValid_createTaskAndResetState() = runTest {
        val environment = FakeToDoMainEnvironment()
        val viewModel = ToDoMainViewModel(environment)

        viewModel.dispatch(ToDoMainAction.OpenCreateDialog(TaskQuadrant.Q3))
        viewModel.dispatch(ToDoMainAction.ChangeCreateTaskName(TextFieldValue("  test task  ")))
        viewModel.dispatch(ToDoMainAction.ConfirmCreate)
        advanceUntilIdle()

        Assert.assertEquals(1, environment.createdTasks.size)
        Assert.assertEquals("test task", environment.createdTasks.first().first)
        Assert.assertEquals(TaskQuadrant.Q3, environment.createdTasks.first().second)
        Assert.assertFalse(viewModel.state.value.isCreateDialogVisible)
        Assert.assertEquals("", viewModel.state.value.createTaskName.text)
    }

    @Test
    fun dismissCreateDialog_clearStateWithoutCreateTask() = runTest {
        val environment = FakeToDoMainEnvironment()
        val viewModel = ToDoMainViewModel(environment)

        viewModel.dispatch(ToDoMainAction.OpenCreateDialog(TaskQuadrant.Q1))
        viewModel.dispatch(ToDoMainAction.ChangeCreateTaskName(TextFieldValue("will cancel")))
        viewModel.dispatch(ToDoMainAction.DismissCreateDialog)
        advanceUntilIdle()

        Assert.assertEquals(0, environment.createdTasks.size)
        Assert.assertFalse(viewModel.state.value.isCreateDialogVisible)
        Assert.assertEquals("", viewModel.state.value.createTaskName.text)
        Assert.assertEquals(TaskQuadrant.fromDbDefault(), viewModel.state.value.createQuadrant)
    }

    private class FakeToDoMainEnvironment(
        tasks: List<QuadrantTask> = listOf()
    ) : IToDoMainEnvironment {
        private val initialTasks = tasks
        override val idProvider: IdProvider = IdProviderImpl()
        override val dateTimeProvider: DateTimeProvider = DateTimeProviderImpl()
        override fun loadQuadrantTasks(): Flow<List<QuadrantTask>> = MutableStateFlow(initialTasks)
        override suspend fun ensureQuadrantSystemLists() = Unit
        override suspend fun migrateUncompletedTaskListIdToQuadrantList() = Unit

        val createdTasks = mutableListOf<Pair<String, TaskQuadrant>>()

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
