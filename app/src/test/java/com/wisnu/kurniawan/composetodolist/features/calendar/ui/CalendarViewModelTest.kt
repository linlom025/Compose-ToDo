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
        override val dateTimeProvider: DateTimeProvider = object : DateTimeProvider {
            override fun now(): LocalDateTime = now
        }

        private val flow = MutableStateFlow(tasks)

        override fun loadTasksByCreatedDate(): Flow<List<CalendarTaskItem>> = flow

        override suspend fun toggleTaskStatus(task: ToDoTask) = Unit

        override suspend fun deleteTask(task: ToDoTask) = Unit
    }
}
