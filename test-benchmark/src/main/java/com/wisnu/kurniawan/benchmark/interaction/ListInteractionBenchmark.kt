package com.wisnu.kurniawan.benchmark.interaction

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class ListInteractionBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun scrollAndSwipeDelete() = benchmarkRule.measureRepeated(
        packageName = "com.lltodo.app",
        metrics = listOf(FrameTimingMetric()),
        compilationMode = CompilationMode.Partial(
            baselineProfileMode = BaselineProfileMode.Require,
            warmupIterations = 1
        ),
        startupMode = StartupMode.WARM,
        iterations = 8,
        setupBlock = {
            pressHome()
            startActivityAndWait()
            device.waitForIdle()
        }
    ) {
        repeat(3) {
            device.swipe(
                device.displayWidth / 2,
                (device.displayHeight * 0.8).toInt(),
                device.displayWidth / 2,
                (device.displayHeight * 0.3).toInt(),
                18
            )
            device.waitForIdle()
        }

        val swipeY = (device.displayHeight * 0.62f).toInt()
        device.swipe(
            (device.displayWidth * 0.85f).toInt(),
            swipeY,
            (device.displayWidth * 0.2f).toInt(),
            swipeY,
            16
        )
        device.waitForIdle()
    }
}
