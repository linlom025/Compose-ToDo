package com.wisnu.kurniawan.composetodolist.foundation.share

import java.security.MessageDigest

data class ParsedSharedTaskText(
    val title: String,
    val note: String,
)

data class ParsedClipboardTaskText(
    val title: String,
    val note: String,
    val fingerprint: String,
    val contentFingerprint: String,
    val patternKey: String,
    val decisionScore: Int,
    val decisionLevel: ClipboardDecisionLevel,
    val decisionReasons: List<ReasonCode>,
)

enum class ClipboardDecisionLevel {
    ACCEPT,
    SOFT,
    REJECT
}

enum class ReasonCode {
    EMPTY_TEXT,
    TITLE_TOO_SHORT,
    TITLE_TOO_LONG,
    URL_ONLY,
    EMAIL_ONLY,
    NUMBER_ONLY,
    SYMBOL_ONLY,
    COMMAND_LIKE,
    CODE_LIKE,
    MARKETING_LIKE,
    QUESTION_LIKE,
    NEWS_LIKE,
    NOISE_LIKE,
    TASK_MARKER,
    TASK_VERB,
    TIME_HINT,
    DEADLINE_HINT,
    ADAPTIVE_POSITIVE,
    ADAPTIVE_NEGATIVE,
}

data class ClipboardDecision(
    val score: Int,
    val level: ClipboardDecisionLevel,
    val reasons: List<ReasonCode>,
    val parsedDraft: ParsedClipboardTaskText?,
)

object SharedTextTaskParser {
    private const val MIN_TITLE_LENGTH = 2
    private const val MAX_TITLE_LENGTH = 80
    private const val HARD_MAX_TITLE_LENGTH = 120
    private const val HARD_MAX_TEXT_LENGTH = 1000
    private const val ACCEPT_THRESHOLD = 70
    private const val SOFT_THRESHOLD = 45

    private val urlRegex = Regex("^(https?://|www\\.)\\S+$", RegexOption.IGNORE_CASE)
    private val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val numberOnlyRegex = Regex("^\\d+$")
    private val symbolOnlyRegex = Regex("^[^\\p{L}\\p{N}]+$")
    private val commandLikeRegex = Regex(
        "^(adb|git|npm|pnpm|yarn|curl|wget|python|java|gradle|\\./|sh\\s+)\\b",
        RegexOption.IGNORE_CASE
    )
    private val codeLikeRegex = Regex(
        "(```|\\b(class|fun|public|private|import|SELECT|INSERT|UPDATE|DELETE|FROM|WHERE)\\b|\\{\\s*\\}|;\\s*$)",
        RegexOption.IGNORE_CASE
    )
    private val todoMarkerRegex = Regex("(^|\\s)(todo|to\\s*do|待办|待辦|-\\s*\\[\\s*\\]|\\[\\s*\\])($|\\s)", RegexOption.IGNORE_CASE)
    private val taskVerbRegex = Regex(
        "(提交|完成|安排|处理|跟进|整理|发送|回复|记得|需要|准备|修复|确认|联系|购买|买|检查|review|finish|submit|send|reply|fix|plan|schedule|remember|buy)",
        RegexOption.IGNORE_CASE
    )
    private val timeHintRegex = Regex("(今天|明天|今晚|下班前|本周|周[一二三四五六日天]|\\d{1,2}[:：]\\d{2}|\\d+分钟后|\\d+小时后)", RegexOption.IGNORE_CASE)
    private val deadlineHintRegex = Regex("(截止|之前|下班前|会前|到期|due|before|deadline)", RegexOption.IGNORE_CASE)
    private val marketingLikeRegex = Regex("(点击|优惠|活动|直播|转发|关注|抽奖|福利|领取|限时|爆款)", RegexOption.IGNORE_CASE)
    private val questionLikeRegex = Regex("(^为什么|^怎么|^如何|^请问|\\?$|？$)")
    private val newsLikeRegex = Regex("(据.+报道|最新消息|快讯|记者|新华社|公告|发布会|通报)")

    @Volatile
    private var adaptiveBiasByPattern: Map<String, Int> = emptyMap()

    fun updateAdaptiveBias(biasByPattern: Map<String, Int>) {
        adaptiveBiasByPattern = biasByPattern
    }

    fun parse(rawText: String): ParsedSharedTaskText? {
        val lines = normalizeToNonBlankLines(rawText)
        if (lines.isEmpty()) return null

        val title = lines.first()
        val note = lines.drop(1).joinToString("\n").trim()
        return ParsedSharedTaskText(title = title, note = note)
    }

    fun parseClipboardCandidate(rawText: String): ParsedClipboardTaskText? {
        val decision = evaluateClipboard(rawText)
        return decision.parsedDraft
    }

