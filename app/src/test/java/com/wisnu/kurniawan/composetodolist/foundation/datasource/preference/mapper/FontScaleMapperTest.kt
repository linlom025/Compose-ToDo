package com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.mapper

import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.model.FontScalePreference
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.model.UserFontScalePreference
import org.junit.Assert.assertEquals
import org.junit.Test

class FontScaleMapperTest {

    @Test
    fun resolveUseScalePercentWhenValid() {
        val preference = UserFontScalePreference.newBuilder()
            .setPreset(FontScalePreference.SMALL)
            .setScalePercent(137)
            .build()

        assertEquals(137, preference.resolveScalePercent(50, 200))
    }

    @Test
    fun resolveFallbackToLegacyPresetWhenScalePercentInvalid() {
        val preference = UserFontScalePreference.newBuilder()
            .setPreset(FontScalePreference.LARGE)
            .setScalePercent(0)
            .build()

        assertEquals(115, preference.resolveScalePercent(50, 200))
    }
}
