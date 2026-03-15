package com.wisnu.kurniawan.composetodolist.foundation.uicomponent

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

private const val dismissFraction = 0.4f
private const val iconShownFraction = 0.07f
private const val defaultUndoMessage = "\u5df2\u5220\u9664"
private const val defaultUndoActionLabel = "\u64a4\u9500"

object ContentVisibility {
    const val visible: Float = 1f
    const val hidden: Float = 0f
}

@Composable
fun SwipeDismiss(
    modifier: Modifier = Modifier,
    backgroundModifier: Modifier = Modifier,
    backgroundSecondaryModifier: Modifier = Modifier,
    content: @Composable (isDismissed: Boolean) -> Unit,
    onDismiss: () -> Unit,
    onRequestDismiss: (() -> Boolean)? = null,
    undoEnabled: Boolean = true,
) {
    SwipeDismiss(
        modifier = modifier,
        background = { _, fraction ->
            val wouldCompleteOnRelease = fraction.absoluteValue >= dismissFraction
            val iconVisible = fraction.absoluteValue >= iconShownFraction
            val haptic = LocalHapticFeedback.current

            var hapticDispatched by remember { mutableStateOf(false) }

            val backgroundProgress by animateFloatAsState(
                targetValue = when {
                    wouldCompleteOnRelease -> ContentVisibility.visible
                    iconVisible -> 0.6f
                    else -> ContentVisibility.hidden
                },
                animationSpec = MotionTokens.dismissRevealSpec(),
                label = "dismiss-background-progress"
            )

            val iconScale by animateFloatAsState(
                targetValue = when {
                    !iconVisible -> 0.85f
                    wouldCompleteOnRelease -> 1.08f
                    else -> 1f
                },
                animationSpec = MotionTokens.iconScaleSpec(),
                label = "dismiss-icon-scale"
            )

            val iconAlpha by animateFloatAsState(
                targetValue = if (iconVisible) 1f else 0.45f,
                animationSpec = MotionTokens.iconScaleSpec(),
                label = "dismiss-icon-alpha"
            )

            LaunchedEffect(wouldCompleteOnRelease) {
                if (wouldCompleteOnRelease && !hapticDispatched) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    hapticDispatched = true
                } else if (!wouldCompleteOnRelease) {
                    hapticDispatched = false
                }
            }

            Box(
                modifier = backgroundModifier.fillMaxSize()
            ) {
                Box(
                    modifier = backgroundSecondaryModifier
                        .fillMaxSize()
                        .background(
                            color = lerp(
                                start = MaterialTheme.colorScheme.surfaceVariant,
                                stop = MaterialTheme.colorScheme.errorContainer,
                                fraction = backgroundProgress
                            )
                        )
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(horizontal = 16.dp)
                ) {
                    PgIcon(
                        imageVector = Icons.Rounded.Delete,
                        modifier = Modifier
                            .size(28.dp)
                            .graphicsLayer {
                                scaleX = iconScale
                                scaleY = iconScale
                            },
                        tint = lerp(
                            start = MaterialTheme.colorScheme.onSurfaceVariant,
                            stop = MaterialTheme.colorScheme.onErrorContainer,
                            fraction = backgroundProgress
                        ).copy(alpha = iconAlpha)
                    )
                }
            }
        },
        content = content,
        onDismiss = onDismiss,
        onRequestDismiss = onRequestDismiss,
        undoEnabled = undoEnabled
    )
}

@Composable
fun SwipeDismiss(
    modifier: Modifier = Modifier,
    background: @Composable (isDismissed: Boolean, fraction: Float) -> Unit,
    content: @Composable (isDismissed: Boolean) -> Unit,
    directions: Set<SwipeToDismissBoxValue> = setOf(SwipeToDismissBoxValue.EndToStart),
    enter: EnterTransition = expandVertically(
        animationSpec = MotionTokens.dismissEnterSpec()
    ),
    exit: ExitTransition = shrinkVertically(
        animationSpec = MotionTokens.dismissExitSpec(),
    ),
    onDismiss: () -> Unit,
    onRequestDismiss: (() -> Boolean)? = null,
    undoEnabled: Boolean = true,
    undoMessage: String = defaultUndoMessage,
    undoActionLabel: String = defaultUndoActionLabel
) {
    var isDismissed by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = LocalPgSnackbarHostState.current
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    val currentUndoMessage by rememberUpdatedState(undoMessage)
    val currentUndoActionLabel by rememberUpdatedState(undoActionLabel)

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                val approved = onRequestDismiss?.invoke() ?: true
                if (!approved) {
                    return@rememberSwipeToDismissBoxState false
                }
                isDismissed = true
                true
            } else {
                false
            }
        }
    )

    LaunchedEffect(isDismissed) {
        if (!isDismissed) return@LaunchedEffect

        delay(MotionTokens.dismissCommitDelayMillis())

        val hostState = snackbarHostState
        if (!undoEnabled || hostState == null) {
            currentOnDismiss()
            return@LaunchedEffect
        }

        val autoDismissJob = launch {
            delay(MotionTokens.undoWindowMillis())
            hostState.currentSnackbarData?.dismiss()
        }

        val snackbarResult = hostState.showSnackbar(
            message = currentUndoMessage,
            actionLabel = currentUndoActionLabel,
            duration = SnackbarDuration.Indefinite,
            withDismissAction = false
        )
        autoDismissJob.cancel()

        if (snackbarResult == SnackbarResult.ActionPerformed) {
            isDismissed = false
            dismissState.reset()
        } else {
            currentOnDismiss()
        }
    }

    AnimatedVisibility(
        modifier = modifier,
        visible = !isDismissed,
        enter = enter,
        exit = exit
    ) {
        SwipeToDismissBox(
            modifier = modifier,
            state = dismissState,
            enableDismissFromStartToEnd = false,
            enableDismissFromEndToStart = true,
            backgroundContent = {
                if (dismissState.dismissDirection in directions) {
                    background(isDismissed, dismissState.progress)
                }
            },
            content = { content(isDismissed) },
        )
    }
}
