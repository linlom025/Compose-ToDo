package com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.serializer

import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.model.UserFontScalePreference

object FontScalePreferenceSerializer : EncryptedProtoSerializer<UserFontScalePreference>() {

    override val defaultValue: UserFontScalePreference = UserFontScalePreference.getDefaultInstance()

    override fun parseFrom(bytes: ByteArray): UserFontScalePreference {
        return UserFontScalePreference.parseFrom(bytes)
    }
}
