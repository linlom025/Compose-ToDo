package com.wisnu.kurniawan.composetodolist.features.todo.taskreminder.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import com.wisnu.foundation.coredatetime.toMillis
import com.wisnu.foundation.coreloggr.Loggr
import com.wisnu.kurniawan.composetodolist.R
import com.wisnu.kurniawan.composetodolist.features.todo.taskreminder.ui.TaskBroadcastReceiver
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.provider.AppDisplayNameProvider
import com.wisnu.kurniawan.composetodolist.foundation.extension.toDisplayableDate
import com.wisnu.kurniawan.composetodolist.foundation.extension.toDisplayableDateTime
import com.wisnu.kurniawan.composetodolist.foundation.localization.LocalizationUtil
import com.wisnu.kurniawan.composetodolist.model.QuadrantDisplayNames
import com.wisnu.kurniawan.composetodolist.model.ToDoList
import com.wisnu.kurniawan.composetodolist.model.ToDoTask
import com.wisnu.kurniawan.composetodolist.runtime.navigation.StepFlow
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@Singleton
class TaskNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    appDisplayNameProvider: AppDisplayNameProvider,
) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    @Volatile
    private var quadrantDisplayNames = QuadrantDisplayNames.default()

    init {
        initChannel()
        scope.launch {
            appDisplayNameProvider.getDisplayNameConfig().collect { config ->
                quadrantDisplayNames = config.quadrantTitles
            }
        }
    }

    private fun initChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val localizedContext = getLocalizedContext()
            val name = localizedContext.getString(R.string.todo_task_channel_name)
            val description = localizedContext.getString(R.string.todo_task_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH

            NotificationChannel(CHANNEL_ID, name, importance).apply {
                this.description = description
                enableLights(true)
                lightColor = ResourcesCompat.getColor(localizedContext.resources, R.color.light_primary, null)
                enableVibration(true)
                notificationManager?.createNotificationChannel(this)
            }
        }
    }

    fun show(
        task: ToDoTask,
        toDoList: ToDoList,
        reminderKind: ReminderKind,
        leadMinutes: Int?,
    ) {
        Loggr.debug("AlarmFlow") {
            "Show notification taskId=${task.id}, listId=${toDoList.id}, kind=$reminderKind, leadMinutes=$leadMinutes"
        }
        val builder = buildNotification(task, toDoList, reminderKind, leadMinutes)
        val id = task.createdAt.toMillis().toInt()
        notificationManager?.notify(id, builder.build())
    }

    fun dismiss(task: ToDoTask) {
        Loggr.debug("AlarmFlow") { "Dismiss notification taskId=${task.id}" }
        val id = task.createdAt.toMillis().toInt()
        notificationManager?.cancel(id)
    }

    fun showTestNotification(): TaskNotificationSendResult {
        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        if (!hasPermission) {
            Loggr.debug("AlarmFlow") { "Test notification blocked: permission denied" }
            return TaskNotificationSendResult.PERMISSION_DENIED
        }
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            Loggr.debug("AlarmFlow") { "Test notification blocked: notification disabled" }
            return TaskNotificationSendResult.NOTIFICATION_DISABLED
        }

        val now = LocalDateTime.now()
        val testTask = ToDoTask(
            id = "test_notification",
            name = "ll-todo test notification: reminder is working",
            dueDate = now.plusMinutes(15),
            isDueDateTimeSet = true,
            createdAt = now,
            updatedAt = now
        )
        val testList = ToDoList(
            id = "test_list",
            name = "ll-todo",
            createdAt = now,
            updatedAt = now
        )
        show(
            task = testTask,
            toDoList = testList,
            reminderKind = ReminderKind.CUSTOM_LEAD,
            leadMinutes = 15
        )
        return TaskNotificationSendResult.SENT
    }

    private fun buildNotification(
        task: ToDoTask,
        toDoList: ToDoList,
        reminderKind: ReminderKind,
        leadMinutes: Int?,
    ): NotificationCompat.Builder {
        val localizedContext = getLocalizedContext()
        val title = buildNotificationTitle(localizedContext, task, reminderKind, leadMinutes)
        val content = buildNotificationContent(localizedContext, task, toDoList)

        return NotificationCompat.Builder(context, CHANNEL_ID).apply {
            setSmallIcon(R.drawable.ic_round_check_24)
            setContentTitle(title)
            setContentText(content)
            setStyle(NotificationCompat.BigTextStyle().bigText(content))
            setColor(ResourcesCompat.getColor(localizedContext.resources, R.color.light_primary, null))
            setContentIntent(buildPendingIntent(task.id, toDoList.id))
            setAutoCancel(true)
            setColorized(true)
            setShowWhen(true)
            priority = NotificationCompat.PRIORITY_HIGH
            addAction(getSnoozeAction(task.id))
            addAction(getCompleteAction(task.id))
        }
    }

    private fun buildNotificationTitle(
        localizedContext: Context,
        task: ToDoTask,
        reminderKind: ReminderKind,
        leadMinutes: Int?,
    ): String {
        return when (reminderKind) {
            ReminderKind.CUSTOM_LEAD -> localizedContext.getString(
                R.string.todo_task_notification_title_custom_lead,
                leadMinutes ?: 0,
                task.name
            )
            ReminderKind.THIRTY_MIN_LEFT -> localizedContext.getString(
                R.string.todo_task_notification_title_custom_lead,
                30,
                task.name
            )
            ReminderKind.DUE_NOW -> localizedContext.getString(
                R.string.todo_task_notification_title_due_now,
                task.name
            )
        }
    }

    private fun buildNotificationContent(
        localizedContext: Context,
        task: ToDoTask,
        toDoList: ToDoList,
    ): String {
        val dueDateText = task.dueDate?.let { due ->
            if (task.isDueDateTimeSet) {
                due.toDisplayableDateTime()
            } else {
                due.toLocalDate().toDisplayableDate()
            }
        } ?: localizedContext.getString(R.string.todo_task_due_date_today)

        return localizedContext.getString(
            R.string.todo_task_notification_content,
            toDoList.toDisplayableName(localizedContext),
            dueDateText
        )
    }

    private fun ToDoList.toDisplayableName(localizedContext: Context): String {
        return when (id) {
            "system_quadrant_q1" -> quadrantDisplayNames.q1
            "system_quadrant_q2" -> quadrantDisplayNames.q2
            "system_quadrant_q3" -> quadrantDisplayNames.q3
            "system_quadrant_q4" -> quadrantDisplayNames.q4
            else -> name
        }
    }

    private fun buildPendingIntent(taskId: String, listId: String): PendingIntent {
        val openTaskIntent = Intent(
            Intent.ACTION_VIEW,
            StepFlow.TaskDetailScreen.deeplink(taskId, listId).toUri()
        )
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        return TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(openTaskIntent)
            getPendingIntent(buildOpenTaskRequestCode(taskId), flags)
        }
    }

    private fun getCompleteAction(taskId: String): NotificationCompat.Action {
        val actionTitle = getLocalizedContext().getString(R.string.todo_done)
        val intent = getActionIntent(taskId, TaskBroadcastReceiver.ACTION_NOTIFICATION_COMPLETED, ACTION_COMPLETE_SEED)
        return NotificationCompat.Action(ACTION_NO_ICON, actionTitle, intent)
    }

    private fun getSnoozeAction(taskId: String): NotificationCompat.Action {
        val actionTitle = getLocalizedContext().getString(R.string.todo_task_notification_action_snooze)
        val intent = getActionIntent(taskId, TaskBroadcastReceiver.ACTION_NOTIFICATION_SNOOZE, ACTION_SNOOZE_SEED)
        return NotificationCompat.Action(ACTION_NO_ICON, actionTitle, intent)
    }

    private fun getActionIntent(
        taskId: String,
        intentAction: String,
        seed: Int
    ): PendingIntent {
        val receiverIntent = Intent(context, TaskBroadcastReceiver::class.java).apply {
            action = intentAction
            putExtra(TaskBroadcastReceiver.EXTRA_TASK_ID, taskId)
        }

        return PendingIntent.getBroadcast(
            context,
            buildActionRequestCode(taskId, seed),
            receiverIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun buildOpenTaskRequestCode(taskId: String): Int {
        return ("open_$taskId").hashCode()
    }

    private fun buildActionRequestCode(taskId: String, seed: Int): Int {
        return ("$seed:$taskId").hashCode()
    }

    private fun getLocalizedContext(): Context {
        val locale = AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault()
        return LocalizationUtil.applyLanguageContext(context, locale)
    }

    companion object {
        private const val ACTION_COMPLETE_SEED = 2
        private const val ACTION_SNOOZE_SEED = 3
        private const val ACTION_NO_ICON = 0

        private const val CHANNEL_ID = "task_notification_channel"
    }
}
