package com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.provider

import androidx.datastore.core.DataStore
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.model.UserReminderPreference
import com.wisnu.kurniawan.composetodolist.foundation.di.DiName
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named

class ReminderPreferenceProvider @Inject constructor(
    @Named(DiName.DISPATCHER_IO) private val dispatcher: CoroutineDispatcher,
    private val reminderDataStore: DataStore<UserReminderPreference>,
) {

    fun getReminderLeadMinutes(): Flow<Int> {
        return reminderDataStore.data
            .map { pref ->
                val minutes = pref.leadMinutes
                if (minutes in REMINDER_LEAD_MIN_MINUTES..REMINDER_LEAD_MAX_MINUTES) {
                    minutes
                } else {
                    REMINDER_LEAD_DEFAULT_MINUTES
                }
            }
            .catch { throwable ->
                if (throwable is IOException) {
                    emit(REMINDER_LEAD_DEFAULT_MINUTES)
                } else {
                    throw throwable
                }
            }
            .flowOn(dispatcher)
    }

    suspend fun setReminderLeadMinutes(minutes: Int) {
        val target = minutes.coerceIn(REMINDER_LEAD_MIN_MINUTES, REMINDER_LEAD_MAX_MINUTES)
        withContext(dispatcher) {
            reminderDataStore.updateData {
                UserReminderPreference.newBuilder()
                    .setLeadMinutes(target)
                    .build()
            }
        }
    }

    companion object {
        const val REMINDER_LEAD_DEFAULT_MINUTES = 15
        const val REMINDER_LEAD_MIN_MINUTES = 1
        const val REMINDER_LEAD_MAX_MINUTES = 1440
    }
}
