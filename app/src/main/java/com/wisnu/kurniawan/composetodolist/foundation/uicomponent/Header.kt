package com.wisnu.kurniawan.composetodolist.foundation.uicomponent

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wisnu.kurniawan.composetodolist.foundation.theme.Space12

@Composable
fun PgModalBackHeader(
    text: String,
    onClickBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .padding(start = Space12)
                .weight(0.2F)
        ) {
            PgModalBackButton(
                onClick = onClickBack
            )
        }

        PgModalTitle(
            text = text,
            modifier = Modifier.weight(0.6F)
        )

        Spacer(
            Modifier
                .size(0.dp)
                .weight(0.2F)
        )
    }
}