    fun evaluateClipboard(rawText: String): ClipboardDecision {
        val lines = normalizeToNonBlankLines(rawText)
        if (lines.isEmpty()) {
            return ClipboardDecision(
                score = 0,
                level = ClipboardDecisionLevel.REJECT,
                reasons = listOf(ReasonCode.EMPTY_TEXT),
                parsedDraft = null
            )
        }

        val title = lines.first()
        val normalizedText = lines.joinToString("\n")

        val hardRejectReasons = buildList {
            if (title.length < MIN_TITLE_LENGTH) add(ReasonCode.TITLE_TOO_SHORT)
            if (title.length > HARD_MAX_TITLE_LENGTH) add(ReasonCode.TITLE_TOO_LONG)
            if (normalizedText.length > HARD_MAX_TEXT_LENGTH) add(ReasonCode.NEWS_LIKE)
            if (urlRegex.matches(title)) add(ReasonCode.URL_ONLY)
            if (emailRegex.matches(title)) add(ReasonCode.EMAIL_ONLY)
            if (numberOnlyRegex.matches(title)) add(ReasonCode.NUMBER_ONLY)
            if (symbolOnlyRegex.matches(title)) add(ReasonCode.SYMBOL_ONLY)
            if (commandLikeRegex.containsMatchIn(title)) add(ReasonCode.COMMAND_LIKE)
            if (codeLikeRegex.containsMatchIn(rawText)) add(ReasonCode.CODE_LIKE)
        }

        if (hardRejectReasons.isNotEmpty()) {
            return ClipboardDecision(
                score = 0,
                level = ClipboardDecisionLevel.REJECT,
                reasons = hardRejectReasons,
                parsedDraft = null
            )
        }

        var score = 50
        val reasons = mutableSetOf<ReasonCode>()

        if (todoMarkerRegex.containsMatchIn(rawText)) {
            score += 25
            reasons += ReasonCode.TASK_MARKER
        }
        if (taskVerbRegex.containsMatchIn(title)) {
            score += 18
            reasons += ReasonCode.TASK_VERB
        }
        if (timeHintRegex.containsMatchIn(rawText)) {
            score += 14
            reasons += ReasonCode.TIME_HINT
        }
        if (deadlineHintRegex.containsMatchIn(rawText)) {
            score += 12
            reasons += ReasonCode.DEADLINE_HINT
        }

        if (marketingLikeRegex.containsMatchIn(rawText)) {
            score -= 22
            reasons += ReasonCode.MARKETING_LIKE
        }
        if (questionLikeRegex.containsMatchIn(title)) {
            score -= 20
            reasons += ReasonCode.QUESTION_LIKE
        }
        if (newsLikeRegex.containsMatchIn(rawText)) {
            score -= 16
            reasons += ReasonCode.NEWS_LIKE
        }
        if (isNoiseLike(title)) {
            score -= 16
            reasons += ReasonCode.NOISE_LIKE
        }
        if (title.length > MAX_TITLE_LENGTH) {
            score -= 20
            reasons += ReasonCode.TITLE_TOO_LONG
        }

        val patternKey = buildPatternKey(title)
        val adaptiveBias = adaptiveBiasByPattern[patternKey] ?: 0
        if (adaptiveBias > 0) {
            score += 15
            reasons += ReasonCode.ADAPTIVE_POSITIVE
        } else if (adaptiveBias < 0) {
            score -= 15
            reasons += ReasonCode.ADAPTIVE_NEGATIVE
        }

        score = score.coerceIn(0, 100)
        val level = when {
            score >= ACCEPT_THRESHOLD -> ClipboardDecisionLevel.ACCEPT
            score >= SOFT_THRESHOLD -> ClipboardDecisionLevel.SOFT
            else -> ClipboardDecisionLevel.REJECT
        }

        val parsedDraft = if (level == ClipboardDecisionLevel.REJECT) {
            null
        } else {
            val contentFingerprint = calculateFingerprint(normalizedText)
            ParsedClipboardTaskText(
                title = normalizedText,
                note = "",
                fingerprint = contentFingerprint,
                contentFingerprint = contentFingerprint,
                patternKey = patternKey,
                decisionScore = score,
                decisionLevel = level,
                decisionReasons = reasons.toList()
            )
        }

        return ClipboardDecision(
            score = score,
            level = level,
            reasons = reasons.toList(),
            parsedDraft = parsedDraft
        )
    }

    private fun normalizeToNonBlankLines(rawText: String): List<String> {
        return rawText
            .replace("\r\n", "\n")
            .replace('\r', '\n')
            .trim()
            .split("\n")
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }

    private fun buildPatternKey(title: String): String {
        return title
            .lowercase()
            .replace(Regex("\\d+"), "#")
            .replace(Regex("[^\\p{L}\\s#]"), " ")
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .take(6)
            .joinToString(" ")
            .trim()
    }

    private fun isNoiseLike(text: String): Boolean {
        val lettersOrDigits = text.count { it.isLetterOrDigit() }
        if (lettersOrDigits == 0) return true
        val punctuationOrSymbol = text.count { !it.isLetterOrDigit() && !it.isWhitespace() }
        val ratio = punctuationOrSymbol.toDouble() / text.length.toDouble().coerceAtLeast(1.0)
        return ratio > 0.45
    }

    private fun calculateFingerprint(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(value.toByteArray(Charsets.UTF_8))
        return bytes.joinToString(separator = "") { byte -> "%02x".format(byte) }
    }
}
