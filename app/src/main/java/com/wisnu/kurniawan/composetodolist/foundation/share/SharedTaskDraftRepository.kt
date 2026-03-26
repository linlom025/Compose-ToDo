package com.wisnu.kurniawan.composetodolist.foundation.share

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicLong

object SharedTaskDraftRepository {
    private val nextId = AtomicLong(1L)
    private val pendingDraftMutable = MutableStateFlow<SharedTaskDraft?>(null)
    private val pendingClipboardCandidateMutable = MutableStateFlow<SharedTaskDraft?>(null)
    val pendingDraft: StateFlow<SharedTaskDraft?> = pendingDraftMutable.asStateFlow()
    val pendingClipboardCandidate: StateFlow<SharedTaskDraft?> = pendingClipboardCandidateMutable.asStateFlow()

    fun publishFromShareText(rawText: String, source: String = "system_share"): SharedTaskDraft? {
        val parsed = SharedTextTaskParser.parse(rawText) ?: return null
        val draft = SharedTaskDraft(
            id = nextId.getAndIncrement(),
            rawText = rawText,
            title = parsed.title,
            note = parsed.note,
            source = source,
            fingerprint = ""
        )
        pendingDraftMutable.value = draft
        return draft
    }

    fun publishFromClipboardText(
        rawText: String,
        source: String = "clipboard",
        copyEventMarker: String = "",
    ): SharedTaskDraft? {
        val decision = SharedTextTaskParser.evaluateClipboard(rawText)
        val parsed = decision.parsedDraft ?: return null

        val uniqueFingerprint = if (copyEventMarker.isBlank()) {
            parsed.fingerprint
        } else {
            "${parsed.fingerprint}:$copyEventMarker"
        }
        val current = pendingClipboardCandidateMutable.value
        if (current?.fingerprint == uniqueFingerprint) return current

        val draft = SharedTaskDraft(
            id = nextId.getAndIncrement(),
            rawText = rawText,
            title = parsed.title,
            note = parsed.note,
            source = source,
            fingerprint = uniqueFingerprint,
            contentFingerprint = parsed.contentFingerprint,
            patternKey = parsed.patternKey,
            clipboardDecisionLevel = parsed.decisionLevel,
            clipboardScore = parsed.decisionScore,
            clipboardReasons = parsed.decisionReasons
        )
        pendingClipboardCandidateMutable.value = draft
        return draft
    }

    fun consume(draftId: Long) {
        if (pendingDraftMutable.value?.id == draftId) {
            pendingDraftMutable.value = null
        }
    }

    fun consumeClipboardCandidate(draftId: Long) {
        if (pendingClipboardCandidateMutable.value?.id == draftId) {
            pendingClipboardCandidateMutable.value = null
        }
    }

    fun clear() {
        pendingDraftMutable.value = null
        pendingClipboardCandidateMutable.value = null
    }
}
