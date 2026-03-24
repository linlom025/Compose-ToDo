package com.wisnu.kurniawan.composetodolist.runtime.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.wisnu.kurniawan.composetodolist.features.calendar.ui.CalendarScreen
import com.wisnu.kurniawan.composetodolist.features.calendar.ui.CalendarViewModel
import com.wisnu.kurniawan.composetodolist.features.dashboard.ui.DashboardScreen
import com.wisnu.kurniawan.composetodolist.features.dashboard.ui.DashboardViewModel
import com.wisnu.kurniawan.composetodolist.features.setting.ui.SettingScreen
import com.wisnu.kurniawan.composetodolist.features.setting.ui.SettingViewModel
import com.wisnu.kurniawan.composetodolist.features.todo.main.ui.ToDoMainViewModel

fun NavGraphBuilder.HomeNavHost(
    navController: NavHostController
) {
    navigation(startDestination = HomeFlow.DashboardScreen.route, route = HomeFlow.Root.route) {
        composable(HomeFlow.DashboardScreen.route) {
            val viewModel = hiltViewModel<DashboardViewModel>()
            val toDoMainViewModel = hiltViewModel<ToDoMainViewModel>()
            DashboardScreen(
                viewModel = viewModel,
                toDoMainViewModel = toDoMainViewModel,
                onCalendarClick = { navController.navigate(HomeFlow.CalendarScreen.route) },
                onSettingClick = { navController.navigate(HomeFlow.SettingsScreen.route) },
                onTaskItemClick = { taskId, listId -> navController.navigate(StepFlow.Root.route(taskId, listId)) }
            )
        }

        composable(HomeFlow.CalendarScreen.route) {
            val viewModel = hiltViewModel<CalendarViewModel>()
            CalendarScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onTaskItemClick = { taskId, listId ->
                    navController.navigate(StepFlow.Root.route(taskId, listId))
                }
            )
        }

        composable(HomeFlow.SettingsScreen.route) {
            val viewModel = hiltViewModel<SettingViewModel>()
            SettingScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

fun NavGraphBuilder.HomeTabletNavHost(
    navController: NavHostController,
    navControllerLeft: NavHostController,
    navControllerRight: NavHostController
) {
    navigation(startDestination = HomeFlow.DashboardScreen.route, route = HomeFlow.Root.route) {
        composable(HomeFlow.DashboardScreen.route) {
            val viewModel = hiltViewModel<DashboardViewModel>()
            val toDoMainViewModel = hiltViewModel<ToDoMainViewModel>()

            DashboardScreen(
                viewModel = viewModel,
                toDoMainViewModel = toDoMainViewModel,
                onCalendarClick = { navControllerLeft.navigate(HomeFlow.CalendarScreen.route) },
                onSettingClick = { navControllerLeft.navigate(HomeFlow.SettingsScreen.route) },
                onTaskItemClick = { taskId, listId ->
                    navControllerRight.navigate(StepFlow.Root.route(taskId, listId)) {
                        popUpTo(MainFlow.RootEmpty.route)
                    }
                }
            )
        }

        composable(HomeFlow.CalendarScreen.route) {
            val viewModel = hiltViewModel<CalendarViewModel>()
            CalendarScreen(
                viewModel = viewModel,
                onBackClick = { navControllerLeft.popBackStack() },
                onTaskItemClick = { taskId, listId ->
                    navControllerRight.navigate(StepFlow.Root.route(taskId, listId)) {
                        popUpTo(MainFlow.RootEmpty.route)
                    }
                }
            )
        }

        composable(HomeFlow.SettingsScreen.route) {
            val viewModel = hiltViewModel<SettingViewModel>()
            SettingScreen(
                viewModel = viewModel,
                onBackClick = { navControllerLeft.popBackStack() }
            )
        }
    }
}
