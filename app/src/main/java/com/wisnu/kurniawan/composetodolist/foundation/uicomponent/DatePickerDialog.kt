package com.wisnu.kurniawan.composetodolist.foundation.uicomponent

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.wisnu.kurniawan.composetodolist.R
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@Composable
fun PgDatePickerDialog(
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
) {
    val appLocale = AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault()
    val weekDays = remember(appLocale) { daysOfWeek() }
    val coroutineScope = rememberCoroutineScope()

    var selectedDate by remember(initialDate) { mutableStateOf(initialDate) }
    var visibleMonth by remember(initialDate) { mutableStateOf(YearMonth.from(initialDate)) }

    val calendarState = rememberCalendarState(
        startMonth = remember { YearMonth.now().minusYears(20) },
        endMonth = remember { YearMonth.now().plusYears(20) },
        firstVisibleMonth = visibleMonth,
        firstDayOfWeek = weekDays.first()
    )

    LaunchedEffect(visibleMonth) {
        val current = calendarState.firstVisibleMonth.yearMonth
        if (current != visibleMonth) {
            calendarState.animateScrollToMonth(visibleMonth)
        }
    }

    LaunchedEffect(calendarState) {
        snapshotFlow { calendarState.firstVisibleMonth.yearMonth }
            .distinctUntilChanged()
            .collect { visibleMonth = it }
    }

    val weekDayLabels = remember(weekDays, appLocale) {
        weekDays.map { day ->
            if (appLocale.language == Locale.SIMPLIFIED_CHINESE.language) {
                day.toChineseWeekLabel()
            } else {
                day.getDisplayName(TextStyle.SHORT, appLocale)
            }
        }
    }

    val monthLabel = remember(visibleMonth, appLocale) {
        if (appLocale.language == Locale.SIMPLIFIED_CHINESE.language) {
            visibleMonth.format(DateTimeFormatter.ofPattern("yyyy年M月", appLocale))
        } else {
            visibleMonth.format(DateTimeFormatter.ofPattern("yyyy MMMM", appLocale))
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.todo_add_due_date_task),
                    style = MaterialTheme.typography.titleSmall
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PgIconButton(
                        onClick = {
                            coroutineScope.launch {
                                calendarState.animateScrollToMonth(visibleMonth.minusMonths(1))
                            }
                        },
                        variant = PgIconButtonVariant.FilledSoft,
                        size = PgIconButtonSize.Small,
                        modifier = Modifier.size(28.dp),
                        enforceMinSize = false,
                        color = MaterialTheme.colorScheme.secondary
                    ) {
                        PgIcon(imageVector = Icons.Rounded.ChevronLeft, modifier = Modifier.size(16.dp))
                    }

                    Text(
                        text = monthLabel,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 10.dp)
                    )

                    PgIconButton(
                        onClick = {
                            coroutineScope.launch {
                                calendarState.animateScrollToMonth(visibleMonth.plusMonths(1))
                            }
                        },
                        variant = PgIconButtonVariant.FilledSoft,
                        size = PgIconButtonSize.Small,
                        modifier = Modifier.size(28.dp),
                        enforceMinSize = false,
                        color = MaterialTheme.colorScheme.secondary
                    ) {
                        PgIcon(imageVector = Icons.Rounded.ChevronRight, modifier = Modifier.size(16.dp))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    weekDayLabels.forEach { label ->
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
                        val isMonthDate = day.position == DayPosition.MonthDate
                        val isSelected = day.date == selectedDate
                        val textColor = when {
                            !isMonthDate -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                            isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                            else -> MaterialTheme.colorScheme.onSurface
                        }

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .background(
                                    color = if (isSelected && isMonthDate) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        Color.Transparent
                                    },
                                    shape = CircleShape
                                )
                                .clickable(
                                    enabled = isMonthDate,
                                    onClick = { selectedDate = day.date },
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
                        }
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = stringResource(R.string.todo_cancel))
                    }
                    Spacer(modifier = Modifier.size(4.dp))
                    TextButton(onClick = { onConfirm(selectedDate) }) {
                        Text(text = stringResource(R.string.todo_done))
                    }
                }
            }
        }
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
