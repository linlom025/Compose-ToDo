package com.wisnu.kurniawan.composetodolist.features.splash.ui

data class SplashState(
    val migrationStatus: MigrationStatus = MigrationStatus.Idle,
    val errorMessage: String = ""
)

enum class MigrationStatus {
    Idle,
    Migrating,
    Failed,
    Done,
}
