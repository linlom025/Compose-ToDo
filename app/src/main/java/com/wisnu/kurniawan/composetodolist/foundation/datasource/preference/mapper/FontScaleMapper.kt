package com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.mapper

import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.model.FontScalePreference
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.model.UserFontScalePreference

fun FontScalePreference.toLegacyScalePercent() = when (this) {
    FontScalePreference.SMALL -> 90
    FontScalePreference.NORMAL -> 100
    FontScalePreference.LARGE -> 115
    FontScalePreference.UNRECOGNIZED -> 100
}

fun UserFontScalePreference.resolveScalePercent(min: Int, max: Int): Int {
    val percent = scalePercent
    return if (percent in min..max) {
        percent
    } else {
        preset.toLegacyScalePercent()
    }
}
