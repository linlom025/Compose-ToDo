package com.wisnu.kurniawan.composetodolist.features.calendar.ui

import androidx.compose.runtime.Immutable
import com.wisnu.kurniawan.composetodolist.features.calendar.data.CalendarTaskItem
import com.wisnu.kurniawan.composetodolist.model.ToDoTask
import java.time.LocalDate
import java.time.YearMonth

@Immutable
data class CalendarState(
    val visibleMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val tasksByCreatedDate: Map<LocalDate, List<CalendarTaskItem>> = mapOf(),
    val selectedDateTasks: List<CalendarTaskItem> = listOf(),
    val showDeleteTaskConfirmDialog: Boolean = false,
    val pendingDeleteTask: ToDoTask? = null,
)
