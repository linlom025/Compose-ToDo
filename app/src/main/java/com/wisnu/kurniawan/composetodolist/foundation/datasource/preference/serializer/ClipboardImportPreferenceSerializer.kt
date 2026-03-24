package com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.serializer

import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.model.UserClipboardImportPreference

object ClipboardImportPreferenceSerializer : EncryptedProtoSerializer<UserClipboardImportPreference>() {

    override val defaultValue: UserClipboardImportPreference = UserClipboardImportPreference.getDefaultInstance()

    override fun parseFrom(bytes: ByteArray): UserClipboardImportPreference {
        return UserClipboardImportPreference.parseFrom(bytes)
    }
}
