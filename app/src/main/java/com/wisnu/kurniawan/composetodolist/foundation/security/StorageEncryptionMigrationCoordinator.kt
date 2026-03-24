package com.wisnu.kurniawan.composetodolist.foundation.security

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import androidx.datastore.dataStoreFile
import com.wisnu.foundation.coreloggr.Loggr
import com.wisnu.kurniawan.composetodolist.foundation.datasource.local.ToDoDatabase
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.serializer.CredentialPreferenceSerializer
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.serializer.ClipboardImportPreferenceSerializer
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.serializer.FontScalePreferenceSerializer
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.serializer.LanguagePreferenceSerializer
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.serializer.ReminderPreferenceSerializer
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.serializer.ThemePreferenceSerializer
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.serializer.TodoVisibilityPreferenceSerializer
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.serializer.UserPreferenceSerializer
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.serializer.AppDisplayNamePreferenceSerializer
import com.wisnu.kurniawan.composetodolist.foundation.datasource.preference.serializer.AuthGatePreferenceSerializer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Singleton
import net.sqlcipher.database.SQLiteDatabase as SqlCipherDatabase

@Singleton
class StorageEncryptionMigrationCoordinator @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val markerPref by lazy {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun isMigrationCompleted(): Boolean {
        return markerPref.getInt(KEY_MIGRATION_VERSION, 0) >= MIGRATION_VERSION
    }

    suspend fun getMigrationRequirement(): MigrationRequirement = withContext(Dispatchers.IO) {
        val sourceDb = context.getDatabasePath(ToDoDatabase.TODO_DB_NAME)
        val encryptedTempDb = context.getDatabasePath("${ToDoDatabase.TODO_DB_NAME}.enc_tmp")
        val plainBackupDb = context.getDatabasePath("${ToDoDatabase.TODO_DB_NAME}.plain_backup")

        recoverOrCleanupHalfMigratedState(sourceDb, encryptedTempDb, plainBackupDb)

        val databaseRequirement = when {
            !sourceDb.exists() -> MigrationRequirement.NO_MIGRATION
            isPlaintextDatabase(sourceDb) -> MigrationRequirement.NEEDS_MIGRATION
            isEncryptedDatabase(sourceDb) -> MigrationRequirement.NO_MIGRATION
            else -> MigrationRequirement.UNRECOVERABLE
        }

        if (databaseRequirement == MigrationRequirement.UNRECOVERABLE) {
            return@withContext MigrationRequirement.UNRECOVERABLE
        }

        var datastoreRequirementResult = getDataStoreRequirementResult()
        var datastoreRequirement = datastoreRequirementResult.requirement

        if (
            databaseRequirement == MigrationRequirement.NO_MIGRATION &&
            datastoreRequirement == MigrationRequirement.NEEDS_MIGRATION
        ) {
            runCatching {
                migrateDataStoreIfNeeded()
            }.onFailure { throwable ->
                Loggr.debug("StorageMigration") {
                    "DataStore 自动归一化失败：${throwable.message}"
                }
            }

            datastoreRequirementResult = getDataStoreRequirementResult()
            datastoreRequirement = datastoreRequirementResult.requirement
        }

        val requirement = if (
            databaseRequirement == MigrationRequirement.NEEDS_MIGRATION ||
            datastoreRequirement == MigrationRequirement.NEEDS_MIGRATION
        ) {
            MigrationRequirement.NEEDS_MIGRATION
        } else {
            MigrationRequirement.NO_MIGRATION
        }

        if (requirement == MigrationRequirement.NEEDS_MIGRATION) {
            val datastoreReasons = datastoreRequirementResult.plaintextFiles
                .takeIf { it.isNotEmpty() }
                ?.joinToString()
                ?: "无"
            Loggr.debug("StorageMigration") {
                "判定需要迁移：db=$databaseRequirement, datastore=$datastoreRequirement, 明文DataStore=$datastoreReasons"
            }
        }

        if (requirement == MigrationRequirement.NO_MIGRATION && !isMigrationCompleted()) {
            markerPref.edit()
                .putInt(KEY_MIGRATION_VERSION, MIGRATION_VERSION)
                .apply()
        }

        requirement
    }

