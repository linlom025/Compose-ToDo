package com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.serializer

import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.model.UserPreference

object UserPreferenceSerializer : EncryptedProtoSerializer<UserPreference>() {

    override val defaultValue: UserPreference = UserPreference.getDefaultInstance()

    override fun parseFrom(bytes: ByteArray): UserPreference {
        return UserPreference.parseFrom(bytes)
    }
}
