package com.wisnu.kurniawan.composetodolist.features.splash.ui

import app.cash.turbine.test
import com.wisnu.kurniawan.composetodolist.BaseViewModelTest
import com.wisnu.kurniawan.composetodolist.features.splash.data.ISplashEnvironment
import com.wisnu.kurniawan.composetodolist.foundation.security.MigrationRequirement
import com.wisnu.kurniawan.composetodolist.model.Credential
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.junit.Assert
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.ExperimentalTime

@ExperimentalTime
@ExperimentalCoroutinesApi
class SplashViewModelTest : BaseViewModelTest() {

    @Test
    fun retryMigration_whenAuthGateDisabled_navigateToHome() = runTest {
        val splashViewModel = SplashViewModel(
            buildFakeSplashEnvironment(
                credential = Credential("qwe-123"),
                authGateEnabled = false
            )
        )

        splashViewModel.effect.test {
            splashViewModel.dispatch(SplashAction.RetryMigration)
            Assert.assertEquals(SplashEffect.NavigateToHome, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun retryMigration_whenAuthGateEnabled_navigateToAuthGate() = runTest {
        val splashViewModel = SplashViewModel(
            buildFakeSplashEnvironment(
                credential = Credential("qwe-123"),
                authGateEnabled = true
            )
        )

        splashViewModel.effect.test {
            splashViewModel.dispatch(SplashAction.RetryMigration)
            Assert.assertEquals(SplashEffect.NavigateToAuthGate, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    private fun buildFakeSplashEnvironment(
        credential: Credential,
        authGateEnabled: Boolean
    ): ISplashEnvironment {
        return object : ISplashEnvironment {
            override fun getCredential(): Flow<Credential> {
                return flow { emit(credential) }
            }

            override suspend fun getAuthGateEnabled(): Boolean {
                return authGateEnabled
            }

            override suspend fun getMigrationRequirement(): MigrationRequirement {
                return MigrationRequirement.NO_MIGRATION
            }

            override suspend fun migrateStorageIfNeeded() = Unit
        }
    }

}
