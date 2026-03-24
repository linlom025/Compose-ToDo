package com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.serializer

import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.model.UserThemePreference

object ThemePreferenceSerializer : EncryptedProtoSerializer<UserThemePreference>() {

    override val defaultValue: UserThemePreference = UserThemePreference.getDefaultInstance()

    override fun parseFrom(bytes: ByteArray): UserThemePreference {
        return UserThemePreference.parseFrom(bytes)
    }
}
