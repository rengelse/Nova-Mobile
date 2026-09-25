package no.nova.training.data.model

data class FoodBarcodeScan(
    val scanId: String,
    val ean: String,
    val scannedAt: Long,
    val syncStatus: String,
    val syncedAt: Long? = null,
    val errorMessage: String? = null
)
