package com.wisnu.kurniawan.composetodolist.features.host.ui

import app.cash.turbine.test
import com.wisnu.kurniawan.composetodolist.BaseViewModelTest
import com.wisnu.kurniawan.composetodolist.features.host.data.IHostEnvironment
import com.wisnu.kurniawan.composetodolist.model.Theme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import kotlin.time.ExperimentalTime

@ExperimentalTime
@ExperimentalCoroutinesApi
class HostViewModelTest : BaseViewModelTest() {

    @Test
    fun init() = runTest {
        val environment = object : IHostEnvironment {
            override fun getTheme(): Flow<Theme> {
                return flow { emit(Theme.SUNRISE) }
            }

            override fun getFontScalePercent(): Flow<Int> {
                return flow { emit(130) }
            }
        }

        val viewModel = HostViewModel(environment)

        viewModel.state.test {
            val first = awaitItem()
            val result = if (
                first.theme == Theme.SUNRISE &&
                first.fontScalePercent == 130
            ) {
                first
            } else {
                awaitItem()
            }

            Assert.assertEquals(Theme.SUNRISE, result.theme)
            Assert.assertEquals(130, result.fontScalePercent)

            cancelAndConsumeRemainingEvents()
        }
    }

}
