package com.wisnu.kurniawan.composetodolist.foundation.uicomponent

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

val LocalPgSnackbarHostState = staticCompositionLocalOf<SnackbarHostState?> { null }

@Composable
fun PgSnackbarHostContainer(
    content: @Composable () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    CompositionLocalProvider(LocalPgSnackbarHostState provides snackbarHostState) {
        Box(modifier = Modifier.fillMaxSize()) {
            content()
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .imePadding()
            )
        }
    }
}
