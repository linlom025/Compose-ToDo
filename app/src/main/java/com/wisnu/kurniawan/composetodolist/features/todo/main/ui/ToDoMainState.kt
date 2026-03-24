package com.wisnu.kurniawan.composetodolist.features.todo.main.ui

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.input.TextFieldValue
import com.wisnu.kurniawan.composetodolist.features.todo.main.data.QuadrantTask
import com.wisnu.kurniawan.composetodolist.foundation.datasource.local.model.ToDoGroupDb
import com.wisnu.kurniawan.composetodolist.foundation.extension.sortedByTaskForDisplay
import com.wisnu.kurniawan.composetodolist.foundation.share.SharedTaskDraft
import com.wisnu.kurniawan.composetodolist.model.QuadrantDisplayNames
import com.wisnu.kurniawan.composetodolist.model.TaskQuadrant
import com.wisnu.kurniawan.composetodolist.model.ToDoGroup
import com.wisnu.kurniawan.composetodolist.model.ToDoList
import com.wisnu.kurniawan.composetodolist.model.ToDoStatus
import com.wisnu.kurniawan.composetodolist.model.ToDoTask
import java.time.LocalDate
import java.time.LocalTime

@Immutable
data class ToDoMainState(
    val tasks: List<QuadrantTask> = listOf(),
    val quadrantDisplayNames: QuadrantDisplayNames = QuadrantDisplayNames.default(),
    val showCompleted: Boolean = false,
    val isCreateDialogVisible: Boolean = false,
    val createQuadrant: TaskQuadrant = TaskQuadrant.fromDbDefault(),
    val isCreateQuadrantLocked: Boolean = false,
    val createTaskName: TextFieldValue = TextFieldValue(),
    val createDueEnabled: Boolean = false,
    val createDueDate: LocalDate = LocalDate.now(),
    val createDueTime: LocalTime = LocalTime.of(9, 0),
    val createNote: TextFieldValue = TextFieldValue(),
    val showCreateDueDatePicker: Boolean = false,
    val showCreateDueTimePicker: Boolean = false,
    val showClipboardImportDialog: Boolean = false,
    val pendingClipboardCandidate: SharedTaskDraft? = null,
    val showClipboardSoftImportHint: Boolean = false,
    val pendingSoftClipboardCandidate: SharedTaskDraft? = null,
    val showDeleteTaskConfirmDialog: Boolean = false,
    val pendingDeleteTask: ToDoTask? = null,
) {
    val validCreateTaskName: Boolean = createTaskName.text.isNotBlank()

    val quadrants: Map<TaskQuadrant, List<QuadrantTask>> = TaskQuadrant.entries.associateWith { quadrant ->
        val quadrantTasks = tasks.filter { it.task.quadrant == quadrant }
        if (showCompleted) {
            quadrantTasks.sortedByTaskForDisplay { it.task }
        } else {
            quadrantTasks.filter { it.task.status != ToDoStatus.COMPLETE }
        }
    }
}

// Legacy model kept for compatibility with existing tests and helpers.
sealed class SelectedItemState {
    object Empty : SelectedItemState()
    object AllTask : SelectedItemState()
    object ScheduledTodayTask : SelectedItemState()
    object ScheduledTask : SelectedItemState()
    data class List(val listId: String) : SelectedItemState()
}

// Legacy model kept for compatibility with existing tests and helpers.
sealed class ItemMainState {
    data class ItemGroup(
        val group: ToDoGroup
    ) : ItemMainState()

    sealed class ItemListType(
        open val list: ToDoList,
        open val selected: Boolean,
    ) : ItemMainState() {
        data class First(
            override val list: ToDoList,
            override val selected: Boolean,
        ) : ItemListType(list, selected)

        data class Middle(
            override val list: ToDoList,
            override val selected: Boolean,
        ) : ItemListType(list, selected)

        data class Last(
            override val list: ToDoList,
            override val selected: Boolean,
        ) : ItemListType(list, selected)

        data class Single(
            override val list: ToDoList,
            override val selected: Boolean,
        ) : ItemListType(list, selected)
    }
}

// Legacy mapper kept for compatibility with existing tests and helpers.
fun List<ToDoGroup>.toItemGroup(selectedItemState: SelectedItemState): List<ItemMainState> {
    val data = mutableListOf<ItemMainState>()

    forEach {
        if (it.id != ToDoGroupDb.DEFAULT_ID) {
            data.add(ItemMainState.ItemGroup(it))
        }
        data.addAll(it.lists.toItemListMainState(selectedItemState))
    }

    return data
}

private fun List<ToDoList>.toItemListMainState(selectedItemState: SelectedItemState): List<ItemMainState.ItemListType> {
    return mapIndexed { index, list ->
        val selected = selectedItemState is SelectedItemState.List && selectedItemState.listId == list.id
        if (size == 1) {
            ItemMainState.ItemListType.Single(
                list = list,
                selected = selected
            )
        } else {
            when (index) {
                0 -> ItemMainState.ItemListType.First(
                    list = list,
                    selected = selected
                )
                lastIndex -> ItemMainState.ItemListType.Last(
                    list = list,
                    selected = selected
                )
                else -> ItemMainState.ItemListType.Middle(
                    list = list,
                    selected = selected
                )
            }
        }
    }
}
