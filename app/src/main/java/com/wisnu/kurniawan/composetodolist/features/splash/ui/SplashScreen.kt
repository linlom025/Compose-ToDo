package com.wisnu.kurniawan.composetodolist.features.splash.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.wisnu.kurniawan.composetodolist.foundation.uicomponent.PgPageLayout
import com.wisnu.kurniawan.composetodolist.foundation.viewmodel.HandleEffect
import com.wisnu.kurniawan.composetodolist.runtime.navigation.HomeFlow
import com.wisnu.kurniawan.composetodolist.runtime.navigation.MainFlow

@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: SplashViewModel
) {
    PgPageLayout(verticalArrangement = Arrangement.Center) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "ll-todo", style = MaterialTheme.typography.titleLarge)
        }
    }

    HandleEffect(viewModel) {
        when (it) {
            SplashEffect.NavigateToDashboard -> {
                navController.navigate(HomeFlow.Root.route) {
                    popUpTo(MainFlow.Root.route) {
                        inclusive = true
                    }
                }
            }

            SplashEffect.NavigateToLogin -> {
                navController.navigate(HomeFlow.Root.route) {
                    popUpTo(MainFlow.Root.route) {
                        inclusive = true
                    }
                }
            }
        }
    }
}
