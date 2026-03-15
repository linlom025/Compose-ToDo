package com.wisnu.kurniawan.composetodolist.foundation.uicomponent

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.wisnu.kurniawan.composetodolist.foundation.theme.AlphaDisabled
import com.wisnu.kurniawan.composetodolist.foundation.theme.AlphaHigh
import com.wisnu.kurniawan.composetodolist.foundation.theme.AlphaMedium
import com.wisnu.kurniawan.composetodolist.foundation.theme.CardMinHeight
import com.wisnu.kurniawan.composetodolist.foundation.theme.Space2
import com.wisnu.kurniawan.composetodolist.foundation.theme.Space12
import com.wisnu.kurniawan.composetodolist.foundation.theme.Space16
import com.wisnu.kurniawan.composetodolist.foundation.theme.Space4
import com.wisnu.kurniawan.composetodolist.foundation.theme.Space8

@Composable
fun PgModalCell(
    onClick: () -> Unit,
    text: String,
    color: Color = MaterialTheme.colorScheme.surfaceVariant,
    textColor: Color = Color.Unspecified,
    enabled: Boolean = true,
    leftIcon: @Composable (() -> Unit)? = null,
    rightIcon: @Composable (() -> Unit)? = null
) {
    val colorAlpha = if (enabled) {
        AlphaHigh
    } else {
        AlphaDisabled
    }
    val onClickState = if (enabled) {
        onClick
    } else {
        {}
    }
    val indication = if (enabled) {
        LocalIndication.current
    } else {
        null
    }

    val shape = MaterialTheme.shapes.medium
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Space12)
            .height(CardMinHeight)
            .clip(shape)
            .clickable(
                onClick = onClickState,
                indication = indication,
                interactionSource = remember { MutableInteractionSource() }
            ),
        shape = shape,
        color = color.copy(alpha = colorAlpha),
        tonalElevation = 1.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Space12),
        ) {
            if (leftIcon != null) {
                Spacer(Modifier.width(Space8))
                leftIcon()
            } else {
                Spacer(Modifier.width(Space16))
            }

            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
                color = textColor
            )

            if (rightIcon != null) {
                rightIcon()
                Spacer(Modifier.size(Space12))
            }
        }
    }
}

@Composable
fun PgToDoItemCell(
    modifier: Modifier = Modifier,
    name: String,
    info: AnnotatedString? = null,
    color: Color,
    contentPaddingValues: PaddingValues,
    leftIcon: ImageVector,
    textDecoration: TextDecoration?,
    onClick: () -> Unit,
    onSwipeToDelete: () -> Unit,
    onStatusClick: () -> Unit,
    undoEnabled: Boolean = true,
    onRequestDelete: (() -> Unit)? = null,
) {
    val isCompleted = textDecoration == TextDecoration.LineThrough

    SwipeDismiss(
        modifier = modifier,
        backgroundModifier = Modifier
            .background(MaterialTheme.colorScheme.secondary),
        undoEnabled = undoEnabled,
        onRequestDismiss = if (onRequestDelete == null) {
            null
        } else {
            {
                onRequestDelete()
                false
            }
        },
        content = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Space2, vertical = 2.dp)
                    .clickable(onClick = onClick),
                shape = MaterialTheme.shapes.medium,
                color = if (isCompleted) {
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
                } else {
                    MaterialTheme.colorScheme.surface
                },
                tonalElevation = if (isCompleted) 0.dp else 2.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Space8),
                    modifier = Modifier.padding(contentPaddingValues)
                ) {
                    PgIconButton(
                        modifier = Modifier.size(28.dp),
                        onClick = onStatusClick,
                        variant = PgIconButtonVariant.Ghost,
                        enforceMinSize = false
                    ) {
                        PgIcon(
                            imageVector = leftIcon,
                            tint = color
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleSmall.copy(
                                textDecoration = textDecoration,
                                fontWeight = if (isCompleted) FontWeight.Normal else FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurface.copy(
                                alpha = if (isCompleted) AlphaMedium else AlphaHigh
                            )
                        )

                        if (info != null) {
                            Spacer(Modifier.height(Space4))
                            Text(
                                text = info,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = AlphaMedium)
                            )
                        }
                    }
                }
            }
        },
        onDismiss = { onSwipeToDelete() }
    )
}
