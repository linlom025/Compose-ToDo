package com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.serializer

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.MessageLite
import com.wisnu.kurniawan.composetodolist.foundation.security.StorageAead
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

abstract class EncryptedProtoSerializer<T : MessageLite> : Serializer<T> {
    protected abstract fun parseFrom(bytes: ByteArray): T

    override suspend fun readFrom(input: InputStream): T {
        val payload = try {
            input.readBytes()
        } catch (exception: IOException) {
            throw CorruptionException("Cannot read encrypted proto.", exception)
        }

        if (payload.isEmpty()) return defaultValue

        return runCatching {
            if (StorageAead.isEncrypted(payload)) {
                parseFrom(StorageAead.decrypt(payload))
            } else {
                parseFrom(payload)
            }
        }.getOrElse {
            throw CorruptionException("Cannot decode encrypted proto.", it)
        }
    }

    override suspend fun writeTo(t: T, output: OutputStream) {
        try {
            val encryptedBytes = StorageAead.encrypt(t.toByteArray())
            output.write(encryptedBytes)
        } catch (exception: Exception) {
            throw CorruptionException("Cannot write encrypted proto.", exception)
        }
    }
}
