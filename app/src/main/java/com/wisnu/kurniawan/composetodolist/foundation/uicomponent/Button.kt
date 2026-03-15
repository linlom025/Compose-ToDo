package com.wisnu.kurniawan.composetodolist.foundation.uicomponent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.wisnu.kurniawan.composetodolist.foundation.theme.AlphaDisabled
import com.wisnu.kurniawan.composetodolist.foundation.theme.ButtonHeight
import com.wisnu.kurniawan.composetodolist.foundation.theme.CompactIconButtonSize
import com.wisnu.kurniawan.composetodolist.foundation.theme.IconButtonMinSize
import com.wisnu.kurniawan.composetodolist.foundation.theme.MediumIconButtonSize

enum class PgIconButtonVariant {
    FilledSoft,
    Ghost
}

enum class PgIconButtonSize {
    Small,
    Medium
}

@Composable
fun PgModalBackButton(
    onClick: () -> Unit,
    imageVector: ImageVector = Icons.Rounded.ChevronLeft
) {
    PgIconButton(
        onClick = onClick,
        size = PgIconButtonSize.Small
    ) {
        PgIcon(
            imageVector = imageVector,
        )
    }
}

@Composable
fun PgIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = MaterialTheme.colorScheme.secondary,
    variant: PgIconButtonVariant = PgIconButtonVariant.FilledSoft,
    size: PgIconButtonSize = PgIconButtonSize.Medium,
    enforceMinSize: Boolean = false,
    content: @Composable () -> Unit
) {
    val shape = CircleShape
    val buttonSize = when (size) {
        PgIconButtonSize.Small -> CompactIconButtonSize
        PgIconButtonSize.Medium -> MediumIconButtonSize
    }
    val containerColor = if (variant == PgIconButtonVariant.Ghost) {
        Color.Transparent
    } else {
        color
    }
    val interactionSource = remember { MutableInteractionSource() }
    val visualModifier = Modifier
        .size(buttonSize)
        .clip(shape)
        .background(
            color = containerColor,
            shape = shape
        )

    if (enforceMinSize) {
        Box(
            modifier = modifier
                .sizeIn(minWidth = IconButtonMinSize, minHeight = IconButtonMinSize)
                .clickable(
                    enabled = enabled,
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = visualModifier,
                contentAlignment = Alignment.Center
            ) {
                content()
            }
        }
    } else {
        Box(
            modifier = visualModifier
                .then(modifier)
                .clickable(
                    enabled = enabled,
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
fun PgButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        modifier = modifier.height(ButtonHeight),
        enabled = enabled,
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        content = content,
        colors = ButtonDefaults.buttonColors(
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = AlphaDisabled)
        ),
    )
}

@Composable
fun PgSecondaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    OutlinedButton(
        modifier = modifier.height(ButtonHeight),
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.secondary
        ),
        content = content
    )
}
