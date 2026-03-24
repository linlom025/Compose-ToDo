package com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.provider

import androidx.datastore.core.DataStore
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.model.UserAuthGatePreference
import com.wisnu.kurniawan.composetodolist.foundation.di.DiName
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AuthGatePreferenceProvider @Inject constructor(
    @Named(DiName.DISPATCHER_IO) private val dispatcher: CoroutineDispatcher,
    private val authGateDataStore: DataStore<UserAuthGatePreference>,
) {

    fun getAuthGateEnabled(): Flow<Boolean> {
        return authGateDataStore.data
            .map { it.enabled }
            .catch { throwable ->
                if (throwable is IOException) {
                    emit(DEFAULT_ENABLED)
                } else {
                    throw throwable
                }
            }
            .flowOn(dispatcher)
    }

    suspend fun setAuthGateEnabled(enabled: Boolean) {
        withContext(dispatcher) {
            authGateDataStore.updateData {
                UserAuthGatePreference.newBuilder()
                    .setEnabled(enabled)
                    .build()
            }
        }
    }

    companion object {
        const val DEFAULT_ENABLED = false
    }
}
