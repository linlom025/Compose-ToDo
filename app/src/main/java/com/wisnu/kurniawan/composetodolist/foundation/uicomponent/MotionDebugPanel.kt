package com.wisnu.kurniawan.composetodolist.foundation.uicomponent

import android.content.pm.ApplicationInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun PgMotionDebugPanel(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDebuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    if (!isDebuggable) return

    val scale by MotionScaleProvider.scaleFlow.collectAsStateWithLifecycle()
    var expanded by remember { mutableStateOf(false) }
    val snapshot = MotionTokens.snapshot()

    Surface(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { expanded = !expanded },
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row {
                Text(
                    text = "Motion ${MotionTokens.profile.name}",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "x${"%.2f".format(scale)}",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            if (expanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    DebugMetric("Click", snapshot.clickFeedbackMillis)
                    DebugMetric("Icon", snapshot.iconMillis)
                    DebugMetric("List", snapshot.listPlacementMillis)
                    DebugMetric("Dismiss", snapshot.dismissExitMillis)
                    DebugMetric("Search", snapshot.searchSnapMillis)
                    DebugMetric("Sheet", snapshot.bottomSheetMillis)
                }
            } else {
                Text(
                    text = "Tap to expand",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.defaultMinSize(minHeight = 18.dp),
                )
            }
        }
    }
}

@Composable
private fun DebugMetric(label: String, valueMs: Int) {
    Text(
        text = "$label ${valueMs}ms",
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.padding(PaddingValues(vertical = 1.dp))
    )
}
