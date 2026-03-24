package com.wisnu.kurniawan.composetodolist.runtime.navigation

import androidx.navigation.navArgument
import androidx.navigation.navDeepLink

sealed class MainFlow(val name: String) {
    object Root : MainFlow("main-root") {
        val route = name
    }

    object AuthGate : MainFlow("auth-gate") {
        val route = name
    }

    object RootEmpty : MainFlow("list-detail-root-empty") {
        val route = name
    }
}

sealed class HomeFlow(val name: String) {
    object Root : HomeFlow("home-root") {
        val route = name
    }

    object DashboardScreen : HomeFlow("dashboard-screen") {
        val route = name
    }

    object CalendarScreen : HomeFlow("calendar-screen") {
        val route = name
    }

    object SettingsScreen : HomeFlow("settings-screen") {
        val route = name
    }
}

sealed class ListDetailFlow(val name: String) {
    object Root : ListDetailFlow("list-detail-root") {
        val route = "$name?$ARG_LIST_ID={$ARG_LIST_ID}"

        fun route(listId: String = ""): String {
            return "$name?$ARG_LIST_ID=${listId}"
        }
    }

    object ListDetailScreen : ListDetailFlow("list-detail-screen") {
        val arguments = listOf(
            navArgument(ARG_LIST_ID) {
                defaultValue = ""
            }
        )

        val route = "$name?$ARG_LIST_ID={$ARG_LIST_ID}"
    }

    object CreateList : ListDetailFlow("create-list-screen") {
        val route = name
    }

    object UpdateList : ListDetailFlow("update-list-screen") {
        val route = name
    }

    object CreateTask : ListDetailFlow("create-task-screen") {
        val route = name
    }
}

sealed class StepFlow(val name: String) {
    object Root : StepFlow("step-root") {
        val route = "$name?$ARG_TASK_ID={$ARG_TASK_ID}&$ARG_LIST_ID={$ARG_LIST_ID}"

        fun route(taskId: String, listId: String): String {
            return "$name?$ARG_TASK_ID=${taskId}&$ARG_LIST_ID=${listId}"
        }
    }

    object TaskDetailScreen : StepFlow("task-detail-screen") {
        val arguments = listOf(
            navArgument(ARG_TASK_ID) {
                defaultValue = ""
            },
            navArgument(ARG_LIST_ID) {
                defaultValue = ""
            }
        )

        val route = "$name?$ARG_TASK_ID={$ARG_TASK_ID}&$ARG_LIST_ID={$ARG_LIST_ID}"

        val deepLinks = listOf(navDeepLink { uriPattern = "$BASE_DEEPLINK/$route" })

        fun deeplink(taskId: String, listId: String): String {
            return "$BASE_DEEPLINK/$name?$ARG_TASK_ID=${taskId}&$ARG_LIST_ID=${listId}"
        }
    }

    object CreateStep : StepFlow("create-step-screen") {
        val route = name
    }

    object EditStep : StepFlow("edit-step-screen") {
        val arguments = listOf(
            navArgument(ARG_STEP_ID) {
                defaultValue = ""
            }
        )

        val route = "$name?$ARG_STEP_ID={$ARG_STEP_ID}"

        fun route(stepId: String): String {
            return "$name?$ARG_STEP_ID=${stepId}"
        }
    }

    object EditTask : StepFlow("edit-task-screen") {
        val route = name
    }

    object SelectRepeatTask : StepFlow("select-repeat-task-screen") {
        val route = name
    }
}

const val BASE_DEEPLINK = "remindee://com.wisnu.kurniawan"

const val ARG_STEP_ID = "stepId"
const val ARG_TASK_ID = "taskId"
const val ARG_LIST_ID = "listId"
