package com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.provider

import androidx.datastore.core.DataStore
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.mapper.resolveScalePercent
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.model.UserFontScalePreference
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

class FontScaleProvider @Inject constructor(
    @Named(DiName.DISPATCHER_IO) private val dispatcher: CoroutineDispatcher,
    private val fontScaleDataStore: DataStore<UserFontScalePreference>,
) {

    fun getFontScalePercent(): Flow<Int> {
        return fontScaleDataStore.data
            .map { it.resolveScalePercent(FONT_SCALE_MIN_PERCENT, FONT_SCALE_MAX_PERCENT) }
            .catch { throwable ->
                if (throwable is IOException) {
                    emit(FONT_SCALE_DEFAULT_PERCENT)
                } else {
                    throw throwable
                }
            }
            .flowOn(dispatcher)
    }

    suspend fun setFontScalePercent(percent: Int) {
        val target = percent.coerceIn(FONT_SCALE_MIN_PERCENT, FONT_SCALE_MAX_PERCENT)
        withContext(dispatcher) {
            fontScaleDataStore.updateData {
                UserFontScalePreference.newBuilder()
                    .setScalePercent(target)
                    .setPreset(it.preset)
                    .build()
            }
        }
    }

    companion object {
        const val FONT_SCALE_MIN_PERCENT = 50
        const val FONT_SCALE_MAX_PERCENT = 200
        const val FONT_SCALE_DEFAULT_PERCENT = 100
    }
}
