package com.wisnu.kurniawan.composetodolist.foundation.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageKeyProvider @Inject constructor(
    @ApplicationContext context: Context
) {
    private val appContext = context.applicationContext
    private val preferences by lazy {
        appContext.getSharedPreferences(SECURITY_PREFS_NAME, Context.MODE_PRIVATE)
    }

    @Volatile
    private var cachedDatabasePassphrase: ByteArray? = null

    @Volatile
    private var cachedDataStoreKey: ByteArray? = null

    fun getDatabasePassphrase(): ByteArray {
        return cachedDatabasePassphrase?.copyOf() ?: synchronized(this) {
            cachedDatabasePassphrase?.copyOf() ?: run {
                val keyBytes = getOrCreateRawKey(DB_KEY_PREF)
                val passphrase = Base64.encodeToString(keyBytes, Base64.NO_WRAP)
                    .toByteArray(StandardCharsets.UTF_8)
                cachedDatabasePassphrase = passphrase
                passphrase.copyOf()
            }
        }
    }

    fun getDatabasePassphraseString(): String {
        return String(getDatabasePassphrase(), StandardCharsets.UTF_8)
    }

    fun getDataStoreAesKey(): ByteArray {
        return cachedDataStoreKey?.copyOf() ?: synchronized(this) {
            cachedDataStoreKey?.copyOf() ?: run {
                val keyBytes = getOrCreateRawKey(DATASTORE_KEY_PREF)
                cachedDataStoreKey = keyBytes
                keyBytes.copyOf()
            }
        }
    }

    private fun getOrCreateRawKey(prefName: String): ByteArray {
        val encoded = preferences.getString(prefName, null)
        if (!encoded.isNullOrBlank()) {
            return unwrapKey(Base64.decode(encoded, Base64.DEFAULT))
        }

        val raw = ByteArray(KEY_SIZE_BYTES)
        SecureRandom().nextBytes(raw)

        val wrapped = wrapKey(raw)
        preferences.edit().putString(
            prefName,
            Base64.encodeToString(wrapped, Base64.NO_WRAP)
        ).apply()

        return raw
    }

    private fun wrapKey(raw: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateMasterKey())
        val encrypted = cipher.doFinal(raw)

        val result = ByteArray(NONCE_SIZE_BYTES + encrypted.size)
        cipher.iv.copyInto(result, 0, 0, NONCE_SIZE_BYTES)
        encrypted.copyInto(result, NONCE_SIZE_BYTES)
        return result
    }

    private fun unwrapKey(payload: ByteArray): ByteArray {
        require(payload.size > NONCE_SIZE_BYTES) { "Invalid wrapped key payload." }

        val nonce = payload.copyOfRange(0, NONCE_SIZE_BYTES)
        val encrypted = payload.copyOfRange(NONCE_SIZE_BYTES, payload.size)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            getOrCreateMasterKey(),
            GCMParameterSpec(TAG_LENGTH_BITS, nonce)
        )
        return cipher.doFinal(encrypted)
    }

    private fun getOrCreateMasterKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_TYPE).apply { load(null) }
        val existed = keyStore.getEntry(MASTER_KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        if (existed != null) return existed.secretKey

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_TYPE)
        val spec = KeyGenParameterSpec.Builder(
            MASTER_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(false)
            .build()

        generator.init(spec)
        return generator.generateKey()
    }

    companion object {
        private const val SECURITY_PREFS_NAME = "lltodo-secure-storage-keys"
        private const val DB_KEY_PREF = "db-key-v1"
        private const val DATASTORE_KEY_PREF = "datastore-key-v1"
        private const val KEY_SIZE_BYTES = 32
        private const val KEYSTORE_TYPE = "AndroidKeyStore"
        private const val MASTER_KEY_ALIAS = "lltodo-storage-master-key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val NONCE_SIZE_BYTES = 12
        private const val TAG_LENGTH_BITS = 128

        @Volatile
        private var INSTANCE: StorageKeyProvider? = null

        fun getInstance(context: Context): StorageKeyProvider {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: StorageKeyProvider(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}
