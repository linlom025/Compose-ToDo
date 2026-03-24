package com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.provider

import androidx.datastore.core.DataStore
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.model.UserTodoVisibilityPreference
import com.wisnu.kurniawan.composetodolist.foundation.di.DiName
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named

class TodoVisibilityProvider @Inject constructor(
    @Named(DiName.DISPATCHER_IO) private val dispatcher: CoroutineDispatcher,
    private val todoVisibilityDataStore: DataStore<UserTodoVisibilityPreference>,
) {

    fun getShowCompleted(): Flow<Boolean> {
        return todoVisibilityDataStore.data
            .map { it.showCompleted }
            .catch { emit(DEFAULT_SHOW_COMPLETED) }
            .flowOn(dispatcher)
    }

    suspend fun setShowCompleted(showCompleted: Boolean) {
        withContext(dispatcher) {
            todoVisibilityDataStore.updateData {
                UserTodoVisibilityPreference.newBuilder()
                    .setShowCompleted(showCompleted)
                    .build()
            }
        }
    }

    companion object {
        const val DEFAULT_SHOW_COMPLETED = false
    }
}
