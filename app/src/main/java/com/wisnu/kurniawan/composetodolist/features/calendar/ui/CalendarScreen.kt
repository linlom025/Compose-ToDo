package com.wisnu.kurniawan.composetodolist.features.calendar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.wisnu.kurniawan.composetodolist.R
import com.wisnu.kurniawan.composetodolist.features.calendar.data.CalendarTaskItem
import com.wisnu.kurniawan.composetodolist.foundation.theme.ListBlue
import com.wisnu.kurniawan.composetodolist.foundation.theme.ListGreen
import com.wisnu.kurniawan.composetodolist.foundation.theme.ListOrange
import com.wisnu.kurniawan.composetodolist.foundation.theme.ListRed
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgConfirmDeleteDialog
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgEmpty
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgIcon
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgIconButton
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgIconButtonSize
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgIconButtonVariant
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgPageLayout
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgToDoItemCell
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.itemInfoDisplayable
import com.wisnu.kurniawan.composetodolist.model.TaskQuadrant
import com.wisnu.kurniawan.composetodolist.model.ToDoStatus
import com.wisnu.kurniawan.composetodolist.model.ToDoTask
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onBackClick: () -> Unit,
    onTaskItemClick: (String, String) -> Unit,
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    CalendarContent(
        state = state,
        onBackClick = onBackClick,
        onSwitchMode = { viewModel.dispatch(CalendarAction.SwitchMode(it)) },
        onPreviousMonth = { viewModel.dispatch(CalendarAction.PreviousMonth) },
        onNextMonth = { viewModel.dispatch(CalendarAction.NextMonth) },
        onVisibleMonthChanged = { viewModel.dispatch(CalendarAction.VisibleMonthChanged(it)) },
        onSelectDate = { viewModel.dispatch(CalendarAction.SelectDate(it)) },
        onTaskStatusClick = { viewModel.dispatch(CalendarAction.ToggleTaskStatus(it)) },
        onTaskSwipeToDelete = { viewModel.dispatch(CalendarAction.RequestDeleteTask(it)) },
        onConfirmDeleteTask = { viewModel.dispatch(CalendarAction.ConfirmDeleteTask) },
        onDismissDeleteTask = { viewModel.dispatch(CalendarAction.DismissDeleteTask) },
        onTaskItemClick = onTaskItemClick
    )
}

