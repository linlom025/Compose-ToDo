package com.wisnu.kurniawan.composetodolist.runtime

internal data class ClipboardCheckDecision(
    val shouldCheck: Boolean,
    val reason: String
)

internal enum class ClipboardCheckResult {
    SUCCESS_PUBLISHED,
    SUCCESS_IGNORED,
    SKIP_DISABLED,
    SKIP_NO_TEXT_MIME,
    SKIP_NO_CLIP_ITEM,
    SKIP_BLANK_TEXT,
    RETRYABLE_UNAVAILABLE
}

internal data class ClipboardCheckGateState(
    val quickFillReady: Boolean,
    val isForegroundResumed: Boolean,
    val hasWindowFocus: Boolean,
    val pendingCheckOnReady: Boolean
)

internal class ClipboardCheckGate {
    private var quickFillReady: Boolean = false
    private var isForegroundResumed: Boolean = false
    private var hasWindowFocus: Boolean = false
    private var pendingCheckOnReady: Boolean = false
    private var checkedForCurrentStart: Boolean = false

    fun onStart() {
        checkedForCurrentStart = false
    }

    fun onResume(): ClipboardCheckDecision {
        isForegroundResumed = true
        if (checkedForCurrentStart) {
            return ClipboardCheckDecision(
                shouldCheck = false,
                reason = "resume_checked_once"
            )
        }
        if (!quickFillReady) {
            pendingCheckOnReady = true
            return ClipboardCheckDecision(
                shouldCheck = false,
                reason = "resume_wait_pref"
            )
        }
        if (!hasWindowFocus) {
            pendingCheckOnReady = true
            return ClipboardCheckDecision(
                shouldCheck = false,
                reason = "resume_wait_focus"
            )
        }
        return ClipboardCheckDecision(
            shouldCheck = true,
            reason = "resume_triggered"
        )
    }

    fun onStop() {
        isForegroundResumed = false
        hasWindowFocus = false
        pendingCheckOnReady = false
    }

    fun onWindowFocusChanged(hasFocus: Boolean): ClipboardCheckDecision {
        hasWindowFocus = hasFocus
        if (!hasFocus) {
            return ClipboardCheckDecision(
                shouldCheck = false,
                reason = "focus_lost"
            )
        }
        if (checkedForCurrentStart) {
            return ClipboardCheckDecision(
                shouldCheck = false,
                reason = "focus_checked_once"
            )
        }
        if (!isForegroundResumed) {
            return ClipboardCheckDecision(
                shouldCheck = false,
                reason = "focus_without_resume"
            )
        }
        if (!quickFillReady) {
            pendingCheckOnReady = true
            return ClipboardCheckDecision(
                shouldCheck = false,
                reason = "focus_wait_pref"
            )
        }
        return ClipboardCheckDecision(
            shouldCheck = true,
            reason = "focus_triggered"
        )
    }

    fun onQuickFillPreferenceLoaded(): ClipboardCheckDecision {
        val wasReady = quickFillReady
        quickFillReady = true
        if (checkedForCurrentStart) {
            return ClipboardCheckDecision(
                shouldCheck = false,
                reason = if (wasReady) "pref_update_checked_once" else "pref_ready_checked_once"
            )
        }
        if (!isForegroundResumed) {
            return ClipboardCheckDecision(
                shouldCheck = false,
                reason = if (wasReady) "pref_update_background" else "pref_ready_background"
            )
        }
        if (!pendingCheckOnReady && !hasWindowFocus) {
            return ClipboardCheckDecision(
                shouldCheck = false,
                reason = if (wasReady) "pref_update_no_pending" else "pref_ready_no_pending"
            )
        }
        if (!hasWindowFocus) {
            return ClipboardCheckDecision(
                shouldCheck = false,
                reason = if (wasReady) "pref_update_wait_focus" else "pref_ready_wait_focus"
            )
        }
        return ClipboardCheckDecision(
            shouldCheck = true,
            reason = if (pendingCheckOnReady) "pref_ready_pending" else "pref_ready_focus"
        )
    }

    fun onCheckFinished(result: ClipboardCheckResult) {
        when (result) {
            ClipboardCheckResult.RETRYABLE_UNAVAILABLE -> {
                checkedForCurrentStart = false
                pendingCheckOnReady = true
            }

            else -> {
                checkedForCurrentStart = true
                pendingCheckOnReady = false
            }
        }
    }

    fun snapshot(): ClipboardCheckGateState {
        return ClipboardCheckGateState(
            quickFillReady = quickFillReady,
            isForegroundResumed = isForegroundResumed,
            hasWindowFocus = hasWindowFocus,
            pendingCheckOnReady = pendingCheckOnReady
        )
    }
}
