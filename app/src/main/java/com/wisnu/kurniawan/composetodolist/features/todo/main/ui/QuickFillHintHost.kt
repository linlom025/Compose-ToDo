package com.wisnu.kurniawan.composetodolist.features.todo.main.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wisnu.kurniawan.composetodolist.R
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgButton
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgSecondaryButton
import kotlinx.coroutines.delay

private const val CLIPBOARD_HINT_AUTO_DISMISS_MILLIS = 15_000L

@Composable
fun QuickFillHintHost(
    title: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (title.isBlank()) return

    var canDismissByBackground by remember(title) { mutableStateOf(false) }

    LaunchedEffect(title) {
        canDismissByBackground = false
        delay(500L)
        canDismissByBackground = true
    }

    LaunchedEffect(title) {
        delay(CLIPBOARD_HINT_AUTO_DISMISS_MILLIS)
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(title) {
                detectTapGestures(
                    onTap = {
                        if (canDismissByBackground) {
                            onDismiss()
                        }
                    }
                )
            }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) { detectTapGestures(onTap = {}) },
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.todo_clipboard_soft_import_message, title),
                    style = MaterialTheme.typography.bodyMedium
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PgSecondaryButton(
                        modifier = Modifier.weight(1f),
                        onClick = onDismiss
                    ) {
                        Text(
                            text = stringResource(R.string.todo_clipboard_soft_import_ignore),
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    }

                    PgButton(
                        modifier = Modifier.weight(1f),
                        onClick = onConfirm
                    ) {
                        Text(text = stringResource(R.string.todo_clipboard_soft_import_action))
                    }
                }
            }
        }
    }
}
