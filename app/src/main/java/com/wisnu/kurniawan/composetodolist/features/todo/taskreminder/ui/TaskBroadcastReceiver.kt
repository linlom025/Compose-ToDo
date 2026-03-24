package com.wisnu.kurniawan.composetodolist.features.todo.taskreminder.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.wisnu.foundation.coreloggr.Loggr
import com.wisnu.kurniawan.composetodolist.features.todo.taskreminder.data.ReminderKind
import com.wisnu.kurniawan.composetodolist.foundation.security.StorageEncryptionMigrationCoordinator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TaskBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var taskReminderViewModel: TaskReminderViewModel

    @Inject
    lateinit var migrationCoordinator: StorageEncryptionMigrationCoordinator

    override fun onReceive(context: Context?, intent: Intent?) {
        Loggr.debug("AlarmFlow") { "onReceive ${intent?.action}" }
        val action = intent?.action ?: return

        if (!migrationCoordinator.isMigrationCompleted()) {
            Loggr.debug("AlarmFlow") {
                "迁移未完成，跳过提醒广播 action=$action"
            }
            return
        }

        when (action) {
            ACTION_ALARM_SHOW -> {
                taskReminderViewModel.dispatch(
                    TaskReminderAction.AlarmShow(
                        taskId = getTaskId(intent),
                        reminderKind = getReminderKind(intent),
                        leadMinutes = getLeadMinutes(intent)
                    )
                )
            }
            ACTION_NOTIFICATION_COMPLETED -> {
                taskReminderViewModel.dispatch(TaskReminderAction.NotificationCompleted(getTaskId(intent)))
            }
            ACTION_NOTIFICATION_SNOOZE -> {
                taskReminderViewModel.dispatch(TaskReminderAction.NotificationSnooze(getTaskId(intent)))
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                taskReminderViewModel.dispatch(TaskReminderAction.AppBootCompleted)
            }
        }
    }

    private fun getTaskId(intent: Intent?) = intent?.getStringExtra(EXTRA_TASK_ID) ?: ""
    private fun getLeadMinutes(intent: Intent?) = intent?.getIntExtra(EXTRA_LEAD_MINUTES, -1)?.takeIf { it > 0 }
    private fun getReminderKind(intent: Intent?): ReminderKind {
        val raw = intent?.getStringExtra(EXTRA_REMINDER_KIND) ?: return ReminderKind.DUE_NOW
        return ReminderKind.entries.firstOrNull { it.name == raw } ?: ReminderKind.DUE_NOW
    }

    companion object {
        const val EXTRA_TASK_ID = "com.wisnu.kurniawan.intent.extra.TASK_ID"
        const val EXTRA_REMINDER_KIND = "com.wisnu.kurniawan.intent.extra.REMINDER_KIND"
        const val EXTRA_LEAD_MINUTES = "com.wisnu.kurniawan.intent.extra.LEAD_MINUTES"

        const val ACTION_ALARM_SHOW = "com.wisnu.kurniawan.intent.action.ALARM_SHOW"
        const val ACTION_NOTIFICATION_COMPLETED = "com.wisnu.kurniawan.intent.action.NOTIFICATION_COMPLETED"
        const val ACTION_NOTIFICATION_SNOOZE = "com.wisnu.kurniawan.intent.action.NOTIFICATION_SNOOZE"
    }
}
