package com.wisnu.kurniawan.composetodolist.features.calendar.data

import com.wisnu.kurniawan.composetodolist.model.ToDoTask

data class CalendarTaskItem(
    val task: ToDoTask,
    val listId: String
)
