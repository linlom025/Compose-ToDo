package com.wisnu.kurniawan.composetodolist.features.splash.ui

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.wisnu.foundation.coreviewmodel.StatefulViewModel
import com.wisnu.kurniawan.composetodolist.features.splash.data.ISplashEnvironment
import com.wisnu.kurniawan.composetodolist.foundation.security.AppAuthGate
import com.wisnu.kurniawan.composetodolist.foundation.security.MigrationRequirement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    splashEnvironment: ISplashEnvironment
) : StatefulViewModel<SplashState, SplashEffect, SplashAction, ISplashEnvironment>(
    SplashState(),
    splashEnvironment
) {

    init {
        dispatch(SplashAction.AppLaunch)
    }

    override fun dispatch(action: SplashAction) {
        when (action) {
            SplashAction.AppLaunch,
            SplashAction.RetryMigration -> migrateAndNavigate()
        }
    }

    private fun migrateAndNavigate() {
        if (state.value.migrationStatus == MigrationStatus.Migrating) return

        viewModelScope.launch {
            val requirement = runCatching {
                environment.getMigrationRequirement()
            }.getOrElse { throwable ->
                Log.e("StorageMigration", "迁移状态检测失败。", throwable)
                setState {
                    copy(
                        migrationStatus = MigrationStatus.Failed,
                        errorMessage = throwable.message ?: "升级失败，请重试"
                    )
                }
                return@launch
            }

            when (requirement) {
                MigrationRequirement.NO_MIGRATION -> {
                    setState {
                        copy(
                            migrationStatus = MigrationStatus.Done,
                            errorMessage = ""
                        )
                    }
                    navigateAfterMigration()
                }

                MigrationRequirement.NEEDS_MIGRATION -> {
                    setState {
                        copy(
                            migrationStatus = MigrationStatus.Migrating,
                            errorMessage = ""
                        )
                    }

                    runCatching {
                        environment.migrateStorageIfNeeded()
                    }.onSuccess {
                        setState {
                            copy(
                                migrationStatus = MigrationStatus.Done,
                                errorMessage = ""
                            )
                        }
                        navigateAfterMigration()
                    }.onFailure { throwable ->
                        Log.e("StorageMigration", "存储加密迁移失败，已保留原始数据。", throwable)
                        setState {
                            copy(
                                migrationStatus = MigrationStatus.Failed,
                                errorMessage = throwable.message ?: "升级失败，请重试"
                            )
                        }
                    }
                }

                MigrationRequirement.UNRECOVERABLE -> {
                    setState {
                        copy(
                            migrationStatus = MigrationStatus.Failed,
                            errorMessage = "检测到数据状态异常，请重试升级。"
                        )
                    }
                }
            }
        }
    }

    private suspend fun navigateAfterMigration() {
        val authGateEnabled = environment.getAuthGateEnabled()
        AppAuthGate.setGateEnabled(authGateEnabled)
        if (authGateEnabled) {
            setEffect(SplashEffect.NavigateToAuthGate)
        } else {
            setEffect(SplashEffect.NavigateToHome)
        }
    }
}
