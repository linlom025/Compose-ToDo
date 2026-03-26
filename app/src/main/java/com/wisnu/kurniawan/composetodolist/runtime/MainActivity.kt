package com.wisnu.kurniawan.composetodolist.runtime

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.Surface
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.wisnu.kurniawan.composetodolist.R
import com.wisnu.kurniawan.composetodolist.features.host.ui.Host
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.provider.ClipboardImportPreferenceProvider
import com.wisnu.kurniawan.composetodolist.foundation.share.ClipboardDecisionLevel
import com.wisnu.kurniawan.composetodolist.foundation.share.SharedTextTaskParser
import com.wisnu.kurniawan.composetodolist.foundation.share.SharedTaskDraftRepository
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.ObserveSystemMotionScale
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgSnackbarHostContainer
import com.wisnu.kurniawan.composetodolist.foundation.window.WindowState
import com.wisnu.kurniawan.composetodolist.foundation.window.rememberWindowState
import com.wisnu.kurniawan.composetodolist.runtime.navigation.MainNavHost
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private companion object {
        const val CLIPBOARD_LOG_TAG = "ClipboardDetect"
        const val RETRY_DELAY_MILLIS = 200L
        const val MAX_RETRY_ATTEMPTS = 5
    }

    @Inject
    lateinit var clipboardImportPreferenceProvider: ClipboardImportPreferenceProvider

    private lateinit var windowState: WindowState
    private val clipboardCheckGate = ClipboardCheckGate()
    private var quickFillEnabled: Boolean = ClipboardImportPreferenceProvider.DEFAULT_QUICK_FILL_ENABLED

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_ComposeToDoList_Light)
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            clipboardImportPreferenceProvider.getQuickFillEnabled()
                .distinctUntilChanged()
                .collect { enabled ->
                quickFillEnabled = enabled
                val decision = clipboardCheckGate.onQuickFillPreferenceLoaded()
                logGateDecision(action = "pref_loaded", reason = decision.reason)
                if (decision.shouldCheck) {
                    runClipboardCheck(decision.reason, allowDelayedRetry = true)
                }
                }
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            windowState = rememberWindowState()

            Host {
                Surface {
                    PgSnackbarHostContainer {
                        ObserveSystemMotionScale()
                        MainNavHost(windowState)
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        clipboardCheckGate.onStart()
        logGateDecision(action = "start", reason = "foreground_start")
    }

    override fun onResume() {
        super.onResume()
        val decision = clipboardCheckGate.onResume()
        logGateDecision(action = "resume", reason = decision.reason)
        if (!decision.shouldCheck) return
        runClipboardCheck(decision.reason, allowDelayedRetry = true)
    }

    override fun onStop() {
        clipboardCheckGate.onStop()
        logGateDecision(action = "stop", reason = "foreground_stop")
        super.onStop()
    }

    override fun onPause() {
        logGateDecision(action = "pause", reason = "foreground_pause")
        super.onPause()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        val decision = clipboardCheckGate.onWindowFocusChanged(hasFocus)
        logGateDecision(action = "focus_changed", reason = decision.reason)
        if (!decision.shouldCheck) return
        runClipboardCheck(decision.reason, allowDelayedRetry = false)
    }

    private fun handleClipboardIntentIfNeeded(gateReason: String): ClipboardCheckResult {
        if (!quickFillEnabled) {
            logClipboard(action = "disabled", reason = gateReason)
            return ClipboardCheckResult.SKIP_DISABLED
        }
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        if (clipboardManager == null) {
            logClipboard(action = "retry", reason = "clipboard_manager_null($gateReason)")
            return ClipboardCheckResult.RETRYABLE_UNAVAILABLE
        }
        val clipData = clipboardManager.primaryClip
        val clipDescription = clipboardManager.primaryClipDescription ?: clipData?.description
        if (clipData == null) {
            logClipboard(action = "info", reason = "clip_data_null_try_legacy($gateReason)")
        }
        if (clipDescription == null) {
            logClipboard(
                action = "info",
                reason = "clip_description_null_continue($gateReason)#items=${clipData?.itemCount ?: -1}"
            )
        }
        if (clipData != null && clipData.itemCount <= 0) {
            logClipboard(action = "skip", reason = "no_clip_item($gateReason)")
            return ClipboardCheckResult.SKIP_NO_CLIP_ITEM
        }
        val rawTextFromPrimary = clipData
            ?.getItemAt(0)
            ?.coerceToText(this)
            ?.toString()
            .orEmpty()
        val rawText = if (rawTextFromPrimary.isNotBlank()) {
            rawTextFromPrimary
        } else {
            @Suppress("DEPRECATION")
            clipboardManager.text?.toString().orEmpty()
        }
        if (rawText.isBlank()) {
            val retryReason = "blank_text_unavailable($gateReason)"
            logClipboard(action = "retry", reason = retryReason)
            return ClipboardCheckResult.RETRYABLE_UNAVAILABLE
        }
        val decision = SharedTextTaskParser.evaluateClipboard(rawText)
        val levelName = when (decision.level) {
            ClipboardDecisionLevel.ACCEPT -> "ACCEPT"
            ClipboardDecisionLevel.SOFT -> "SOFT"
            ClipboardDecisionLevel.REJECT -> "REJECT"
        }
        val published = SharedTaskDraftRepository.publishFromClipboardText(
            rawText = rawText,
            copyEventMarker = buildCopyEventMarker(
                clipDescription = clipDescription,
                rawText = rawText
            )
        )
        val publishState = if (published == null) "ignored" else "published"
        logClipboard(
            action = publishState,
            reason = gateReason,
            level = levelName,
            score = decision.score,
            reasons = decision.reasons.joinToString(","),
            length = rawText.length
        )

        return if (published == null) {
            ClipboardCheckResult.SUCCESS_IGNORED
        } else {
            ClipboardCheckResult.SUCCESS_PUBLISHED
        }
    }

    private fun runClipboardCheck(reason: String, allowDelayedRetry: Boolean) {
        val checkResult = handleClipboardIntentIfNeeded(reason)
        clipboardCheckGate.onCheckFinished(checkResult)
        logGateDecision(action = "check_finished", reason = checkResult.name)

        if (!allowDelayedRetry || checkResult != ClipboardCheckResult.RETRYABLE_UNAVAILABLE) {
            return
        }

        lifecycleScope.launch {
            var unresolvedRetry = true
            repeat(MAX_RETRY_ATTEMPTS) { attemptIndex ->
                delay(RETRY_DELAY_MILLIS)
                val state = clipboardCheckGate.snapshot()
                if (!state.isForegroundResumed || !state.hasWindowFocus) {
                    logGateDecision(
                        action = "check_finished_delayed_${attemptIndex + 1}",
                        reason = "cancelled_not_foreground"
                    )
                    unresolvedRetry = false
                    return@launch
                }

                val delayedReason = "$reason:delayed_${attemptIndex + 1}"
                val delayedResult = handleClipboardIntentIfNeeded(delayedReason)
                clipboardCheckGate.onCheckFinished(delayedResult)
                logGateDecision(
                    action = "check_finished_delayed_${attemptIndex + 1}",
                    reason = delayedResult.name
                )
                if (delayedResult != ClipboardCheckResult.RETRYABLE_UNAVAILABLE) {
                    unresolvedRetry = false
                    return@launch
                }
            }
            if (unresolvedRetry) {
                logClipboard(action = "retry_exhausted", reason = reason)
            }
        }
    }

    private fun buildCopyEventMarker(
        clipDescription: ClipDescription?,
        rawText: String
    ): String {
        val stableEventMarker = clipDescription?.readStableEventMarker().orEmpty()
        if (stableEventMarker.isNotBlank()) {
            return stableEventMarker
        }
        return "legacy:${rawText.hashCode()}"
    }

    private fun ClipDescription.readStableEventMarker(): String {
        val timestamp = invokeLongMethod("getTimestamp")
        if (timestamp > 0L) {
            return "ts:$timestamp"
        }
        val sequence = invokeLongMethod("getSequenceNumber")
        if (sequence > 0L) {
            return "seq:$sequence"
        }
        return ""
    }

    private fun ClipDescription.invokeLongMethod(methodName: String): Long {
        val value = runCatching {
            val method = ClipDescription::class.java.getMethod(methodName)
            method.invoke(this)
        }.getOrNull()

        return when (value) {
            is Long -> value
            is Int -> value.toLong()
            else -> 0L
        }
    }

    private fun logGateDecision(action: String, reason: String) {
        val state = clipboardCheckGate.snapshot()
        Log.d(
            CLIPBOARD_LOG_TAG,
            "action=$action reason=$reason quickFillEnabled=$quickFillEnabled quickFillReady=${state.quickFillReady} resumed=${state.isForegroundResumed} hasFocus=${state.hasWindowFocus} pending=${state.pendingCheckOnReady}"
        )
    }

    private fun logClipboard(
        action: String,
        reason: String,
        level: String = "",
        score: Int = -1,
        reasons: String = "",
        length: Int = 0
    ) {
        val state = clipboardCheckGate.snapshot()
        Log.d(
            CLIPBOARD_LOG_TAG,
            "action=$action reason=$reason quickFillEnabled=$quickFillEnabled quickFillReady=${state.quickFillReady} resumed=${state.isForegroundResumed} hasFocus=${state.hasWindowFocus} level=$level score=$score reasons=$reasons length=$length"
        )
    }
}