@Composable
private fun CalendarContent(
    state: CalendarState,
    onBackClick: () -> Unit,
    onSwitchMode: (CalendarMode) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onVisibleMonthChanged: (YearMonth) -> Unit,
    onSelectDate: (LocalDate) -> Unit,
    onTaskStatusClick: (ToDoTask) -> Unit,
    onTaskSwipeToDelete: (ToDoTask) -> Unit,
    onConfirmDeleteTask: () -> Unit,
    onDismissDeleteTask: () -> Unit,
    onTaskItemClick: (String, String) -> Unit,
) {
    val resources = LocalContext.current.resources
    val locale = Locale.getDefault()
    val daysOfWeek = remember { daysOfWeek() }
    val today = remember { LocalDate.now() }
    val startMonth = remember { YearMonth.now().minusYears(10) }
    val endMonth = remember { YearMonth.now().plusYears(10) }
    val calendarState = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = state.visibleMonth,
        firstDayOfWeek = daysOfWeek.first()
    )

    LaunchedEffect(state.visibleMonth) {
        val currentVisibleMonth = calendarState.firstVisibleMonth.yearMonth
        if (currentVisibleMonth != state.visibleMonth) {
            calendarState.animateScrollToMonth(state.visibleMonth)
        }
    }

    LaunchedEffect(calendarState) {
        snapshotFlow { calendarState.firstVisibleMonth.yearMonth }
            .distinctUntilChanged()
            .collect { month -> onVisibleMonthChanged(month) }
    }

    val weekdayLabels = remember(daysOfWeek, locale) {
        daysOfWeek.map { day ->
            if (locale.language == Locale.SIMPLIFIED_CHINESE.language) {
                day.toChineseWeekLabel()
            } else {
                day.getDisplayName(TextStyle.SHORT, locale)
            }
        }
    }
    val monthLabel = remember(state.visibleMonth, locale) {
        if (locale.language == "zh") {
            state.visibleMonth.format(DateTimeFormatter.ofPattern("yyyy年M月", locale))
        } else {
            state.visibleMonth.format(DateTimeFormatter.ofPattern("yyyy MMMM", locale))
        }
    }
    val tasksByDate = if (state.mode == CalendarMode.CALENDAR) {
        state.tasksByCreatedDate
    } else {
        state.tasksByCompletedDate
    }
    val emptyTextRes = if (state.mode == CalendarMode.CALENDAR) {
        R.string.todo_calendar_no_task_on_day
    } else {
        R.string.todo_archive_no_task_on_day
    }

    PgPageLayout(horizontalPadding = 2.dp, topContentPadding = 2.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CalendarNavIconButton(onClick = onBackClick, imageVector = Icons.Rounded.ChevronLeft)

            Spacer(modifier = Modifier.size(8.dp))

            Text(
                text = stringResource(R.string.todo_calendar_title),
                style = MaterialTheme.typography.titleMedium
            )
        }

        CalendarModeSwitch(
            mode = state.mode,
            onSwitchMode = onSwitchMode,
            modifier = Modifier.fillMaxWidth()
        )

        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CalendarNavIconButton(
                        onClick = onPreviousMonth,
                        imageVector = Icons.Rounded.ChevronLeft
                    )

                    Text(
                        text = monthLabel,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    )

                    CalendarNavIconButton(
                        onClick = onNextMonth,
                        imageVector = Icons.Rounded.ChevronRight
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    weekdayLabels.forEach { label ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                HorizontalCalendar(
                    state = calendarState,
                    modifier = Modifier.fillMaxWidth(),
                    dayContent = { day ->
                        CalendarDayCell(
                            day = day,
                            today = today,
                            isSelected = day.date == state.selectedDate,
                            hasTask = !tasksByDate[day.date].isNullOrEmpty(),
                            onClick = { onSelectDate(day.date) }
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (state.selectedDateTasks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                PgEmpty(text = stringResource(emptyTextRes))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.selectedDateTasks, key = { it.task.id }) { item ->
                    CalendarTaskCell(
                        item = item,
                        onTaskStatusClick = onTaskStatusClick,
                        onTaskSwipeToDelete = onTaskSwipeToDelete,
                        onTaskItemClick = onTaskItemClick,
                        infoColor = MaterialTheme.colorScheme.error,
                        resources = resources
                    )
                }
            }
        }
    }

    if (state.showDeleteTaskConfirmDialog) {
        PgConfirmDeleteDialog(
            onConfirm = onConfirmDeleteTask,
            onDismiss = onDismissDeleteTask
        )
    }
}

@Composable
private fun CalendarModeSwitch(
    mode: CalendarMode,
    onSwitchMode: (CalendarMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CalendarModeButton(
                text = stringResource(R.string.todo_calendar_mode_calendar),
                selected = mode == CalendarMode.CALENDAR,
                onClick = { onSwitchMode(CalendarMode.CALENDAR) },
                modifier = Modifier.weight(1f)
            )
            CalendarModeButton(
                text = stringResource(R.string.todo_calendar_mode_archive),
                selected = mode == CalendarMode.ARCHIVE,
                onClick = { onSwitchMode(CalendarMode.ARCHIVE) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CalendarModeButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
    }
    val textColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = textColor,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CalendarTaskCell(
    item: CalendarTaskItem,
    onTaskStatusClick: (ToDoTask) -> Unit,
    onTaskSwipeToDelete: (ToDoTask) -> Unit,
    onTaskItemClick: (String, String) -> Unit,
    infoColor: Color,
    resources: android.content.res.Resources,
) {
    val task = item.task
    PgToDoItemCell(
        name = task.name,
        color = task.quadrant.toColor(),
        contentPaddingValues = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
        leftIcon = if (task.status == ToDoStatus.COMPLETE) {
            Icons.Rounded.CheckCircle
        } else {
            Icons.Rounded.RadioButtonUnchecked
        },
        textDecoration = if (task.status == ToDoStatus.COMPLETE) {
            TextDecoration.LineThrough
        } else {
            TextDecoration.None
        },
        onClick = { onTaskItemClick(task.id, item.listId) },
        onSwipeToDelete = { onTaskSwipeToDelete(task) },
        onStatusClick = { onTaskStatusClick(task) },
        info = task.itemInfoDisplayable(resources, infoColor),
        undoEnabled = false,
        onRequestDelete = { onTaskSwipeToDelete(task) }
    )
}

@Composable
private fun CalendarDayCell(
    day: CalendarDay,
    today: LocalDate,
    isSelected: Boolean,
    hasTask: Boolean,
    onClick: () -> Unit,
) {
    val isMonthDate = day.position == DayPosition.MonthDate
    val isToday = day.date == today
    val backgroundColor = if (isSelected && isMonthDate) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
    }
    val borderColor = if (!isSelected && isToday && isMonthDate) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
    } else {
        Color.Transparent
    }
    val textColor = when {
        !isMonthDate -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(10.dp))
            .clickable(
                enabled = isMonthDate,
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
            color = textColor
        )

        if (hasTask && isMonthDate) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 3.dp)
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(LocalContentColor.current.copy(alpha = 0.78f))
            )
        }
    }
}

@Composable
private fun CalendarNavIconButton(
    onClick: () -> Unit,
    imageVector: ImageVector,
) {
    PgIconButton(
        onClick = onClick,
        modifier = Modifier.size(24.dp),
        color = MaterialTheme.colorScheme.secondary,
        enforceMinSize = false,
        size = PgIconButtonSize.Small,
        variant = PgIconButtonVariant.FilledSoft
    ) {
        PgIcon(
            imageVector = imageVector,
            modifier = Modifier.size(16.dp)
        )
    }
}

private fun TaskQuadrant.toColor(): Color {
    return when (this) {
        TaskQuadrant.Q1 -> ListRed
        TaskQuadrant.Q2 -> ListBlue
        TaskQuadrant.Q3 -> ListOrange
        TaskQuadrant.Q4 -> ListGreen
    }
}

private fun DayOfWeek.toChineseWeekLabel(): String {
    return when (this) {
        DayOfWeek.MONDAY -> "周一"
        DayOfWeek.TUESDAY -> "周二"
        DayOfWeek.WEDNESDAY -> "周三"
        DayOfWeek.THURSDAY -> "周四"
        DayOfWeek.FRIDAY -> "周五"
        DayOfWeek.SATURDAY -> "周六"
        DayOfWeek.SUNDAY -> "周日"
    }
}