    suspend fun migrateIfNeeded() = withContext(Dispatchers.IO) {
        when (getMigrationRequirement()) {
            MigrationRequirement.NO_MIGRATION -> return@withContext
            MigrationRequirement.UNRECOVERABLE -> {
                throw IllegalStateException("检测到不可恢复的数据状态，无法继续迁移。")
            }
            MigrationRequirement.NEEDS_MIGRATION -> {
                migrateDatabaseIfNeeded()
                migrateDataStoreIfNeeded()
                markerPref.edit()
                    .putInt(KEY_MIGRATION_VERSION, MIGRATION_VERSION)
                    .apply()
            }
        }
    }

    private fun migrateDatabaseIfNeeded() {
        val sourceDb = context.getDatabasePath(ToDoDatabase.TODO_DB_NAME)
        val encryptedTempDb = context.getDatabasePath("${ToDoDatabase.TODO_DB_NAME}.enc_tmp")
        val plainBackupDb = context.getDatabasePath("${ToDoDatabase.TODO_DB_NAME}.plain_backup")

        recoverOrCleanupHalfMigratedState(sourceDb, encryptedTempDb, plainBackupDb)

        if (!sourceDb.exists()) return

        if (!isPlaintextDatabase(sourceDb)) {
            if (!isEncryptedDatabase(sourceDb)) {
                throw IllegalStateException("数据库文件格式异常，无法识别。")
            }
            return
        }

        deleteDatabaseFamily(encryptedTempDb)
        deleteDatabaseFamily(plainBackupDb)

        val exportSucceeded = trySqlCipherExport(sourceDb, encryptedTempDb)
        val migrated = exportSucceeded || migrateDatabaseByLogicalCopy(sourceDb, encryptedTempDb)
        if (!migrated) {
            throw IllegalStateException("数据库加密迁移失败。")
        }

        verifyDatabaseRowCounts(sourceDb, encryptedTempDb)

        moveDatabaseFamily(sourceDb, plainBackupDb)
        try {
            moveDatabaseFamily(encryptedTempDb, sourceDb)
            deleteDatabaseFamily(plainBackupDb)
        } catch (exception: Exception) {
            moveDatabaseFamily(plainBackupDb, sourceDb)
            throw exception
        }
    }

    private fun trySqlCipherExport(sourceDb: File, encryptedTempDb: File): Boolean {
        SqlCipherDatabase.loadLibs(context)

        val sourcePath = sourceDb.absolutePath
        val tempPath = escapeSql(encryptedTempDb.absolutePath)
        val key = escapeSql(StorageKeyProvider.getInstance(context).getDatabasePassphraseString())

        val legacyDatabase = SqlCipherDatabase.openDatabase(
            sourcePath,
            "",
            null,
            SqlCipherDatabase.OPEN_READWRITE
        )

        return try {
            legacyDatabase.rawExecSQL("ATTACH DATABASE '$tempPath' AS encrypted KEY '$key';")
            legacyDatabase.rawExecSQL("SELECT sqlcipher_export('encrypted');")
            legacyDatabase.rawExecSQL("DETACH DATABASE encrypted;")
            isEncryptedDatabase(encryptedTempDb)
        } catch (exception: Exception) {
            Loggr.debug("StorageMigration") {
                "sqlcipher_export 失败，切换逻辑拷贝迁移：${exception.message}"
            }
            deleteDatabaseFamily(encryptedTempDb)
            false
        } finally {
            legacyDatabase.close()
        }
    }

    private fun migrateDatabaseByLogicalCopy(sourceDb: File, encryptedTempDb: File): Boolean {
        deleteDatabaseFamily(encryptedTempDb)
        SqlCipherDatabase.loadLibs(context)

        val source = SQLiteDatabase.openDatabase(
            sourceDb.absolutePath,
            null,
            SQLiteDatabase.OPEN_READONLY
        )

        val key = StorageKeyProvider.getInstance(context).getDatabasePassphraseString()
        val target = SqlCipherDatabase.openOrCreateDatabase(encryptedTempDb.absolutePath, key, null)

        try {
            target.beginTransaction()
            createSchemaOnTarget(source, target)
            copyEssentialTables(source, target)
            rebuildTaskFtsIndex(target)
            target.setTransactionSuccessful()
        } finally {
            if (target.inTransaction()) {
                target.endTransaction()
            }
            target.close()
            source.close()
        }

        return isEncryptedDatabase(encryptedTempDb)
    }

