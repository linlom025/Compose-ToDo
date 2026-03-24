package com.wisnu.kurniawan.composetodolist.features.todo.taskreminder.data

enum class ReminderKind {
    CUSTOM_LEAD,
    // Legacy value kept temporarily for cancelling old alarms after upgrade.
    THIRTY_MIN_LEFT,
    DUE_NOW,
}
