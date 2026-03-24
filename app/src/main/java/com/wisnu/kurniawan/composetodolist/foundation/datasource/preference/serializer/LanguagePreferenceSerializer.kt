package com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.serializer

import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.model.UserLanguagePreference

object LanguagePreferenceSerializer : EncryptedProtoSerializer<UserLanguagePreference>() {

    override val defaultValue: UserLanguagePreference = UserLanguagePreference.getDefaultInstance()

    override fun parseFrom(bytes: ByteArray): UserLanguagePreference {
        return UserLanguagePreference.parseFrom(bytes)
    }
}
