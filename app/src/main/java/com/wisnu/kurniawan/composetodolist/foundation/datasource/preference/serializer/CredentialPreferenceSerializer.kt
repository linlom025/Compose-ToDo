package com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.serializer

import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.model.CredentialPreference

object CredentialPreferenceSerializer : EncryptedProtoSerializer<CredentialPreference>() {

    override val defaultValue: CredentialPreference = CredentialPreference.getDefaultInstance()

    override fun parseFrom(bytes: ByteArray): CredentialPreference {
        return CredentialPreference.parseFrom(bytes)
    }
}
