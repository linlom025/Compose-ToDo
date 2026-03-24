package com.wisnu.kurniawan.composetodolist.features.calendar.ui

import com.wisnu.kurniawan.composetodolist.BaseViewModelTest
import com.wisnu.kurniawan.composetodolist.features.calendar.data.CalendarTaskItem
import com.wisnu.kurniawan.composetodolist.features.calendar.data.ICalendarEnvironment
import com.wisnu.kurniawan.composetodolist.foundation.wrapper.DateTimeProvider
import com.wisnu.kurniawan.composetodolist.model.ToDoStatus
import com.wisnu.kurniawan.composetodolist.model.ToDoTask
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime
import kotlin.time.ExperimentalTime

@ExperimentalTime
@ExperimentalCoroutinesApi
class CalendarViewModelTest : BaseViewModelTest() {

    @Test
    fun initTasks_sortInProgressFirstAndCompletedByCompletedAtDesc() = runTest {
        val now = LocalDateTime.of(2026, 3, 19, 9, 0)
        val inProgressA = task("ip-a", ToDoStatus.IN_PROGRESS, now.plusMinutes(1))
        val completeOld = task(
            "c-old",
            ToDoStatus.COMPLETE,
            now.plusMinutes(2),
            completedAt = now.plusMinutes(30)
        )
        val inProgressB = task("ip-b", ToDoStatus.IN_PROGRESS, now.plusMinutes(3))
        val completeNew = task(
            "c-new",
            ToDoStatus.COMPLETE,
            now.plusMinutes(4),
            completedAt = now.plusMinutes(50)
        )

        val environment = FakeCalendarEnvironment(
            now = now,
            tasks = listOf(
                CalendarTaskItem(inProgressB, "list"),
                CalendarTaskItem(completeOld, "list"),
                CalendarTaskItem(inProgressA, "list"),
                CalendarTaskItem(completeNew, "list")
            )
        )

        val viewModel = CalendarViewModel(environment)
        advanceUntilIdle()

        val ids = viewModel.state.value.selectedDateTasks.map { it.task.id }
        assertEquals(listOf("ip-b", "ip-a", "c-new", "c-old"), ids)
    }

    @Test
    fun switchMode_archiveUsesCompletedDateGrouping() = runTest {
        val now = LocalDateTime.of(2026, 3, 19, 9, 0)
        val createdOnly = task("created-only", ToDoStatus.IN_PROGRESS, now.plusMinutes(1))
        val completeToday = task(
            "complete-today",
            ToDoStatus.COMPLETE,
            now.minusDays(1),
            completedAt = now.plusMinutes(20)
        )
        val completeTomorrow = task(
            "complete-tomorrow",
            ToDoStatus.COMPLETE,
            now,
            completedAt = now.plusDays(1).plusMinutes(10)
        )

        val environment = FakeCalendarEnvironment(
            now = now,
            tasks = listOf(
                CalendarTaskItem(createdOnly, "list"),
                CalendarTaskItem(completeToday, "list"),
                CalendarTaskItem(completeTomorrow, "list")
            )
        )

        val viewModel = CalendarViewModel(environment)
        advanceUntilIdle()

        assertEquals(CalendarMode.CALENDAR, viewModel.state.value.mode)
        assertEquals(listOf("created-only", "complete-tomorrow"), viewModel.state.value.selectedDateTasks.map { it.task.id })

        viewModel.dispatch(CalendarAction.SwitchMode(CalendarMode.ARCHIVE))
        advanceUntilIdle()

        assertEquals(listOf("complete-today"), viewModel.state.value.selectedDateTasks.map { it.task.id })
    }

    @Test
    fun toggleTaskStatusInArchive_removeRecoveredTaskImmediately() = runTest {
        val now = LocalDateTime.of(2026, 3, 19, 9, 0)
        val completed = task(
            id = "done-1",
            status = ToDoStatus.COMPLETE,
            createdAt = now.minusDays(3),
            completedAt = now.minusMinutes(5)
        )

        val environment = FakeCalendarEnvironment(
            now = now,
            tasks = listOf(CalendarTaskItem(completed, "list"))
        )

        val viewModel = CalendarViewModel(environment)
        viewModel.dispatch(CalendarAction.SwitchMode(CalendarMode.ARCHIVE))
        advanceUntilIdle()

        assertEquals(listOf("done-1"), viewModel.state.value.selectedDateTasks.map { it.task.id })

        viewModel.dispatch(CalendarAction.ToggleTaskStatus(completed))
        advanceUntilIdle()

        assertEquals(emptyList<String>(), viewModel.state.value.selectedDateTasks.map { it.task.id })
    }

    private fun task(
        id: String,
        status: ToDoStatus,
        createdAt: LocalDateTime,
        completedAt: LocalDateTime? = null
    ): ToDoTask {
        return ToDoTask(
            id = id,
            name = id,
            status = status,
            completedAt = completedAt,
            createdAt = createdAt,
            updatedAt = createdAt
        )
    }

    private class FakeCalendarEnvironment(
        now: LocalDateTime,
        tasks: List<CalendarTaskItem>
    ) : ICalendarEnvironment {
        private val currentNow = now

        override val dateTimeProvider: DateTimeProvider = object : DateTimeProvider {
            override fun now(): LocalDateTime = currentNow
        }

        private val flow = MutableStateFlow(tasks)

        override fun loadTasksByCreatedDate(): Flow<List<CalendarTaskItem>> = flow

        override fun loadTasksByCompletedDate(): Flow<List<CalendarTaskItem>> {
            return flow.map { taskItems ->
                taskItems.filter { it.task.status == ToDoStatus.COMPLETE && it.task.completedAt != null }
            }
        }

        override suspend fun toggleTaskStatus(task: ToDoTask) {
            flow.value = flow.value.map { item ->
                if (item.task.id != task.id) {
                    item
                } else {
                    val nextStatus = if (item.task.status == ToDoStatus.COMPLETE) {
                        ToDoStatus.IN_PROGRESS
                    } else {
                        ToDoStatus.COMPLETE
                    }
                    item.copy(
                        task = item.task.copy(
                            status = nextStatus,
                            completedAt = if (nextStatus == ToDoStatus.COMPLETE) currentNow else null,
                            updatedAt = currentNow
                        )
                    )
                }
            }
        }

        override suspend fun deleteTask(task: ToDoTask) = Unit
    }
}
