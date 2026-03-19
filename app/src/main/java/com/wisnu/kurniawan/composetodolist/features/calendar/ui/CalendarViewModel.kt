package com.wisnu.kurniawan.composetodolist.features.calendar.ui

import androidx.lifecycle.viewModelScope
import com.wisnu.foundation.coreviewmodel.StatefulViewModel
import com.wisnu.kurniawan.composetodolist.features.calendar.data.ICalendarEnvironment
import com.wisnu.kurniawan.composetodolist.foundation.extension.sortedByTaskForDisplay
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
                        selectedDateTasks = grouped[selectedDate].orEmpty()
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
                selectedDateTasks = tasksByCreatedDate[nextDate].orEmpty()
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
                selectedDateTasks = tasksByCreatedDate[nextDate].orEmpty()
            )
        }
    }

    private fun selectDate(date: LocalDate) {
        setState {
            copy(
                selectedDate = date,
                visibleMonth = YearMonth.from(date),
                selectedDateTasks = tasksByCreatedDate[date].orEmpty()
            )
        }
    }
}
