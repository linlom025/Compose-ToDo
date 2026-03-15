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
    }
}
