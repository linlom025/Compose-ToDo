package com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.provider

import androidx.datastore.core.DataStore
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.model.UserAppDisplayNamePreference
import com.wisnu.kurniawan.composetodolist.foundation.di.DiName
import com.wisnu.kurniawan.composetodolist.model.AppDisplayNameConfig
import com.wisnu.kurniawan.composetodolist.model.DEFAULT_APP_TITLE
import com.wisnu.kurniawan.composetodolist.model.DEFAULT_Q1_TITLE
import com.wisnu.kurniawan.composetodolist.model.DEFAULT_Q2_TITLE
import com.wisnu.kurniawan.composetodolist.model.DEFAULT_Q3_TITLE
import com.wisnu.kurniawan.composetodolist.model.DEFAULT_Q4_TITLE
import com.wisnu.kurniawan.composetodolist.model.QuadrantDisplayNames
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named

class AppDisplayNameProvider @Inject constructor(
    @Named(DiName.DISPATCHER_IO) private val dispatcher: CoroutineDispatcher,
    private val appDisplayNameDataStore: DataStore<UserAppDisplayNamePreference>,
) {

    fun getDisplayNameConfig(): Flow<AppDisplayNameConfig> {
        return appDisplayNameDataStore.data
            .map { pref ->
                AppDisplayNameConfig(
                    appTitle = pref.appDisplayTitle.trimOrDefault(DEFAULT_APP_TITLE),
                    quadrantTitles = QuadrantDisplayNames(
                        q1 = pref.quadrantTitleQ1.trimOrDefault(DEFAULT_Q1_TITLE),
                        q2 = pref.quadrantTitleQ2.trimOrDefault(DEFAULT_Q2_TITLE),
                        q3 = pref.quadrantTitleQ3.trimOrDefault(DEFAULT_Q3_TITLE),
                        q4 = pref.quadrantTitleQ4.trimOrDefault(DEFAULT_Q4_TITLE)
                    )
                )
            }
            .catch { throwable ->
                if (throwable is IOException) {
                    emit(AppDisplayNameConfig.default())
                } else {
                    throw throwable
                }
            }
            .flowOn(dispatcher)
    }

    suspend fun setDisplayNameConfig(config: AppDisplayNameConfig) {
        withContext(dispatcher) {
            appDisplayNameDataStore.updateData {
                UserAppDisplayNamePreference.newBuilder()
                    .setAppDisplayTitle(config.appTitle.trimOrDefault(DEFAULT_APP_TITLE))
                    .setQuadrantTitleQ1(config.quadrantTitles.q1.trimOrDefault(DEFAULT_Q1_TITLE))
                    .setQuadrantTitleQ2(config.quadrantTitles.q2.trimOrDefault(DEFAULT_Q2_TITLE))
                    .setQuadrantTitleQ3(config.quadrantTitles.q3.trimOrDefault(DEFAULT_Q3_TITLE))
                    .setQuadrantTitleQ4(config.quadrantTitles.q4.trimOrDefault(DEFAULT_Q4_TITLE))
                    .build()
            }
        }
    }

    suspend fun resetDefault() {
        setDisplayNameConfig(AppDisplayNameConfig.default())
    }

    private fun String?.trimOrDefault(default: String): String {
        val text = this?.trim().orEmpty()
        return if (text.isBlank()) default else text
    }
}
