package com.wisnu.kurniawan.benchmark.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import org.junit.Rule
import org.junit.Test

/**
 * Generates a baseline profile which can be copied to `app/src/main/baseline-prof.txt`.
 */
class BaselineProfileGenerator {
    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun startup() =
        baselineProfileRule.collect(
            packageName = "com.lltodo.app"
        ) {
            pressHome()
            // This block defines the app's critical user journey. Here we are interested in
            // optimizing for app startup. But you can also navigate and scroll
            // through your most important UI.
            startActivityAndWait()
            device.waitForIdle()

            repeat(2) {
                device.swipe(
                    device.displayWidth / 2,
                    (device.displayHeight * 0.8).toInt(),
                    device.displayWidth / 2,
                    (device.displayHeight * 0.3).toInt(),
                    18
                )
                device.waitForIdle()
            }
        }
}
