package com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.serializer

import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.model.UserTodoVisibilityPreference

object TodoVisibilityPreferenceSerializer :
    EncryptedProtoSerializer<UserTodoVisibilityPreference>() {

    override val defaultValue: UserTodoVisibilityPreference =
        UserTodoVisibilityPreference.getDefaultInstance()

    override fun parseFrom(bytes: ByteArray): UserTodoVisibilityPreference {
        return UserTodoVisibilityPreference.parseFrom(bytes)
    }
}
