package no.nova.training.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import no.nova.training.data.model.FoodBarcodeScan

class ProgramDatabase(context: Context) : SQLiteOpenHelper(context, "nova_mobile.db", null, 2) {
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
        createBarcodeScans(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) createBarcodeScans(db)
    }

    private fun createBarcodeScans(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS food_barcode_scans (
                scan_id TEXT PRIMARY KEY NOT NULL,
                ean TEXT NOT NULL,
                scanned_at INTEGER NOT NULL,
                sync_status TEXT NOT NULL DEFAULT 'pending',
                synced_at INTEGER,
                error_message TEXT
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_food_barcode_scans_status ON food_barcode_scans(sync_status, scanned_at DESC)")
    }

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
        } finally { db.endTransaction() }
    }

    fun activeProgramJson(): String? = readableDatabase.rawQuery(
        "SELECT payload_json FROM programs WHERE active = 1 ORDER BY imported_at DESC LIMIT 1", null
    ).use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }

    fun allProgramsJson(): List<String> = readableDatabase.rawQuery(
        "SELECT payload_json FROM programs ORDER BY imported_at DESC", null
    ).use { cursor -> buildList { while (cursor.moveToNext()) add(cursor.getString(0)) } }

    fun activate(exportId: String) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.update("programs", ContentValues().apply { put("active", 0) }, null, null)
            db.update("programs", ContentValues().apply { put("active", 1) }, "export_id = ?", arrayOf(exportId))
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
    }

    fun insertBarcodeScan(scan: FoodBarcodeScan) {
        val values = ContentValues().apply {
            put("scan_id", scan.scanId)
            put("ean", scan.ean)
            put("scanned_at", scan.scannedAt)
            put("sync_status", scan.syncStatus)
            if (scan.syncedAt != null) put("synced_at", scan.syncedAt) else putNull("synced_at")
            if (scan.errorMessage != null) put("error_message", scan.errorMessage) else putNull("error_message")
        }
        writableDatabase.insertOrThrow("food_barcode_scans", null, values)
    }

    fun listBarcodeScans(): List<FoodBarcodeScan> = readableDatabase.rawQuery(
        "SELECT scan_id, ean, scanned_at, sync_status, synced_at, error_message FROM food_barcode_scans ORDER BY scanned_at DESC", null
    ).use { cursor ->
        buildList {
            while (cursor.moveToNext()) {
                add(FoodBarcodeScan(
                    scanId = cursor.getString(0),
                    ean = cursor.getString(1),
                    scannedAt = cursor.getLong(2),
                    syncStatus = cursor.getString(3),
                    syncedAt = if (cursor.isNull(4)) null else cursor.getLong(4),
                    errorMessage = if (cursor.isNull(5)) null else cursor.getString(5)
                ))
            }
        }
    }

    fun pendingBarcodeScans(): List<FoodBarcodeScan> = readableDatabase.rawQuery(
        "SELECT scan_id, ean, scanned_at, sync_status, synced_at, error_message FROM food_barcode_scans WHERE sync_status != 'synced' ORDER BY scanned_at ASC", null
    ).use { cursor ->
        buildList {
            while (cursor.moveToNext()) {
                add(FoodBarcodeScan(
                    scanId = cursor.getString(0), ean = cursor.getString(1), scannedAt = cursor.getLong(2),
                    syncStatus = cursor.getString(3), syncedAt = if (cursor.isNull(4)) null else cursor.getLong(4),
                    errorMessage = if (cursor.isNull(5)) null else cursor.getString(5)
                ))
            }
        }
    }

    fun markBarcodeScansSynced(scanIds: List<String>, syncedAt: Long) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val stmt = db.compileStatement("UPDATE food_barcode_scans SET sync_status='synced', synced_at=?, error_message=NULL WHERE scan_id=?")
            scanIds.forEach { id -> stmt.clearBindings(); stmt.bindLong(1, syncedAt); stmt.bindString(2, id); stmt.executeUpdateDelete() }
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
    }

    fun markBarcodeScansError(scanIds: List<String>, message: String) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val stmt = db.compileStatement("UPDATE food_barcode_scans SET sync_status='error', error_message=? WHERE scan_id=?")
            scanIds.forEach { id -> stmt.clearBindings(); stmt.bindString(1, message); stmt.bindString(2, id); stmt.executeUpdateDelete() }
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
    }

    fun deleteBarcodeScan(scanId: String) {
        writableDatabase.delete("food_barcode_scans", "scan_id=?", arrayOf(scanId))
    }

    fun clearSyncedBarcodeScans() {
        writableDatabase.delete("food_barcode_scans", "sync_status='synced'", null)
    }
}
