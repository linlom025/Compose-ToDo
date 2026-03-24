package com.wisnu.kurniawan.composetodolist.foundation.security

import android.content.Context

object AppSecurityContext {
    @Volatile
    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    fun getContextOrNull(): Context? = appContext

    fun requireContext(): Context {
        return appContext
            ?: error("AppSecurityContext is not initialized.")
    }
}
