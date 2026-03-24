package com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.serializer

import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.model.UserAuthGatePreference

object AuthGatePreferenceSerializer : EncryptedProtoSerializer<UserAuthGatePreference>() {

    override val defaultValue: UserAuthGatePreference = UserAuthGatePreference.getDefaultInstance()

    override fun parseFrom(bytes: ByteArray): UserAuthGatePreference {
        return UserAuthGatePreference.parseFrom(bytes)
    }
}
