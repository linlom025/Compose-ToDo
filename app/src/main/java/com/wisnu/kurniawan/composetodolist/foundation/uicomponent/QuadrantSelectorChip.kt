package com.wisnu.kurniawan.composetodolist.foundation.uicomponent

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PgQuadrantSelectorChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val darkSurface = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val selectedContainer = if (darkSurface) Color(0xFF27453D) else Color(0xFFDDF5E9)
    val selectedBorder = if (darkSurface) Color(0xFF6EB39A) else Color(0xFF71B79E)
    val selectedText = if (darkSurface) Color(0xFFCFF6E5) else Color(0xFF1E6A4B)

    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        label = {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
        },
        leadingIcon = if (selected) {
            {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null
                )
            }
        } else {
            null
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color.Transparent,
            labelColor = MaterialTheme.colorScheme.onSurface,
            selectedContainerColor = selectedContainer,
            selectedLabelColor = selectedText,
            selectedLeadingIconColor = selectedText
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
            selectedBorderColor = selectedBorder,
            borderWidth = 1.dp,
            selectedBorderWidth = 1.4.dp
        )
    )
}
