package no.nova.training.data.transfer

import com.google.gson.Gson
import no.nova.training.data.model.FoodBarcodeScan
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URI
import java.util.concurrent.TimeUnit

data class IntakeTransferResult(val accepted: Int, val duplicateCount: Int)

class IntakeTransferClient(private val gson: Gson) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .callTimeout(40, TimeUnit.SECONDS)
        .build()

    companion object { const val MAX_SCANS = 250 }

    fun validateUrl(raw: String): String {
        val uri = URI(raw.trim())
        require(uri.scheme == "http") { "NOVA-overføring må bruke lokal HTTP." }
        require(uri.userInfo == null && uri.fragment == null && uri.query == null) { "QR-adressen har ugyldig format." }
        val host = uri.host ?: error("QR-adressen mangler nettverksadresse.")
        require(isPrivateIpv4(host)) { "QR-adressen peker ikke til en lokal LAN-adresse." }
        require(Regex("^/nova/intake/import/[A-Za-z0-9_-]{12,80}$").matches(uri.path.orEmpty())) { "QR-adressen er ikke en gyldig NOVA-matimport." }
        return uri.toASCIIString()
    }

    fun transfer(url: String, scans: List<FoodBarcodeScan>): IntakeTransferResult {
        require(scans.isNotEmpty()) { "Ingen nye strekkoder å overføre." }
        require(scans.size <= MAX_SCANS) { "For mange strekkoder i én overføring." }
        val body = gson.toJson(mapOf(
            "protocol" to "NOVA_INTAKE_1",
            "scans" to scans.map { mapOf("scanId" to it.scanId, "ean" to it.ean, "scannedAt" to it.scannedAt) }
        ))
        val request = Request.Builder().url(url).header("Cache-Control", "no-store")
            .post(body.toRequestBody("application/json; charset=utf-8".toMediaType())).build()
        client.newCall(request).execute().use { response ->
            val text = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw IllegalStateException(serverError(response.code, text))
            val payload = runCatching { gson.fromJson(text, Map::class.java) }.getOrNull().orEmpty()
            val accepted = (payload["accepted"] as? Number)?.toInt() ?: scans.size
            val duplicates = (payload["duplicates"] as? Number)?.toInt() ?: 0
            return IntakeTransferResult(accepted, duplicates)
        }
    }

    private fun serverError(code: Int, text: String?): String = when (code) {
        404 -> "Import-session finnes ikke eller QR-koden er ugyldig."
        410 -> "Import-session er utløpt. Lag en ny QR-kode på PC-en."
        413 -> "For mange strekkoder i overføringen."
        else -> "Overføringen feilet på PC-en (HTTP $code).${text?.takeIf { it.isNotBlank() }?.let { " $it" } ?: ""}"
    }

    private fun isPrivateIpv4(host: String): Boolean {
        val parts = host.split('.').mapNotNull { it.toIntOrNull() }
        if (parts.size != 4 || parts.any { it !in 0..255 }) return false
        val (a,b,_,_) = parts
        return a == 10 || (a == 172 && b in 16..31) || (a == 192 && b == 168) || (a == 169 && b == 254)
    }
}
