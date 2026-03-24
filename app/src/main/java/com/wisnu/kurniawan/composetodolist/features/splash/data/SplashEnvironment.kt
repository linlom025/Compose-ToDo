package com.wisnu.kurniawan.composetodolist.features.splash.data

import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.provider.CredentialProvider
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.provider.AuthGatePreferenceProvider
import com.wisnu.kurniawan.composetodolist.foundation.security.MigrationRequirement
import com.wisnu.kurniawan.composetodolist.foundation.security.StorageEncryptionMigrationCoordinator
import com.wisnu.kurniawan.composetodolist.model.Credential
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SplashEnvironment @Inject constructor(
    private val credentialProvider: CredentialProvider,
    private val authGatePreferenceProvider: AuthGatePreferenceProvider,
    private val migrationCoordinator: StorageEncryptionMigrationCoordinator
) : ISplashEnvironment {

    override fun getCredential(): Flow<Credential> {
        return credentialProvider.getCredential()
    }

    override suspend fun getAuthGateEnabled(): Boolean {
        return authGatePreferenceProvider.getAuthGateEnabled().first()
    }

    override suspend fun getMigrationRequirement(): MigrationRequirement {
        return migrationCoordinator.getMigrationRequirement()
    }

    override suspend fun migrateStorageIfNeeded() {
        migrationCoordinator.migrateIfNeeded()
    }

}
