package com.wisnu.kurniawan.composetodolist.foundation.uicomponent

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object MotionScaleProvider {
    private val _scaleFlow = MutableStateFlow(1f)
    val scaleFlow: StateFlow<Float> = _scaleFlow

    fun currentScale(): Float = _scaleFlow.value

    fun refresh(context: Context) {
        _scaleFlow.value = readScale(context)
    }

    private fun readScale(context: Context): Float {
        return runCatching {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            )
        }.getOrDefault(1f).coerceAtLeast(0f)
    }
}

@Composable
fun ObserveSystemMotionScale() {
    val context = LocalContext.current

    DisposableEffect(context) {
        MotionScaleProvider.refresh(context)
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                MotionScaleProvider.refresh(context)
            }
        }

        val uri = Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE)
        context.contentResolver.registerContentObserver(uri, false, observer)

        onDispose {
            context.contentResolver.unregisterContentObserver(observer)
        }
    }
}
