package com.wisnu.kurniawan.composetodolist.features.splash.data

import com.wisnu.kurniawan.composetodolist.foundation.security.MigrationRequirement
import com.wisnu.kurniawan.composetodolist.model.Credential
import kotlinx.coroutines.flow.Flow

interface ISplashEnvironment {
    fun getCredential(): Flow<Credential>
    suspend fun getAuthGateEnabled(): Boolean
    suspend fun getMigrationRequirement(): MigrationRequirement
    suspend fun migrateStorageIfNeeded()
}
