package com.wisnu.kurniawan.composetodolist.foundation.uicomponent

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt

enum class MotionProfile {
    FAST,
    BALANCED
}

data class MotionDebugSnapshot(
    val profile: MotionProfile,
    val systemScale: Float,
    val clickFeedbackMillis: Int,
    val iconMillis: Int,
    val listPlacementMillis: Int,
    val dismissRevealMillis: Int,
    val dismissExitMillis: Int,
    val searchSnapMillis: Int,
    val bottomSheetMillis: Int,
    val dismissCommitDelayMillis: Long,
    val undoWindowMillis: Long,
)

object MotionTokens {
    @JvmField
    var profile: MotionProfile = MotionProfile.FAST

    private fun baseDuration(fast: Int, balanced: Int): Int {
        return if (profile == MotionProfile.FAST) fast else balanced
    }

    private fun scaledDuration(baseDuration: Int): Int {
        val scale = MotionScaleProvider.currentScale()
        if (scale <= 0f) return 1
        return (baseDuration * scale).roundToInt().coerceAtLeast(1)
    }

    fun clickFeedbackMillis(): Int = scaledDuration(baseDuration(fast = 70, balanced = 110))

    fun dismissRevealMillis(): Int = scaledDuration(baseDuration(fast = 80, balanced = 130))

    fun dismissExitMillis(): Int = scaledDuration(baseDuration(fast = 110, balanced = 150))

    fun iconMillis(): Int = scaledDuration(baseDuration(fast = 70, balanced = 100))

    fun searchSnapMillis(): Int = scaledDuration(baseDuration(fast = 90, balanced = 130))

    fun listPlacementMillis(): Int = scaledDuration(baseDuration(fast = 100, balanced = 140))

    fun bottomSheetMillis(): Int = scaledDuration(baseDuration(fast = 120, balanced = 170))

    fun dismissCommitDelayMillis(): Long = scaledDuration(baseDuration(fast = 80, balanced = 120)).toLong()

    fun undoWindowMillis(): Long = scaledDuration(baseDuration(fast = 1200, balanced = 1800)).toLong()

    fun dismissRevealSpec(): FiniteAnimationSpec<Float> = tween(
        durationMillis = dismissRevealMillis(),
        easing = LinearOutSlowInEasing
    )

    fun dismissEnterSpec(): FiniteAnimationSpec<IntSize> = tween(
        durationMillis = clickFeedbackMillis(),
        easing = LinearOutSlowInEasing
    )

    fun dismissExitSpec(): FiniteAnimationSpec<IntSize> = tween(
        durationMillis = dismissExitMillis(),
        easing = FastOutLinearInEasing
    )

    fun iconScaleSpec(): FiniteAnimationSpec<Float> = tween(
        durationMillis = iconMillis(),
        easing = LinearOutSlowInEasing
    )

    fun searchSnapSpec(): FiniteAnimationSpec<Float> = tween(
        durationMillis = searchSnapMillis(),
        easing = FastOutLinearInEasing
    )

    fun listPlacementSpec(): FiniteAnimationSpec<IntOffset> = tween(
        durationMillis = listPlacementMillis(),
        easing = FastOutLinearInEasing
    )

    fun bottomSheetSpec(): AnimationSpec<Float> = tween(
        durationMillis = bottomSheetMillis(),
        easing = FastOutLinearInEasing
    )

    fun snapshot(): MotionDebugSnapshot {
        return MotionDebugSnapshot(
            profile = profile,
            systemScale = MotionScaleProvider.currentScale(),
            clickFeedbackMillis = clickFeedbackMillis(),
            iconMillis = iconMillis(),
            listPlacementMillis = listPlacementMillis(),
            dismissRevealMillis = dismissRevealMillis(),
            dismissExitMillis = dismissExitMillis(),
            searchSnapMillis = searchSnapMillis(),
            bottomSheetMillis = bottomSheetMillis(),
            dismissCommitDelayMillis = dismissCommitDelayMillis(),
            undoWindowMillis = undoWindowMillis(),
        )
    }
}
