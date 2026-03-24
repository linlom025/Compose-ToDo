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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private companion object {
        const val CLIPBOARD_LOG_TAG = "ClipboardDetect"
    }

    @Inject
    lateinit var clipboardImportPreferenceProvider: ClipboardImportPreferenceProvider

    private lateinit var windowState: WindowState
    private var clipboardCheckedForCurrentStart: Boolean = false
    private var quickFillEnabled: Boolean = ClipboardImportPreferenceProvider.DEFAULT_QUICK_FILL_ENABLED

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_ComposeToDoList_Light)
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            clipboardImportPreferenceProvider.getQuickFillEnabled().collect { enabled ->
                quickFillEnabled = enabled
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
        clipboardCheckedForCurrentStart = false
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus) return
        if (clipboardCheckedForCurrentStart) return
        clipboardCheckedForCurrentStart = true

        handleClipboardIntentIfNeeded()
    }

    private fun handleClipboardIntentIfNeeded() {
        if (!quickFillEnabled) return
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
        val clipDescription = clipboardManager.primaryClipDescription ?: return
        val hasTextMime = clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) ||
            clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML)
        if (!hasTextMime) return
        val clipData = clipboardManager.primaryClip ?: return
        if (clipData.itemCount <= 0) return
        val rawText = clipData.getItemAt(0)?.coerceToText(this)?.toString().orEmpty()
        if (rawText.isBlank()) return
        val decision = SharedTextTaskParser.evaluateClipboard(rawText)
        val levelName = when (decision.level) {
            ClipboardDecisionLevel.ACCEPT -> "ACCEPT"
            ClipboardDecisionLevel.SOFT -> "SOFT"
            ClipboardDecisionLevel.REJECT -> "REJECT"
        }
        Log.d(
            CLIPBOARD_LOG_TAG,
            "level=$levelName score=${decision.score} reasons=${decision.reasons.joinToString(",")} length=${rawText.length}"
        )
        SharedTaskDraftRepository.publishFromClipboardText(
            rawText = rawText,
            copyEventMarker = clipDescription.readTimestampMarker()
        )
    }

    private fun ClipDescription.readTimestampMarker(): String {
        val timestamp = runCatching {
            val method = ClipDescription::class.java.getMethod("getTimestamp")
            method.invoke(this) as? Long
        }.getOrNull() ?: 0L

        return if (timestamp > 0L) timestamp.toString() else ""
    }
}