    private fun createSchemaOnTarget(source: SQLiteDatabase, target: SqlCipherDatabase) {
        val statements = readSchemaStatements(source)

        statements
            .filter { it.type == "table" && it.name in TABLES_TO_CREATE }
            .forEach { target.rawExecSQL(it.sql) }

        statements
            .filter {
                (it.type == "index" || it.type == "trigger") &&
                    it.tableName in TABLES_TO_CREATE
            }
            .forEach { target.rawExecSQL(it.sql) }
    }

    private fun readSchemaStatements(source: SQLiteDatabase): List<SchemaStatement> {
        val sql = """
            SELECT type, name, tbl_name, sql
            FROM sqlite_master
            WHERE sql IS NOT NULL
            ORDER BY
                CASE type
                    WHEN 'table' THEN 0
                    WHEN 'index' THEN 1
                    WHEN 'trigger' THEN 2
                    ELSE 3
                END,
                name
        """.trimIndent()

        source.rawQuery(sql, null).use { cursor ->
            val statements = mutableListOf<SchemaStatement>()
            while (cursor.moveToNext()) {
                val type = cursor.getString(0) ?: continue
                val name = cursor.getString(1) ?: continue
                if (name.startsWith("sqlite_")) continue

                val tableName = cursor.getString(2)
                val createSql = cursor.getString(3) ?: continue
                statements += SchemaStatement(type, name, tableName, createSql)
            }
            return statements
        }
    }

    private fun copyEssentialTables(source: SQLiteDatabase, target: SqlCipherDatabase) {
        TABLES_TO_COPY_DATA.forEach { tableName ->
            copyTableData(source, target, tableName)
        }
    }

    private fun copyTableData(source: SQLiteDatabase, target: SqlCipherDatabase, tableName: String) {
        source.rawQuery("SELECT * FROM `$tableName`", null).use { cursor ->
            val columnNames = cursor.columnNames
            while (cursor.moveToNext()) {
                val values = ContentValues(columnNames.size)
                repeat(columnNames.size) { index ->
                    putCursorValue(values, columnNames[index], cursor, index)
                }
                target.insertOrThrow(tableName, null, values)
            }
        }
    }

    private fun putCursorValue(
        values: ContentValues,
        columnName: String,
        cursor: Cursor,
        index: Int
    ) {
        when (cursor.getType(index)) {
            Cursor.FIELD_TYPE_NULL -> values.putNull(columnName)
            Cursor.FIELD_TYPE_INTEGER -> values.put(columnName, cursor.getLong(index))
            Cursor.FIELD_TYPE_FLOAT -> values.put(columnName, cursor.getDouble(index))
            Cursor.FIELD_TYPE_STRING -> values.put(columnName, cursor.getString(index))
            Cursor.FIELD_TYPE_BLOB -> values.put(columnName, cursor.getBlob(index))
        }
    }

    private fun rebuildTaskFtsIndex(target: SqlCipherDatabase) {
        target.rawExecSQL("INSERT INTO `ToDoTaskFtsDb`(`ToDoTaskFtsDb`) VALUES('rebuild');")
    }

    private suspend fun migrateDataStoreIfNeeded() {
        migrateDataStoreFile(CREDENTIAL_NAME, CredentialPreferenceSerializer)
        migrateDataStoreFile(USER_NAME, UserPreferenceSerializer)
        migrateDataStoreFile(THEME_NAME, ThemePreferenceSerializer)
        migrateDataStoreFile(LANGUAGE_NAME, LanguagePreferenceSerializer)
        migrateDataStoreFile(FONT_SCALE_NAME, FontScalePreferenceSerializer)
        migrateDataStoreFile(TODO_VISIBILITY_NAME, TodoVisibilityPreferenceSerializer)
        migrateDataStoreFile(REMINDER_NAME, ReminderPreferenceSerializer)
        migrateDataStoreFile(APP_DISPLAY_NAME, AppDisplayNamePreferenceSerializer)
        migrateDataStoreFile(AUTH_GATE_NAME, AuthGatePreferenceSerializer)
        migrateDataStoreFile(CLIPBOARD_IMPORT_NAME, ClipboardImportPreferenceSerializer)
    }

    private suspend fun <T> migrateDataStoreFile(
        fileName: String,
        serializer: Serializer<T>
    ) {
        val file = context.dataStoreFile(fileName)
        if (!file.exists()) return

        val payload = file.readBytes()
        if (payload.isEmpty() || StorageAead.isEncrypted(payload)) return

        val parsed = serializer.readFrom(ByteArrayInputStream(payload))
        val encryptedBytes = ByteArrayOutputStream().use { output ->
            serializer.writeTo(parsed, output)
            output.toByteArray()
        }
        writeBytesAtomically(file, encryptedBytes)
    }

