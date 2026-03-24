package com.wisnu.kurniawan.composetodolist.runtime

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.wisnu.kurniawan.composetodolist.foundation.localization.LanguageType
import com.wisnu.kurniawan.composetodolist.foundation.security.AppAuthGate
import com.wisnu.kurniawan.composetodolist.foundation.security.AppSecurityContext
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ComposeToDoListApp : Application() {

    override fun onCreate() {
        super.onCreate()
        AppSecurityContext.initialize(this)
        AppAuthGate.initialize()
        val appLocale = LocaleListCompat.forLanguageTags(LanguageType.CHINESE)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }
}
