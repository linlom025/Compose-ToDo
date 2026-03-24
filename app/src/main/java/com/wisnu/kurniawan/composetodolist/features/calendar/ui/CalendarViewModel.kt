package com.wisnu.kurniawan.composetodolist.features.calendar.ui

import androidx.lifecycle.viewModelScope
import com.wisnu.foundation.coreviewmodel.StatefulViewModel
import com.wisnu.kurniawan.composetodolist.features.calendar.data.CalendarTaskItem
import com.wisnu.kurniawan.composetodolist.features.calendar.data.ICalendarEnvironment
import com.wisnu.kurniawan.composetodolist.foundation.extension.sortedByTaskForDisplay
import com.wisnu.kurniawan.composetodolist.model.ToDoStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    calendarEnvironment: ICalendarEnvironment
) : StatefulViewModel<CalendarState, Unit, CalendarAction, ICalendarEnvironment>(CalendarState(), calendarEnvironment) {

    init {
        initializeSelectedDate()
        initTasks()
    }

    override fun dispatch(action: CalendarAction) {
        when (action) {
            is CalendarAction.SwitchMode -> switchMode(action.mode)
            CalendarAction.PreviousMonth -> updateMonthBy(-1)
            CalendarAction.NextMonth -> updateMonthBy(1)
            is CalendarAction.VisibleMonthChanged -> updateVisibleMonth(action.month)
            is CalendarAction.SelectDate -> selectDate(action.date)
            is CalendarAction.ToggleTaskStatus -> {
                viewModelScope.launch {
                    environment.toggleTaskStatus(action.task)
                }
            }

            is CalendarAction.RequestDeleteTask -> {
                viewModelScope.launch {
                    setState {
                        copy(
                            pendingDeleteTask = action.task,
                            showDeleteTaskConfirmDialog = true
                        )
                    }
                }
            }

            CalendarAction.ConfirmDeleteTask -> {
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

            CalendarAction.DismissDeleteTask -> {
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

    private fun initializeSelectedDate() {
        val today = environment.dateTimeProvider.now().toLocalDate()
        setState {
            copy(
                selectedDate = today,
                visibleMonth = YearMonth.from(today)
            )
        }
    }

    private fun initTasks() {
        viewModelScope.launch {
            environment.loadTasksByCreatedDate().collect { taskItems ->
                val grouped = taskItems
                    .groupBy { it.task.createdAt.toLocalDate() }
                    .mapValues { (_, items) -> items.sortedByTaskForDisplay { it.task } }

                setState {
                    copy(
                        tasksByCreatedDate = grouped,
                        selectedDateTasks = selectedTasksByMode(
                            mode = mode,
                            date = selectedDate,
                            createdDateTasks = grouped,
                            completedDateTasks = tasksByCompletedDate
                        )
                    )
                }
            }
        }
        viewModelScope.launch {
            environment.loadTasksByCompletedDate().collect { taskItems ->
                val grouped = taskItems
                    .filter { it.task.status == ToDoStatus.COMPLETE && it.task.completedAt != null }
                    .groupBy { it.task.completedAt!!.toLocalDate() }
                    .mapValues { (_, items) ->
                        items.sortedByDescending { it.task.completedAt ?: it.task.updatedAt }
                    }

                setState {
                    copy(
                        tasksByCompletedDate = grouped,
                        selectedDateTasks = selectedTasksByMode(
                            mode = mode,
                            date = selectedDate,
                            createdDateTasks = tasksByCreatedDate,
                            completedDateTasks = grouped
                        )
                    )
                }
            }
        }
    }

    private fun updateMonthBy(offset: Long) {
        setState {
            val nextMonth = visibleMonth.plusMonths(offset)
            val nextDay = selectedDate.dayOfMonth.coerceAtMost(nextMonth.lengthOfMonth())
            val nextDate = nextMonth.atDay(nextDay)
            copy(
                visibleMonth = nextMonth,
                selectedDate = nextDate,
                selectedDateTasks = selectedTasksByMode(
                    mode = mode,
                    date = nextDate,
                    createdDateTasks = tasksByCreatedDate,
                    completedDateTasks = tasksByCompletedDate
                )
            )
        }
    }

    private fun updateVisibleMonth(month: YearMonth) {
        if (state.value.visibleMonth == month) return
        setState {
            val nextDay = selectedDate.dayOfMonth.coerceAtMost(month.lengthOfMonth())
            val nextDate = month.atDay(nextDay)
            copy(
                visibleMonth = month,
                selectedDate = nextDate,
                selectedDateTasks = selectedTasksByMode(
                    mode = mode,
                    date = nextDate,
                    createdDateTasks = tasksByCreatedDate,
                    completedDateTasks = tasksByCompletedDate
                )
            )
        }
    }

    private fun selectDate(date: LocalDate) {
        setState {
            copy(
                selectedDate = date,
                visibleMonth = YearMonth.from(date),
                selectedDateTasks = selectedTasksByMode(
                    mode = mode,
                    date = date,
                    createdDateTasks = tasksByCreatedDate,
                    completedDateTasks = tasksByCompletedDate
                )
            )
        }
    }

    private fun switchMode(mode: CalendarMode) {
        if (state.value.mode == mode) return
        setState {
            copy(
                mode = mode,
                selectedDateTasks = selectedTasksByMode(
                    mode = mode,
                    date = selectedDate,
                    createdDateTasks = tasksByCreatedDate,
                    completedDateTasks = tasksByCompletedDate
                )
            )
        }
    }

    private fun selectedTasksByMode(
        mode: CalendarMode,
        date: LocalDate,
        createdDateTasks: Map<LocalDate, List<CalendarTaskItem>>,
        completedDateTasks: Map<LocalDate, List<CalendarTaskItem>>
    ): List<CalendarTaskItem> {
        return when (mode) {
            CalendarMode.CALENDAR -> createdDateTasks[date].orEmpty()
            CalendarMode.ARCHIVE -> completedDateTasks[date].orEmpty()
        }
    }
}
