package com.wisnu.kurniawan.composetodolist.features.todo.taskreminder.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskReminderCompatibilityTest {

    @Test
    fun shouldNotifyReminder_shouldIgnoreLegacyThirtyMinuteReminder() {
        assertTrue(shouldNotifyReminder(ReminderKind.CUSTOM_LEAD))
        assertTrue(shouldNotifyReminder(ReminderKind.DUE_NOW))
        assertFalse(shouldNotifyReminder(ReminderKind.THIRTY_MIN_LEFT))
    }
}
