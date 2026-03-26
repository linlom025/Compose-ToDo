package com.wisnu.kurniawan.composetodolist.runtime.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.navigation.BottomSheetNavigator
import androidx.compose.material.navigation.ModalBottomSheetLayout
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wisnu.kurniawan.composetodolist.features.auth.ui.AuthGateScreen
import com.wisnu.kurniawan.composetodolist.features.auth.ui.AuthGateViewModel
import com.wisnu.kurniawan.composetodolist.features.splash.ui.SplashScreen
import com.wisnu.kurniawan.composetodolist.features.splash.ui.SplashViewModel
import com.wisnu.kurniawan.composetodolist.features.todo.main.ui.QuickFillHintHost
import com.wisnu.kurniawan.composetodolist.features.todo.main.ui.ToDoMainAction
import com.wisnu.kurniawan.composetodolist.features.todo.main.ui.ToDoMainViewModel
import com.wisnu.kurniawan.composetodolist.foundation.security.AppAuthGate
import com.wisnu.kurniawan.composetodolist.foundation.uiextension.rememberBottomSheetNavigator
import com.wisnu.kurniawan.composetodolist.foundation.window.WindowState

const val MinLargeScreenWidth = 585

@Composable
fun MainNavHost(windowState: WindowState) {
    val bottomSheetNavigator = rememberBottomSheetNavigator()
    val bottomSheetConfig = remember { mutableStateOf(DefaultMainBottomSheetConfig) }

    val smallestScreenWidthDp = LocalConfiguration.current.smallestScreenWidthDp

    val isLargeScreen = smallestScreenWidthDp > MinLargeScreenWidth

    ModalBottomSheetLayout(
        bottomSheetNavigator = bottomSheetNavigator,
        sheetShape = bottomSheetConfig.value.sheetShape,
        scrimColor = if (bottomSheetConfig.value.showScrim) {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.32f)
        } else {
            Color.Transparent
        }
    ) {
        if (isLargeScreen) {
            LargeScreenNavHost(bottomSheetNavigator, windowState, bottomSheetConfig)
        } else {
            SmallScreenNavHost(bottomSheetNavigator, bottomSheetConfig)
        }
    }
}

