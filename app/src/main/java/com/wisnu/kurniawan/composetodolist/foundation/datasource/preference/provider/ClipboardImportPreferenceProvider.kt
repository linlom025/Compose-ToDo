package com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.provider

import androidx.datastore.core.DataStore
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.model.ClipboardPatternFeedback
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.model.UserClipboardImportPreference
import com.wisnu.kurniawan.composetodolist.foundation.di.DiName
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ClipboardImportPreferenceProvider @Inject constructor(
    @Named(DiName.DISPATCHER_IO) private val dispatcher: CoroutineDispatcher,
    private val clipboardImportDataStore: DataStore<UserClipboardImportPreference>,
) {

    data class PatternFeedbackState(
        val positiveTotal: Int,
        val negativeTotal: Int,
        val positiveStreak: Int,
        val negativeStreak: Int
    )

    fun getLastHandledClipboardFingerprint(): Flow<String> {
        return clipboardImportDataStore.data
            .map { it.lastHandledFingerprint }
            .catch { throwable ->
                if (throwable is IOException) {
                    emit(DEFAULT_FINGERPRINT)
                } else {
                    throw throwable
                }
            }
            .flowOn(dispatcher)
    }

    fun getAdaptiveBiasByPattern(): Flow<Map<String, Int>> {
        return clipboardImportDataStore.data
            .map { preference ->
                preference.patternFeedbacksList.associate { feedback ->
                    feedback.patternKey to when {
                        feedback.positiveStreak >= STREAK_THRESHOLD -> 1
                        feedback.negativeStreak >= STREAK_THRESHOLD -> -1
                        else -> 0
                    }
                }
            }
            .catch { throwable ->
                if (throwable is IOException) {
                    emit(emptyMap())
                } else {
                    throw throwable
                }
            }
            .flowOn(dispatcher)
    }

    fun getQuickFillEnabled(): Flow<Boolean> {
        return clipboardImportDataStore.data
            .map { it.quickFillEnabled }
            .catch { throwable ->
                if (throwable is IOException) {
                    emit(DEFAULT_QUICK_FILL_ENABLED)
                } else {
                    throw throwable
                }
            }
            .flowOn(dispatcher)
    }

    fun getQuickFillHintDurationSeconds(): Flow<Int> {
        return clipboardImportDataStore.data
            .map { preference ->
                clampQuickFillHintDuration(preference.quickFillHintDurationSeconds)
            }
            .catch { throwable ->
                if (throwable is IOException) {
                    emit(DEFAULT_QUICK_FILL_HINT_DURATION_SECONDS)
                } else {
                    throw throwable
                }
            }
            .flowOn(dispatcher)
    }

    suspend fun setLastHandledClipboardFingerprint(fingerprint: String) {
        withContext(dispatcher) {
            clipboardImportDataStore.updateData {
                it.toBuilder()
                    .setLastHandledFingerprint(fingerprint)
                    .build()
            }
        }
    }

    suspend fun setQuickFillEnabled(enabled: Boolean) {
        withContext(dispatcher) {
            clipboardImportDataStore.updateData {
                it.toBuilder()
                    .setQuickFillEnabled(enabled)
                    .build()
            }
        }
    }

    suspend fun setQuickFillHintDurationSeconds(seconds: Int) {
        withContext(dispatcher) {
            clipboardImportDataStore.updateData {
                it.toBuilder()
                    .setQuickFillHintDurationSeconds(clampQuickFillHintDuration(seconds))
                    .build()
            }
        }
    }

    suspend fun recordPatternFeedback(patternKey: String, positive: Boolean) {
        if (patternKey.isBlank()) return

        withContext(dispatcher) {
            clipboardImportDataStore.updateData { current ->
                val builder = current.toBuilder()
                val list = builder.patternFeedbacksList.toMutableList()
                val targetIndex = list.indexOfFirst { it.patternKey == patternKey }
                val currentFeedback = if (targetIndex >= 0) {
                    list[targetIndex]
                } else {
                    ClipboardPatternFeedback.newBuilder()
                        .setPatternKey(patternKey)
                        .build()
                }

                val updated = currentFeedback.toBuilder().apply {
                    if (positive) {
                        positiveTotal = currentFeedback.positiveTotal + 1
                        positiveStreak = currentFeedback.positiveStreak + 1
                        negativeStreak = 0
                    } else {
                        negativeTotal = currentFeedback.negativeTotal + 1
                        negativeStreak = currentFeedback.negativeStreak + 1
                        positiveStreak = 0
                    }
                }.build()

                if (targetIndex >= 0) {
                    list[targetIndex] = updated
                } else {
                    list += updated
                }

                val trimmed = list
                    .sortedByDescending { it.positiveTotal + it.negativeTotal }
                    .take(MAX_PATTERN_FEEDBACK_SIZE)

                builder.clearPatternFeedbacks()
                builder.addAllPatternFeedbacks(trimmed)
                builder.build()
            }
        }
    }

    companion object {
        const val DEFAULT_FINGERPRINT = ""
        const val DEFAULT_QUICK_FILL_ENABLED = false
        const val QUICK_FILL_HINT_DURATION_MIN_SECONDS = 3
        const val QUICK_FILL_HINT_DURATION_MAX_SECONDS = 15
        const val DEFAULT_QUICK_FILL_HINT_DURATION_SECONDS = 5
        private const val STREAK_THRESHOLD = 3
        private const val MAX_PATTERN_FEEDBACK_SIZE = 128

        private fun clampQuickFillHintDuration(seconds: Int): Int {
            if (seconds == 0) return DEFAULT_QUICK_FILL_HINT_DURATION_SECONDS
            return seconds.coerceIn(
                QUICK_FILL_HINT_DURATION_MIN_SECONDS,
                QUICK_FILL_HINT_DURATION_MAX_SECONDS
            )
        }
    }
}
