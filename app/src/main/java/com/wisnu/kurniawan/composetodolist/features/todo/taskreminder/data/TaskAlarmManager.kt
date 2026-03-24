package com.wisnu.kurniawan.composetodolist.features.todo.taskreminder.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.AlarmManagerCompat
import com.wisnu.foundation.coreloggr.Loggr
import com.wisnu.foundation.coredatetime.toMillis
import com.wisnu.kurniawan.composetodolist.features.todo.taskreminder.ui.TaskBroadcastReceiver
import com.wisnu.kurniawan.composetodolist.model.ToDoTask
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskAlarmManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
    private val createFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    } else {
        PendingIntent.FLAG_UPDATE_CURRENT
    }
    private val noCreateFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
    } else {
        PendingIntent.FLAG_NO_CREATE
    }

    fun scheduleTaskAlarms(task: ToDoTask, schedules: List<ReminderSchedule>) {
        schedules.forEach { schedule ->
            scheduleTaskAlarm(task, schedule.triggerAt, schedule.kind, schedule.leadMinutes)
        }
    }

    fun scheduleTaskAlarm(
        task: ToDoTask,
        time: LocalDateTime,
        kind: ReminderKind = ReminderKind.DUE_NOW,
        leadMinutes: Int? = null,
    ) {
        Loggr.debug("AlarmFlow") {
            "Schedule alarm taskId=${task.id}, kind=$kind, leadMinutes=$leadMinutes, triggerAt=$time"
        }
        val receiverIntent = Intent(context, TaskBroadcastReceiver::class.java).apply {
            action = TaskBroadcastReceiver.ACTION_ALARM_SHOW
            putExtra(TaskBroadcastReceiver.EXTRA_TASK_ID, task.id)
            putExtra(TaskBroadcastReceiver.EXTRA_REMINDER_KIND, kind.name)
            if (leadMinutes != null) {
                putExtra(TaskBroadcastReceiver.EXTRA_LEAD_MINUTES, leadMinutes)
            }
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            buildRequestCode(task.id, kind),
            receiverIntent,
            createFlags
        )

        setAlarm(time.toMillis(), pendingIntent)
    }

    fun cancelTaskAlarms(task: ToDoTask) {
        ReminderKind.entries.forEach { kind ->
            Loggr.debug("AlarmFlow") { "Cancel alarm taskId=${task.id}, kind=$kind" }
            val receiverIntent = Intent(context, TaskBroadcastReceiver::class.java).apply {
                action = TaskBroadcastReceiver.ACTION_ALARM_SHOW
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                buildRequestCode(task.id, kind),
                receiverIntent,
                noCreateFlags
            )
            cancelAlarm(pendingIntent)
        }
    }

    private fun setAlarm(
        triggerAtMillis: Long,
        operation: PendingIntent?
    ) {
        if (operation == null) {
            return
        }

        alarmManager?.let {
            AlarmManagerCompat.setAndAllowWhileIdle(it, AlarmManager.RTC_WAKEUP, triggerAtMillis, operation)
        }
    }

    private fun cancelAlarm(operation: PendingIntent?) {
        if (operation == null) {
            return
        }

        alarmManager?.cancel(operation)
    }

    private fun buildRequestCode(taskId: String, kind: ReminderKind): Int {
        return "${taskId}_${kind.name}".hashCode()
    }

}
