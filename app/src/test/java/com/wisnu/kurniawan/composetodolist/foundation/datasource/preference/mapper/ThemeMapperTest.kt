package com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.mapper

import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.model.ThemePreference
import com.wisnu.kurniawan.composetodolist.model.Theme
import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeMapperTest {

    @Test
    fun toThemePreference_mapAllSupportedThemes() {
        assertEquals(ThemePreference.SYSTEM, Theme.SYSTEM.toThemePreference())
        assertEquals(ThemePreference.LIGHT, Theme.LIGHT.toThemePreference())
        assertEquals(ThemePreference.TWILIGHT, Theme.TWILIGHT.toThemePreference())
        assertEquals(ThemePreference.NIGHT, Theme.NIGHT.toThemePreference())
        assertEquals(ThemePreference.SUNRISE, Theme.SUNRISE.toThemePreference())
        assertEquals(ThemePreference.AURORA, Theme.AURORA.toThemePreference())
    }

    @Test
    fun toTheme_mapLegacyWallpaperToSystem() {
        assertEquals(Theme.SYSTEM, ThemePreference.WALLPAPER.toTheme())
    }
}
