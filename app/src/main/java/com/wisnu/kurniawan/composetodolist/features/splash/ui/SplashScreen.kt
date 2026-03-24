package com.wisnu.kurniawan.composetodolist.features.splash.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgButton
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgPageLayout
import com.wisnu.kurniawan.composetodolist.foundation.viewmodel.HandleEffect
import com.wisnu.kurniawan.composetodolist.runtime.navigation.HomeFlow
import com.wisnu.kurniawan.composetodolist.runtime.navigation.MainFlow

@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: SplashViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (state.migrationStatus == MigrationStatus.Migrating || state.migrationStatus == MigrationStatus.Failed) {
        PgPageLayout(verticalArrangement = Arrangement.Center) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "ll-todo", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.size(8.dp))

                when (state.migrationStatus) {
                    MigrationStatus.Failed -> {
                        Text(
                            text = "数据安全升级失败，请重试",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (state.errorMessage.isNotBlank()) {
                            Spacer(modifier = Modifier.size(4.dp))
                            Text(
                                text = state.errorMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.size(12.dp))
                        PgButton(onClick = { viewModel.dispatch(SplashAction.RetryMigration) }) {
                            Text(text = "重试")
                        }
                    }

                    MigrationStatus.Migrating -> {
                        Text(text = "正在进行数据安全升级", style = MaterialTheme.typography.bodyMedium)
                    }

                    else -> Unit
                }
            }
        }
    }

    HandleEffect(viewModel) {
        when (it) {
            SplashEffect.NavigateToAuthGate -> {
                navController.navigate(MainFlow.AuthGate.route) {
                    popUpTo(MainFlow.Root.route) {
                        inclusive = true
                    }
                }
            }

            SplashEffect.NavigateToHome -> {
                navController.navigate(HomeFlow.Root.route) {
                    popUpTo(MainFlow.Root.route) {
                        inclusive = true
                    }
                }
            }
        }
    }
}
