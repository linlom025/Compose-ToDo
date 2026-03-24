package com.wisnu.kurniawan.composetodolist.features.calendar.ui

import com.wisnu.kurniawan.composetodolist.model.ToDoTask
import java.time.LocalDate
import java.time.YearMonth

sealed class CalendarAction {
    data class SwitchMode(val mode: CalendarMode) : CalendarAction()
    object PreviousMonth : CalendarAction()
    object NextMonth : CalendarAction()
    data class VisibleMonthChanged(val month: YearMonth) : CalendarAction()
    data class SelectDate(val date: LocalDate) : CalendarAction()
    data class ToggleTaskStatus(val task: ToDoTask) : CalendarAction()
    data class RequestDeleteTask(val task: ToDoTask) : CalendarAction()
    object ConfirmDeleteTask : CalendarAction()
    object DismissDeleteTask : CalendarAction()
}
