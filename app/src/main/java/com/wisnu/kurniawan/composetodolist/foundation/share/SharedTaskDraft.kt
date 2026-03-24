package com.wisnu.kurniawan.composetodolist.foundation.share

data class SharedTaskDraft(
    val id: Long,
    val rawText: String,
    val title: String,
    val note: String,
    val source: String,
    val fingerprint: String = "",
    val contentFingerprint: String = "",
    val patternKey: String = "",
    val clipboardDecisionLevel: ClipboardDecisionLevel = ClipboardDecisionLevel.ACCEPT,
    val clipboardScore: Int = 100,
    val clipboardReasons: List<ReasonCode> = emptyList(),
)
