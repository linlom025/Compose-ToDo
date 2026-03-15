package com.wisnu.kurniawan.composetodolist.foundation.uiextension

import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.navigation.BottomSheetNavigator
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.MotionTokens

@Composable
fun rememberBottomSheetNavigator(
): BottomSheetNavigator {
    val sheetState = rememberModalBottomSheetState(
        ModalBottomSheetValue.Hidden,
        animationSpec = MotionTokens.bottomSheetSpec(),
        skipHalfExpanded = true
    )
    return remember(sheetState) {
        BottomSheetNavigator(sheetState = sheetState)
    }
}
