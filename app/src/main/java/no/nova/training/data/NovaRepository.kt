package no.nova.training.data

import android.content.Context
import com.google.gson.Gson
import no.nova.training.data.exercises.ExerciseCatalog
import no.nova.training.data.local.ProgramDatabase
import no.nova.training.data.model.Ntp1Payload
import no.nova.training.data.model.StoredProgram
import no.nova.training.data.transfer.Ntp1Validator
import no.nova.training.data.transfer.TrainingTransferClient
import no.nova.training.data.transfer.BarcodeTransferClient

class NovaRepository(context: Context) {
    val gson = Gson()
    val catalog = ExerciseCatalog(context, gson)
    private val database = ProgramDatabase(context)
    private val validator = Ntp1Validator(catalog)
    private val transferClient = TrainingTransferClient(gson)
    private val barcodeClient = BarcodeTransferClient(gson)

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

    fun isBarcodePairingUrl(raw: String): Boolean = barcodeClient.isBarcodePairingUrl(raw)
    fun validateBarcodeUrl(raw: String): String = barcodeClient.validateBarcodeUrl(raw)
    fun pairBarcode(url: String) = barcodeClient.pair(url)
    fun sendBarcode(url: String, ean: String) = barcodeClient.sendBarcode(url, ean)

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
