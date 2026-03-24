package com.wisnu.kurniawan.composetodolist.foundation.share

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SharedTextTaskParserTest {

    @Test
    fun parse_singleLineText_titleOnly() {
        val parsed = SharedTextTaskParser.parse("buy milk")

        assertEquals("buy milk", parsed?.title)
        assertEquals("", parsed?.note)
    }

    @Test
    fun parse_multiLineText_firstLineTitleRemainingNote() {
        val parsed = SharedTextTaskParser.parse("title\nline 1\nline 2")

        assertEquals("title", parsed?.title)
        assertEquals("line 1\nline 2", parsed?.note)
    }

    @Test
    fun parse_blankText_returnNull() {
        val parsed = SharedTextTaskParser.parse(" \n\t\r\n ")

        assertNull(parsed)
    }

    @Test
    fun evaluateClipboard_todoSentence_accept() {
        val decision = SharedTextTaskParser.evaluateClipboard("今天下班前提交周报")

        assertEquals(ClipboardDecisionLevel.ACCEPT, decision.level)
        assertNotNull(decision.parsedDraft)
        assertTrue(decision.reasons.contains(ReasonCode.TASK_VERB))
        assertTrue(decision.reasons.contains(ReasonCode.TIME_HINT))
    }

    @Test
    fun evaluateClipboard_plainQuestion_reject() {
        val decision = SharedTextTaskParser.evaluateClipboard("为什么今天这么热？")

        assertEquals(ClipboardDecisionLevel.REJECT, decision.level)
        assertNull(decision.parsedDraft)
        assertTrue(decision.reasons.contains(ReasonCode.QUESTION_LIKE))
    }

    @Test
    fun parseClipboardCandidate_pureUrl_returnNull() {
        val parsed = SharedTextTaskParser.parseClipboardCandidate("https://example.com")

        assertNull(parsed)
    }

    @Test
    fun parseClipboardCandidate_pureNumber_returnNull() {
        val parsed = SharedTextTaskParser.parseClipboardCandidate("12345678")

        assertNull(parsed)
    }

    @Test
    fun parseClipboardCandidate_fingerprintStableAndDistinct() {
        val first = SharedTextTaskParser.parseClipboardCandidate("buy milk\n2 packs")
        val second = SharedTextTaskParser.parseClipboardCandidate("buy milk\n2 packs")
        val third = SharedTextTaskParser.parseClipboardCandidate("buy eggs\n2 packs")

        assertEquals(first?.fingerprint, second?.fingerprint)
        assertNotEquals(first?.fingerprint, third?.fingerprint)
    }

    @Test
    fun evaluateClipboard_adaptivePositiveBias_raiseLevel() {
        SharedTextTaskParser.updateAdaptiveBias(mapOf("整理一下资料" to 1))

        val decision = SharedTextTaskParser.evaluateClipboard("整理一下资料")

        assertEquals(ClipboardDecisionLevel.ACCEPT, decision.level)
        assertTrue(decision.reasons.contains(ReasonCode.ADAPTIVE_POSITIVE))

        SharedTextTaskParser.updateAdaptiveBias(emptyMap())
    }

    @Test
    fun evaluateClipboard_adaptiveNegativeBias_dropLevel() {
        SharedTextTaskParser.updateAdaptiveBias(mapOf("整理一下资料" to -1))

        val decision = SharedTextTaskParser.evaluateClipboard("整理一下资料")

        assertEquals(ClipboardDecisionLevel.SOFT, decision.level)
        assertTrue(decision.reasons.contains(ReasonCode.ADAPTIVE_NEGATIVE))

        SharedTextTaskParser.updateAdaptiveBias(emptyMap())
    }
}
