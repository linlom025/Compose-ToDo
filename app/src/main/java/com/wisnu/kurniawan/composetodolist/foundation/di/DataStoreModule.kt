package com.wisnu.kurniawan.composetodolist.foundation.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.serializer.AuthGatePreferenceSerializer
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.serializer.ClipboardImportPreferenceSerializer
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.serializer.CredentialPreferenceSerializer
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.serializer.FontScalePreferenceSerializer
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.serializer.LanguagePreferenceSerializer
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.serializer.ReminderPreferenceSerializer
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.serializer.ThemePreferenceSerializer
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.serializer.TodoVisibilityPreferenceSerializer
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.serializer.UserPreferenceSerializer
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.serializer.AppDisplayNamePreferenceSerializer
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.model.UserAppDisplayNamePreference
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.model.CredentialPreference
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.model.UserFontScalePreference
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.model.UserLanguagePreference
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.model.UserPreference
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.model.UserReminderPreference
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.model.UserThemePreference
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.model.UserTodoVisibilityPreference
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.model.UserAuthGatePreference
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.model.UserClipboardImportPreference
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val CREDENTIAL_NAME = "credential-preference.pb"
private const val USER_NAME = "user-preference.pb"
private const val THEME_NAME = "theme-preference.pb"
private const val LANGUAGE_NAME = "language-preference.pb"
private const val FONT_SCALE_NAME = "font-scale-preference.pb"
private const val TODO_VISIBILITY_NAME = "todo-visibility-preference.pb"
private const val REMINDER_NAME = "reminder-preference.pb"
private const val APP_DISPLAY_NAME = "app-display-name-preference.pb"
private const val AUTH_GATE_NAME = "auth-gate-preference.pb"
private const val CLIPBOARD_IMPORT_NAME = "clipboard-import-preference.pb"

private val Context.credentialDataStore: DataStore<CredentialPreference> by dataStore(
    fileName = CREDENTIAL_NAME,
    serializer = CredentialPreferenceSerializer
)
private val Context.userDataStore: DataStore<UserPreference> by dataStore(
    fileName = USER_NAME,
    serializer = UserPreferenceSerializer
)
private val Context.themeDataStore: DataStore<UserThemePreference> by dataStore(
    fileName = THEME_NAME,
    serializer = ThemePreferenceSerializer
)
private val Context.fontScaleDataStore: DataStore<UserFontScalePreference> by dataStore(
    fileName = FONT_SCALE_NAME,
    serializer = FontScalePreferenceSerializer
)
private val Context.todoVisibilityDataStore: DataStore<UserTodoVisibilityPreference> by dataStore(
    fileName = TODO_VISIBILITY_NAME,
    serializer = TodoVisibilityPreferenceSerializer
)
private val Context.reminderDataStore: DataStore<UserReminderPreference> by dataStore(
    fileName = REMINDER_NAME,
    serializer = ReminderPreferenceSerializer
)
private val Context.appDisplayNameDataStore: DataStore<UserAppDisplayNamePreference> by dataStore(
    fileName = APP_DISPLAY_NAME,
    serializer = AppDisplayNamePreferenceSerializer
)
private val Context.authGateDataStore: DataStore<UserAuthGatePreference> by dataStore(
    fileName = AUTH_GATE_NAME,
    serializer = AuthGatePreferenceSerializer
)
private val Context.clipboardImportDataStore: DataStore<UserClipboardImportPreference> by dataStore(
    fileName = CLIPBOARD_IMPORT_NAME,
    serializer = ClipboardImportPreferenceSerializer
)
val Context.languageDatastore: DataStore<UserLanguagePreference> by dataStore(
    fileName = LANGUAGE_NAME,
    serializer = LanguagePreferenceSerializer
)

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Singleton
    @Provides
    fun provideCredentialDataStore(@ApplicationContext context: Context): DataStore<CredentialPreference> {
        return context.credentialDataStore
    }

    @Singleton
    @Provides
    fun provideUserDataStore(@ApplicationContext context: Context): DataStore<UserPreference> {
        return context.userDataStore
    }

    @Singleton
    @Provides
    fun provideThemeDataStore(@ApplicationContext context: Context): DataStore<UserThemePreference> {
        return context.themeDataStore
    }

    @Singleton
    @Provides
    fun provideFontScaleDataStore(@ApplicationContext context: Context): DataStore<UserFontScalePreference> {
        return context.fontScaleDataStore
    }

    @Singleton
    @Provides
    fun provideTodoVisibilityDataStore(@ApplicationContext context: Context): DataStore<UserTodoVisibilityPreference> {
        return context.todoVisibilityDataStore
    }

    @Singleton
    @Provides
    fun provideReminderDataStore(@ApplicationContext context: Context): DataStore<UserReminderPreference> {
        return context.reminderDataStore
    }

    @Singleton
    @Provides
    fun provideLanguageDataStore(@ApplicationContext context: Context): DataStore<UserLanguagePreference> {
        return context.languageDatastore
    }

    @Singleton
    @Provides
    fun provideAppDisplayNameDataStore(@ApplicationContext context: Context): DataStore<UserAppDisplayNamePreference> {
        return context.appDisplayNameDataStore
    }

    @Singleton
    @Provides
    fun provideAuthGateDataStore(@ApplicationContext context: Context): DataStore<UserAuthGatePreference> {
        return context.authGateDataStore
    }

    @Singleton
    @Provides
    fun provideClipboardImportDataStore(@ApplicationContext context: Context): DataStore<UserClipboardImportPreference> {
        return context.clipboardImportDataStore
    }

}