@Composable
private fun LargeScreenNavHost(
    bottomSheetNavigator: BottomSheetNavigator,
    windowState: WindowState,
    bottomSheetConfig: MutableState<MainBottomSheetConfig>
) {
    val navController = rememberNavController(bottomSheetNavigator)
    val toDoMainViewModel = hiltViewModel<ToDoMainViewModel>()
    val authSession by AppAuthGate.session.collectAsStateWithLifecycle()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    LaunchedEffect(authSession.lockVersion, authSession.isLocked, currentRoute) {
        if (!authSession.isGateEnabled || !authSession.isLocked) return@LaunchedEffect
        if (currentRoute == null || currentRoute == MainFlow.Root.route || currentRoute == MainFlow.AuthGate.route) return@LaunchedEffect
        navController.navigate(MainFlow.AuthGate.route) {
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = MainFlow.Root.route
    ) {
        composable(route = MainFlow.Root.route) {
            val viewModel = hiltViewModel<SplashViewModel>()
            SplashScreen(navController = navController, viewModel = viewModel)
        }

        composable(route = MainFlow.AuthGate.route) {
            val viewModel = hiltViewModel<AuthGateViewModel>()
            AuthGateScreen(navController = navController, viewModel = viewModel)
        }

        composable(HomeFlow.Root.route) {
            if (windowState.isDualPortrait()) {
                HomeTabletNavHost(navController, 1F, 1F, toDoMainViewModel)
            } else {
                HomeTabletNavHost(navController, 0.333F, 0.666F, toDoMainViewModel)
            }
        }
    }
}

@Composable
private fun SmallScreenNavHost(
    bottomSheetNavigator: BottomSheetNavigator,
    bottomSheetConfig: MutableState<MainBottomSheetConfig>
) {
    val navController = rememberNavController(bottomSheetNavigator)
    val toDoMainViewModel = hiltViewModel<ToDoMainViewModel>()
    val toDoMainState by toDoMainViewModel.state.collectAsStateWithLifecycle()
    val authSession by AppAuthGate.session.collectAsStateWithLifecycle()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val canShowGlobalHint =
        currentRoute != null &&
            currentRoute != MainFlow.Root.route &&
            currentRoute != MainFlow.AuthGate.route

    LaunchedEffect(authSession.lockVersion, authSession.isLocked, currentRoute) {
        if (!authSession.isGateEnabled || !authSession.isLocked) return@LaunchedEffect
        if (currentRoute == null || currentRoute == MainFlow.Root.route || currentRoute == MainFlow.AuthGate.route) return@LaunchedEffect
        navController.navigate(MainFlow.AuthGate.route) {
            launchSingleTop = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = MainFlow.Root.route
        ) {
            composable(route = MainFlow.Root.route) {
                val viewModel = hiltViewModel<SplashViewModel>()
                SplashScreen(navController = navController, viewModel = viewModel)
            }

            composable(route = MainFlow.AuthGate.route) {
                val viewModel = hiltViewModel<AuthGateViewModel>()
                AuthGateScreen(navController = navController, viewModel = viewModel)
            }

            HomeNavHost(navController, toDoMainViewModel)

            ListDetailNavHost(navController, bottomSheetConfig, Icons.Rounded.ChevronLeft)

            StepNavHost(navController, bottomSheetConfig)
        }

        if (canShowGlobalHint && toDoMainState.showClipboardSoftImportHint) {
            QuickFillHintHost(
                title = toDoMainState.pendingSoftClipboardCandidate?.title.orEmpty(),
                onConfirm = {
                    navController.navigate(HomeFlow.DashboardScreen.route) {
                        launchSingleTop = true
                        popUpTo(HomeFlow.Root.route) {
                            inclusive = false
                        }
                    }
                    toDoMainViewModel.dispatch(ToDoMainAction.ConfirmImportClipboardHint)
                },
                onDismiss = {
                    toDoMainViewModel.dispatch(ToDoMainAction.DismissImportClipboardHint)
                }
            )
        }
    }
}

@Composable
private fun HomeTabletNavHost(
    navController: NavHostController,
    weightLeft: Float,
    weightRight: Float,
    toDoMainViewModel: ToDoMainViewModel,
) {
    val bottomSheetNavigatorLeft = rememberBottomSheetNavigator()
    val bottomSheetConfigLeft = remember { mutableStateOf(DefaultMainBottomSheetConfig) }
    val navControllerLeft = rememberNavController(bottomSheetNavigatorLeft)
    val toDoMainState by toDoMainViewModel.state.collectAsStateWithLifecycle()

    val bottomSheetNavigatorRight = rememberBottomSheetNavigator()
    val bottomSheetConfigRight = remember { mutableStateOf(DefaultMainBottomSheetConfig) }
    val navControllerRight = rememberNavController(bottomSheetNavigatorRight)

    Box(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Left column
            Box(modifier = Modifier.fillMaxHeight().weight(weightLeft)) {
                ModalBottomSheetLayout(
                    bottomSheetNavigator = bottomSheetNavigatorLeft,
                    sheetShape = bottomSheetConfigLeft.value.sheetShape,
                    scrimColor = if (bottomSheetConfigLeft.value.showScrim) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.32f)
                    } else {
                        Color.Transparent
                    }
                ) {
                    NavHost(
                        navController = navControllerLeft,
                        startDestination = HomeFlow.Root.route
                    ) {
                        HomeTabletNavHost(
                            navController,
                            navControllerLeft,
                            navControllerRight,
                            toDoMainViewModel
                        )
                    }
                }
            }

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
            )

            // Right column
            Box(modifier = Modifier.fillMaxHeight().weight(weightRight)) {
                ModalBottomSheetLayout(
                    bottomSheetNavigator = bottomSheetNavigatorRight,
                    sheetShape = bottomSheetConfigRight.value.sheetShape,
                    scrimColor = if (bottomSheetConfigRight.value.showScrim) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.32f)
                    } else {
                        Color.Transparent
                    }
                ) {
                    NavHost(
                        navController = navControllerRight,
                        startDestination = MainFlow.RootEmpty.route
                    ) {
                        composable(route = MainFlow.RootEmpty.route) {

                        }

                        ListDetailNavHost(navControllerRight, bottomSheetConfigRight, Icons.Rounded.Close)

                        StepNavHost(navControllerRight, bottomSheetConfigRight)
                    }
                }
            }
        }

        if (toDoMainState.showClipboardSoftImportHint) {
            QuickFillHintHost(
                title = toDoMainState.pendingSoftClipboardCandidate?.title.orEmpty(),
                onConfirm = {
                    navControllerLeft.navigate(HomeFlow.DashboardScreen.route) {
                        launchSingleTop = true
                        popUpTo(HomeFlow.Root.route) {
                            inclusive = false
                        }
                    }
                    toDoMainViewModel.dispatch(ToDoMainAction.ConfirmImportClipboardHint)
                },
                onDismiss = {
                    toDoMainViewModel.dispatch(ToDoMainAction.DismissImportClipboardHint)
                }
            )
        }
    }
}
