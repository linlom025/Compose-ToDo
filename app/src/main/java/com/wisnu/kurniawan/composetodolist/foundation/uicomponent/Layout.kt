package com.wisnu.kurniawan.composetodolist.foundation.uicomponent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wisnu.kurniawan.composetodolist.foundation.theme.Space12
import com.wisnu.kurniawan.composetodolist.foundation.theme.Space16
import com.wisnu.kurniawan.composetodolist.foundation.theme.Space8

@Composable
fun PgPageLayout(
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = Space12,
    topContentPadding: Dp = Space8,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = horizontalPadding)
            .padding(top = topContentPadding),
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        content = content
    )
}

@Composable
fun PgModalLayout(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: LazyListScope.() -> Unit
) {
    PgModalLazyColumn(modifier) {
        item {
            Spacer(Modifier.height(16.dp))
            title()
            Spacer(Modifier.height(14.dp))
        }

        content()

        item {
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
fun PgModalLazyColumn(
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    content: LazyListScope.() -> Unit
) {
    Box(
        modifier = Modifier
        .background(
                color = MaterialTheme.colorScheme.surface,
                shape = shape
            )
            .padding(horizontal = Space8, vertical = Space8)
    ) {
        LazyColumn(
            modifier = modifier
                .padding(horizontal = Space8)
                .navigationBarsPadding()
                .imePadding(),
            content = content
        )
    }
}

@Composable
fun PgModalRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    content: @Composable RowScope.() -> Unit
) {
    Box(
        modifier = Modifier.background(
            color = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = modifier
                .navigationBarsPadding()
                .imePadding(),
            horizontalArrangement = horizontalArrangement,
            verticalAlignment = verticalAlignment,
            content = content
        )
    }
}