    private fun writeBytesAtomically(target: File, bytes: ByteArray) {
        val temp = File(target.absolutePath + ".enc_tmp_write")
        temp.writeBytes(bytes)
        if (target.exists() && !target.delete()) {
            temp.delete()
            throw IllegalStateException("无法替换偏好文件：${target.absolutePath}")
        }
        if (!temp.renameTo(target)) {
            temp.delete()
            throw IllegalStateException("无法写入偏好文件：${target.absolutePath}")
        }
    }

    private fun getDataStoreRequirementResult(): DataStoreRequirementResult {
        val plaintextFiles = mutableListOf<String>()

        DATASTORE_FILES.forEach { fileName ->
            val file = context.dataStoreFile(fileName)
            if (!file.exists() || file.length() == 0L) return@forEach

            val payload = runCatching { file.readBytes() }.getOrElse {
                return DataStoreRequirementResult(MigrationRequirement.UNRECOVERABLE, emptyList())
            }

            if (payload.isNotEmpty() && !StorageAead.isEncrypted(payload)) {
                plaintextFiles += fileName
            }
        }

        return if (plaintextFiles.isEmpty()) {
            DataStoreRequirementResult(MigrationRequirement.NO_MIGRATION, emptyList())
        } else {
            DataStoreRequirementResult(MigrationRequirement.NEEDS_MIGRATION, plaintextFiles)
        }
    }

    private fun verifyDatabaseRowCounts(sourceDb: File, encryptedDb: File) {
        val tableCountsSource = readRowCountsFromPlainDb(sourceDb)
        val tableCountsEncrypted = readRowCountsFromEncryptedDb(encryptedDb)

        if (tableCountsSource != tableCountsEncrypted) {
            throw IllegalStateException("数据库加密迁移校验失败，表行数不一致。")
        }
    }

    private fun readRowCountsFromPlainDb(databaseFile: File): Map<String, Long> {
        val database = SQLiteDatabase.openDatabase(
            databaseFile.absolutePath,
            null,
            SQLiteDatabase.OPEN_READONLY
        )

        return try {
            TABLES_TO_VERIFY.associateWith { tableName ->
                queryRowCount(database, tableName)
            }
        } finally {
            database.close()
        }
    }

    private fun readRowCountsFromEncryptedDb(databaseFile: File): Map<String, Long> {
        val key = StorageKeyProvider.getInstance(context).getDatabasePassphraseString()
        val database = SqlCipherDatabase.openDatabase(
            databaseFile.absolutePath,
            key,
            null,
            SqlCipherDatabase.OPEN_READONLY
        )

        return try {
            TABLES_TO_VERIFY.associateWith { tableName ->
                queryRowCount(database, tableName)
            }
        } finally {
            database.close()
        }
    }

    private fun queryRowCount(database: SQLiteDatabase, tableName: String): Long {
        database.rawQuery("SELECT COUNT(*) FROM `$tableName`", null).use { cursor ->
            if (cursor.moveToFirst()) return cursor.getLong(0)
        }
        return 0
    }

    private fun queryRowCount(database: SqlCipherDatabase, tableName: String): Long {
        database.rawQuery("SELECT COUNT(*) FROM `$tableName`", emptyArray()).use { cursor ->
            if (cursor.moveToFirst()) return cursor.getLong(0)
        }
        return 0
    }

    private fun recoverOrCleanupHalfMigratedState(
        sourceDb: File,
        encryptedTempDb: File,
        plainBackupDb: File
    ) {
        deleteDatabaseFamily(encryptedTempDb)

        if (!plainBackupDb.exists()) return

        when {
            !sourceDb.exists() && isPlaintextDatabase(plainBackupDb) -> {
                moveDatabaseFamily(plainBackupDb, sourceDb)
            }

            sourceDb.exists() && isEncryptedDatabase(sourceDb) -> {
                deleteDatabaseFamily(plainBackupDb)
            }

            sourceDb.exists() && isPlaintextDatabase(sourceDb) -> {
                deleteDatabaseFamily(plainBackupDb)
            }
        }
    }

