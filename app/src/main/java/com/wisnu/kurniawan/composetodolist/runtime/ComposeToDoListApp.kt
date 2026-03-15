package com.wisnu.kurniawan.composetodolist.runtime

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.wisnu.kurniawan.composetodolist.foundation.localization.LanguageType
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ComposeToDoListApp : Application() {

    override fun onCreate() {
        super.onCreate()
        val appLocale = LocaleListCompat.forLanguageTags(LanguageType.CHINESE)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }
}
