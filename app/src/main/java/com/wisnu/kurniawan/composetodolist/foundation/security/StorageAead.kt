package com.wisnu.kurniawan.composetodolist.foundation.security

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object StorageAead {
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val KEY_ALGORITHM = "AES"
    private const val TAG_LENGTH_BITS = 128
    private const val NONCE_SIZE_BYTES = 12
    private const val VERSION: Byte = 1
    private val MAGIC = byteArrayOf(0x4c, 0x54, 0x44, 0x53) // LTDS

    private val secureRandom by lazy { SecureRandom() }

    fun isEncrypted(payload: ByteArray): Boolean {
        if (payload.size < MAGIC.size + 1) return false

        if (!payload.copyOfRange(0, MAGIC.size).contentEquals(MAGIC)) return false

        return payload[MAGIC.size] == VERSION
    }

    fun encrypt(plaintext: ByteArray): ByteArray {
        val nonce = ByteArray(NONCE_SIZE_BYTES)
        secureRandom.nextBytes(nonce)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), GCMParameterSpec(TAG_LENGTH_BITS, nonce))
        val cipherText = cipher.doFinal(plaintext)

        val output = ByteArray(MAGIC.size + 1 + NONCE_SIZE_BYTES + cipherText.size)
        var writeOffset = 0

        MAGIC.copyInto(output, writeOffset)
        writeOffset += MAGIC.size

        output[writeOffset] = VERSION
        writeOffset += 1

        nonce.copyInto(output, writeOffset)
        writeOffset += NONCE_SIZE_BYTES

        cipherText.copyInto(output, writeOffset)
        return output
    }

    fun decrypt(payload: ByteArray): ByteArray {
        if (!isEncrypted(payload)) {
            throw IllegalArgumentException("Payload is not encrypted with expected format.")
        }

        val nonceStart = MAGIC.size + 1
        val cipherTextStart = nonceStart + NONCE_SIZE_BYTES
        if (payload.size <= cipherTextStart) {
            throw IllegalArgumentException("Encrypted payload is truncated.")
        }

        val nonce = payload.copyOfRange(nonceStart, cipherTextStart)
        val cipherText = payload.copyOfRange(cipherTextStart, payload.size)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), GCMParameterSpec(TAG_LENGTH_BITS, nonce))
        return cipher.doFinal(cipherText)
    }

    private fun getSecretKey(): SecretKeySpec {
        val context = AppSecurityContext.requireContext()
        val key = StorageKeyProvider.getInstance(context).getDataStoreAesKey()
        return SecretKeySpec(key, KEY_ALGORITHM)
    }
}
