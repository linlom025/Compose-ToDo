package com.wisnu.kurniawan.composetodolist.runtime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClipboardCheckGateTest {

    @Test
    fun resumeBeforePreferenceReady_preferenceLoadedShouldAllowRepeatedCheck() {
        val gate = ClipboardCheckGate()
        gate.onStart()

        val resumeDecision = gate.onResume()
        assertFalse(resumeDecision.shouldCheck)
        assertEquals("resume_wait_pref", resumeDecision.reason)

        val prefDecision = gate.onQuickFillPreferenceLoaded()
        assertFalse(prefDecision.shouldCheck)
        assertEquals("pref_ready_wait_focus", prefDecision.reason)

        val focusDecision = gate.onWindowFocusChanged(hasFocus = true)
        assertTrue(focusDecision.shouldCheck)
        assertEquals("focus_triggered", focusDecision.reason)
    }

    @Test
    fun newForegroundCycle_shouldAllowCheckAgain() {
        val gate = ClipboardCheckGate()

        gate.onStart()
        gate.onQuickFillPreferenceLoaded()
        gate.onWindowFocusChanged(hasFocus = true)
        val firstResume = gate.onResume()
        assertTrue(firstResume.shouldCheck)
        assertEquals("resume_triggered", firstResume.reason)
        gate.onCheckFinished(ClipboardCheckResult.SUCCESS_PUBLISHED)

        gate.onStop()
        gate.onStart()
        val secondResume = gate.onResume()
        assertFalse(secondResume.shouldCheck)
        assertEquals("resume_wait_focus", secondResume.reason)
        val secondFocus = gate.onWindowFocusChanged(hasFocus = true)
        assertTrue(secondFocus.shouldCheck)
        assertEquals("focus_triggered", secondFocus.reason)
    }

    @Test
    fun preferenceLoadedWithoutResume_shouldNotCheckUntilResume() {
        val gate = ClipboardCheckGate()
        gate.onStart()

        val prefDecision = gate.onQuickFillPreferenceLoaded()
        assertFalse(prefDecision.shouldCheck)
        assertEquals("pref_ready_background", prefDecision.reason)

        val resumeDecision = gate.onResume()
        assertFalse(resumeDecision.shouldCheck)
        assertEquals("resume_wait_focus", resumeDecision.reason)

        val focusDecision = gate.onWindowFocusChanged(hasFocus = true)
        assertTrue(focusDecision.shouldCheck)
        assertEquals("focus_triggered", focusDecision.reason)
    }

    @Test
    fun focusAsSupplementWithoutResume_shouldNotCheck() {
        val gate = ClipboardCheckGate()
        gate.onStart()
        gate.onQuickFillPreferenceLoaded()

        val focusDecision = gate.onWindowFocusChanged(hasFocus = true)
        assertFalse(focusDecision.shouldCheck)
        assertEquals("focus_without_resume", focusDecision.reason)
    }

    @Test
    fun retryableResult_shouldAllowFocusRetryInSameCycle() {
        val gate = ClipboardCheckGate()
        gate.onStart()
        gate.onQuickFillPreferenceLoaded()

        val resumeDecision = gate.onResume()
        assertFalse(resumeDecision.shouldCheck)
        assertEquals("resume_wait_focus", resumeDecision.reason)

        val firstFocusDecision = gate.onWindowFocusChanged(hasFocus = true)
        assertTrue(firstFocusDecision.shouldCheck)
        gate.onCheckFinished(ClipboardCheckResult.RETRYABLE_UNAVAILABLE)

        val focusDecision = gate.onWindowFocusChanged(hasFocus = true)
        assertTrue(focusDecision.shouldCheck)
        assertEquals("focus_triggered", focusDecision.reason)
    }

    @Test
    fun successfulCheck_shouldBlockRepeatedCheckInSameCycle() {
        val gate = ClipboardCheckGate()
        gate.onStart()
        gate.onQuickFillPreferenceLoaded()

        val resumeDecision = gate.onResume()
        assertFalse(resumeDecision.shouldCheck)
        assertEquals("resume_wait_focus", resumeDecision.reason)

        val focusDecision = gate.onWindowFocusChanged(hasFocus = true)
        assertTrue(focusDecision.shouldCheck)
        assertEquals("focus_triggered", focusDecision.reason)
        gate.onCheckFinished(ClipboardCheckResult.SUCCESS_PUBLISHED)

        val secondFocusDecision = gate.onWindowFocusChanged(hasFocus = true)
        assertFalse(secondFocusDecision.shouldCheck)
        assertEquals("focus_checked_once", secondFocusDecision.reason)
    }
}
