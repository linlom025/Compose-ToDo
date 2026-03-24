package com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.serializer

import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.model.UserReminderPreference

object ReminderPreferenceSerializer : EncryptedProtoSerializer<UserReminderPreference>() {

    override val defaultValue: UserReminderPreference = UserReminderPreference.getDefaultInstance()

    override fun parseFrom(bytes: ByteArray): UserReminderPreference {
        return UserReminderPreference.parseFrom(bytes)
    }
}