    private fun isPlaintextDatabase(databaseFile: File): Boolean {
        if (!databaseFile.exists() || databaseFile.length() < SQLITE_HEADER_SIZE) return false

        val headerBytes = ByteArray(SQLITE_HEADER_SIZE)
        FileInputStream(databaseFile).use { input ->
            val readCount = input.read(headerBytes)
            if (readCount < SQLITE_HEADER_SIZE) return false
        }

        val signature = String(headerBytes, StandardCharsets.US_ASCII)
        return signature == SQLITE_HEADER_SIGNATURE
    }

    private fun isEncryptedDatabase(databaseFile: File): Boolean {
        if (!databaseFile.exists()) return false

        return runCatching {
            SqlCipherDatabase.loadLibs(context)
            val key = StorageKeyProvider.getInstance(context).getDatabasePassphraseString()
            SqlCipherDatabase.openDatabase(
                databaseFile.absolutePath,
                key,
                null,
                SqlCipherDatabase.OPEN_READONLY
            ).use { }
        }.isSuccess
    }

    private fun moveDatabaseFamily(sourceBase: File, targetBase: File) {
        DB_SUFFIXES.forEach { suffix ->
            val source = File(sourceBase.absolutePath + suffix)
            if (!source.exists()) return@forEach

            val target = File(targetBase.absolutePath + suffix)
            if (target.exists() && !target.delete()) {
                throw IllegalStateException("无法删除旧数据库文件：${target.absolutePath}")
            }

            if (!source.renameTo(target)) {
                throw IllegalStateException("无法移动数据库文件：${source.absolutePath}")
            }
        }
    }

    private fun deleteDatabaseFamily(baseFile: File) {
        DB_SUFFIXES.forEach { suffix ->
            val target = File(baseFile.absolutePath + suffix)
            if (target.exists()) {
                target.delete()
            }
        }
    }

    private fun escapeSql(raw: String): String = raw.replace("'", "''")

    private data class SchemaStatement(
        val type: String,
        val name: String,
        val tableName: String?,
        val sql: String
    )

    private data class DataStoreRequirementResult(
        val requirement: MigrationRequirement,
        val plaintextFiles: List<String>
    )

    companion object {
        private const val PREF_NAME = "storage-encryption-migration"
        private const val KEY_MIGRATION_VERSION = "migration_version"
        private const val MIGRATION_VERSION = 1
        private const val SQLITE_HEADER_SIZE = 16
        private const val SQLITE_HEADER_SIGNATURE = "SQLite format 3\u0000"

        private const val CREDENTIAL_NAME = "credential-preference.pb"
        private const val USER_NAME = "user-preference.pb"
        private const val THEME_NAME = "theme-preference.pb"
        private const val LANGUAGE_NAME = "language-preference.pb"
        private const val FONT_SCALE_NAME = "font-scale-preference.pb"
        private const val TODO_VISIBILITY_NAME = "todo-visibility-preference.pb"
        private const val REMINDER_NAME = "reminder-preference.pb"
        private const val APP_DISPLAY_NAME = "app-display-name-preference.pb"
        private const val AUTH_GATE_NAME = "auth-gate-preference.pb"
        private const val CLIPBOARD_IMPORT_NAME = "clipboard-import-preference.pb"
        private val DATASTORE_FILES = listOf(
            CREDENTIAL_NAME,
            USER_NAME,
            THEME_NAME,
            LANGUAGE_NAME,
            FONT_SCALE_NAME,
            TODO_VISIBILITY_NAME,
            REMINDER_NAME,
            APP_DISPLAY_NAME,
            AUTH_GATE_NAME,
            CLIPBOARD_IMPORT_NAME
        )

        private val DB_SUFFIXES = listOf("", "-wal", "-shm", "-journal")
        private val TABLES_TO_CREATE = setOf(
            "ToDoGroupDb",
            "ToDoListDb",
            "ToDoTaskDb",
            "ToDoTaskFtsDb",
            "ToDoStepDb",
            "room_master_table"
        )
        private val TABLES_TO_COPY_DATA = listOf(
            "room_master_table",
            "ToDoGroupDb",
            "ToDoListDb",
            "ToDoTaskDb",
            "ToDoStepDb"
        )
        private val TABLES_TO_VERIFY = listOf(
            "ToDoGroupDb",
            "ToDoListDb",
            "ToDoTaskDb",
            "ToDoStepDb",
            "ToDoTaskFtsDb"
        )
    }
}
