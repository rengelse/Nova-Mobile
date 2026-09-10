package no.nova.training.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import no.nova.training.data.model.StoredProgram

class ProgramDatabase(context: Context) : SQLiteOpenHelper(context, "nova_mobile.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE programs (
                export_id TEXT PRIMARY KEY NOT NULL,
                program_id INTEGER NOT NULL,
                name TEXT NOT NULL,
                imported_at INTEGER NOT NULL,
                payload_json TEXT NOT NULL,
                active INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX idx_programs_imported_at ON programs(imported_at DESC)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit

    fun saveProgram(exportId: String, programId: Long, name: String, payloadJson: String, importedAt: Long) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.update("programs", ContentValues().apply { put("active", 0) }, null, null)
            val values = ContentValues().apply {
                put("export_id", exportId)
                put("program_id", programId)
                put("name", name)
                put("imported_at", importedAt)
                put("payload_json", payloadJson)
                put("active", 1)
            }
            db.insertWithOnConflict("programs", null, values, SQLiteDatabase.CONFLICT_REPLACE)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun activeProgramJson(): String? = readableDatabase.rawQuery(
        "SELECT payload_json FROM programs WHERE active = 1 ORDER BY imported_at DESC LIMIT 1", null
    ).use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }

    fun allProgramsJson(): List<String> = readableDatabase.rawQuery(
        "SELECT payload_json FROM programs ORDER BY imported_at DESC", null
    ).use { cursor ->
        buildList { while (cursor.moveToNext()) add(cursor.getString(0)) }
    }

    fun activate(exportId: String) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.update("programs", ContentValues().apply { put("active", 0) }, null, null)
            db.update("programs", ContentValues().apply { put("active", 1) }, "export_id = ?", arrayOf(exportId))
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
    }
}
