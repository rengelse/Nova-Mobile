package no.nova.training.data

import android.content.Context
import com.google.gson.Gson
import no.nova.training.data.exercises.ExerciseCatalog
import no.nova.training.data.local.ProgramDatabase
import no.nova.training.data.model.Ntp1Payload
import no.nova.training.data.model.StoredProgram
import no.nova.training.data.transfer.Ntp1Validator
import no.nova.training.data.transfer.TrainingTransferClient
import no.nova.training.data.transfer.IntakeTransferClient
import no.nova.training.data.model.FoodBarcodeScan
import java.util.UUID

class NovaRepository(context: Context) {
    val gson = Gson()
    val catalog = ExerciseCatalog(context, gson)
    private val database = ProgramDatabase(context)
    private val validator = Ntp1Validator(catalog)
    private val transferClient = TrainingTransferClient(gson)
    private val intakeTransferClient = IntakeTransferClient(gson)

    fun activeProgram(): StoredProgram? = database.activeProgramJson()?.let(::decodeStored)
    fun allPrograms(): List<StoredProgram> = database.allProgramsJson().mapNotNull { runCatching { decodeStored(it) }.getOrNull() }
    fun activate(exportId: String) = database.activate(exportId)

    fun validateUrl(raw: String): String = transferClient.validateTransferUrl(raw)
    fun fetch(url: String): Pair<Ntp1Payload, String> = transferClient.fetch(url)
    fun validate(payload: Ntp1Payload): Ntp1Payload = validator.validate(payload)

    fun saveValidated(payload: Ntp1Payload, originalJson: String) {
        val exportId = payload.exportId ?: error("Programmet mangler exportId.")
        val program = payload.program ?: error("Programmet mangler metadata.")
        val programId = program.id ?: error("Programmet mangler program-ID.")
        database.saveProgram(exportId, programId, program.name?.preferred().orEmpty().ifBlank { "Treningsprogram" }, originalJson, System.currentTimeMillis())
    }

    fun confirm(url: String, exportId: String) = transferClient.confirm(url, exportId)

    fun addFoodBarcodeScan(ean: String): FoodBarcodeScan {
        val clean = ean.filter(Char::isDigit)
        require(clean.length in setOf(8, 12, 13)) { "Ugyldig strekkode." }
        val scan = FoodBarcodeScan(UUID.randomUUID().toString(), clean, System.currentTimeMillis(), "pending")
        database.insertBarcodeScan(scan)
        return scan
    }
    fun foodBarcodeScans(): List<FoodBarcodeScan> = database.listBarcodeScans()
    fun pendingFoodBarcodeScans(): List<FoodBarcodeScan> = database.pendingBarcodeScans()
    fun deleteFoodBarcodeScan(scanId: String) = database.deleteBarcodeScan(scanId)
    fun clearSyncedFoodBarcodeScans() = database.clearSyncedBarcodeScans()
    fun validateIntakeImportUrl(raw: String): String = intakeTransferClient.validateUrl(raw)
    fun transferPendingFoodBarcodes(url: String): Int {
        val scans = database.pendingBarcodeScans()
        if (scans.isEmpty()) return 0
        return try {
            val result = intakeTransferClient.transfer(url, scans)
            database.markBarcodeScansSynced(scans.map { it.scanId }, System.currentTimeMillis())
            result.accepted + result.duplicateCount
        } catch (error: Exception) {
            database.markBarcodeScansError(scans.map { it.scanId }, error.message ?: "Overføring feilet")
            throw error
        }
    }

    private fun decodeStored(json: String): StoredProgram {
        val payload = gson.fromJson(json, Ntp1Payload::class.java)
        return StoredProgram(
            exportId = payload.exportId.orEmpty(),
            programId = payload.program?.id ?: 0,
            name = payload.program?.name?.preferred().orEmpty().ifBlank { "Treningsprogram" },
            importedAt = 0L,
            payload = payload
        )
    }
}
