package com.wisnu.kurniawan.composetodolist.foundation.extension

import com.wisnu.kurniawan.composetodolist.features.todo.detail.ui.ToDoTaskItem
import com.wisnu.kurniawan.composetodolist.features.todo.main.ui.ItemMainState

fun ToDoTaskItem.identifier() = when (this) {
    is ToDoTaskItem.CompleteHeader -> id
    is ToDoTaskItem.Complete -> toDoTask.id
    is ToDoTaskItem.InProgress -> toDoTask.id
}

fun ItemMainState.identifier() = when (this) {
    is ItemMainState.ItemGroup -> group.id
    is ItemMainState.ItemListType -> list.id
}

