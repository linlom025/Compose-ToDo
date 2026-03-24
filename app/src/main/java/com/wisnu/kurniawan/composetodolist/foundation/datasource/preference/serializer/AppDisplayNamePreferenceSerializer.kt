package com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.serializer

import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.model.UserAppDisplayNamePreference

object AppDisplayNamePreferenceSerializer : EncryptedProtoSerializer<UserAppDisplayNamePreference>() {

    override val defaultValue: UserAppDisplayNamePreference = UserAppDisplayNamePreference.getDefaultInstance()

    override fun parseFrom(bytes: ByteArray): UserAppDisplayNamePreference {
        return UserAppDisplayNamePreference.parseFrom(bytes)
    }
}
