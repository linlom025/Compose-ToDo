package com.wisnu.kurniawan.composetodolist.features.todo.detail.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wisnu.kurniawan.composetodolist.R
import com.wisnu.kurniawan.composetodolist.foundation.extension.identifier
import com.wisnu.kurniawan.composetodolist.foundation.theme.AlphaDisabled
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgEmpty
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgIcon
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.MotionTokens
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgToDoCreator
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgToDoItemCell
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.itemInfoDisplayable
import com.wisnu.kurniawan.composetodolist.foundation.uiextension.requestFocusImeAware
import com.wisnu.kurniawan.composetodolist.model.TaskQuadrant
import com.wisnu.kurniawan.composetodolist.model.ToDoTask
import kotlinx.coroutines.launch

@Composable
fun TaskCreator(
    modifier: Modifier = Modifier,
    text: String,
    color: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
                .height(52.dp)
                .clip(MaterialTheme.shapes.medium)
                .clickable(onClick = onClick),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.secondary,
            tonalElevation = 1.dp
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(Modifier.width(10.dp))

                PgIcon(
                    imageVector = Icons.Rounded.Add,
                    tint = color
                )

                Spacer(Modifier.width(10.dp))

                Text(
                    text = text.ifBlank {
                        stringResource(R.string.todo_add_task)
                    },
                    style = MaterialTheme.typography.titleSmall,
                    color = color
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskContent(
    modifier: Modifier,
    tasks: List<ToDoTaskItem>,
    onClick: (ToDoTask) -> Unit,
    onStatusClick: (ToDoTask) -> Unit,
    onSwipeToDelete: (ToDoTask) -> Unit,
    color: Color,
    listState: LazyListState
) {
    val resources = LocalContext.current.resources

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState
    ) {
        if (tasks.isEmpty()) {
            item {
                PgEmpty(
                    stringResource(R.string.todo_no_task),
                    modifier = Modifier.fillParentMaxHeight()
                        .padding(bottom = 100.dp)
                )
            }
        } else {
            items(
                items = tasks,
                key = { item -> item.identifier() },
            ) {
                when (it) {
                    is ToDoTaskItem.CompleteHeader -> {
                        Spacer(Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp)
                                .fillMaxWidth()
                                .height(32.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.todo_add_task_completed),
                                style = MaterialTheme.typography.titleSmall,
                                color = color
                            )
                        }
                    }
                    is ToDoTaskItem.Complete -> {
                        PgToDoItemCell(
                            modifier = Modifier.animateItem(
                                fadeInSpec = null,
                                placementSpec = MotionTokens.listPlacementSpec(),
                                fadeOutSpec = null
                            ),
                            name = it.toDoTask.name,
                            color = color.copy(alpha = AlphaDisabled),
                            contentPaddingValues = PaddingValues(all = 8.dp),
                            leftIcon = Icons.Rounded.CheckCircle,
                            textDecoration = TextDecoration.LineThrough,
                            onClick = { onClick(it.toDoTask) },
                            onSwipeToDelete = { onSwipeToDelete(it.toDoTask) },
                            onStatusClick = { onStatusClick(it.toDoTask) },
                            info = it.toDoTask.itemInfoDisplayable(resources, MaterialTheme.colorScheme.error),
                            undoEnabled = false,
                            onRequestDelete = { onSwipeToDelete(it.toDoTask) }
                        )
                    }
                    is ToDoTaskItem.InProgress -> {
                        PgToDoItemCell(
                            modifier = Modifier.animateItem(
                                fadeInSpec = null,
                                placementSpec = MotionTokens.listPlacementSpec(),
                                fadeOutSpec = null
                            ),
                            name = it.toDoTask.name,
                            color = color,
                            contentPaddingValues = PaddingValues(all = 8.dp),
                            leftIcon = Icons.Rounded.RadioButtonUnchecked,
                            textDecoration = TextDecoration.None,
                            onClick = { onClick(it.toDoTask) },
                            onSwipeToDelete = { onSwipeToDelete(it.toDoTask) },
                            onStatusClick = { onStatusClick(it.toDoTask) },
                            info = it.toDoTask.itemInfoDisplayable(resources, MaterialTheme.colorScheme.error),
                            undoEnabled = false,
                            onRequestDelete = { onSwipeToDelete(it.toDoTask) }
                        )
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(180.dp))
        }
    }
}

@Composable
fun TaskEditor(
    viewModel: ListDetailViewModel,
) {
    val focusRequest = remember { FocusRequester() }
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        launch { focusRequest.requestFocusImeAware() }
        viewModel.dispatch(ListDetailAction.TaskAction.OnShow)
    }

    Column {
        PgToDoCreator(
            value = state.taskName,
            modifier = Modifier.focusRequester(focusRequest),
            isValid = state.validTaskName,
            placeholder = stringResource(R.string.todo_add_task),
            onValueChange = { viewModel.dispatch(ListDetailAction.TaskAction.ChangeTaskName(it)) },
            onSubmit = { viewModel.dispatch(ListDetailAction.TaskAction.ClickSubmit) },
            onNextSubmit = { viewModel.dispatch(ListDetailAction.TaskAction.ClickImeDone) }
        )

        TaskQuadrantSelector(
            selectedQuadrant = state.taskQuadrant,
            onSelectQuadrant = {
                viewModel.dispatch(ListDetailAction.TaskAction.ChangeTaskQuadrant(it))
            }
        )
    }
}

@Composable
private fun TaskQuadrantSelector(
    selectedQuadrant: TaskQuadrant,
    onSelectQuadrant: (TaskQuadrant) -> Unit
) {
    val quadrants = listOf(
        TaskQuadrant.Q1,
        TaskQuadrant.Q2,
        TaskQuadrant.Q3,
        TaskQuadrant.Q4
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "\u56DB\u8C61\u9650",
            style = MaterialTheme.typography.titleSmall
        )

        quadrants.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { quadrant ->
                    FilterChip(
                        modifier = Modifier.weight(1f),
                        selected = selectedQuadrant == quadrant,
                        onClick = { onSelectQuadrant(quadrant) },
                        label = {
                            Text(
                                text = quadrant.toDisplayName(),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    )
                }
            }
        }
    }
}

private fun TaskQuadrant.toDisplayName(): String {
    return when (this) {
        TaskQuadrant.Q1 -> "\u91CD\u8981\u4E14\u7D27\u6025"
        TaskQuadrant.Q2 -> "\u91CD\u8981\u4E0D\u7D27\u6025"
        TaskQuadrant.Q3 -> "\u4E0D\u91CD\u8981\u4F46\u7D27\u6025"
        TaskQuadrant.Q4 -> "\u4E0D\u91CD\u8981\u4E0D\u7D27\u6025"
    }
}
