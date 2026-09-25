package no.nova.training.data.transfer

import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URI
import java.util.concurrent.TimeUnit

class BarcodeTransferClient(private val gson: Gson) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .callTimeout(18, TimeUnit.SECONDS)
        .build()

    fun isBarcodePairingUrl(raw: String): Boolean = runCatching {
        val uri = URI(raw.trim())
        uri.scheme == "http" && isPrivateIpv4(uri.host ?: "") && Regex("^/nova/intake/barcode/[A-Za-z0-9_-]{12,80}$").matches(uri.path.orEmpty())
    }.getOrDefault(false)

    fun validateBarcodeUrl(raw: String): String {
        val uri = URI(raw.trim())
        require(uri.scheme == "http") { "NOVA-skanning må bruke lokal HTTP." }
        require(uri.userInfo == null && uri.fragment == null && uri.query == null) { "QR-adressen har ugyldig format." }
        val host = uri.host ?: error("QR-adressen mangler nettverksadresse.")
        require(isPrivateIpv4(host)) { "QR-adressen peker ikke til en lokal LAN-adresse." }
        require(Regex("^/nova/intake/barcode/[A-Za-z0-9_-]{12,80}$").matches(uri.path.orEmpty())) { "QR-adressen er ikke en gyldig NOVA-strekkode-session." }
        return uri.toASCIIString()
    }

    fun pair(url: String) {
        val request = Request.Builder().url(url).header("Cache-Control", "no-store").get().build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IllegalStateException(serverError(response.code))
            val body = response.body?.string().orEmpty()
            if (!body.contains("NOVA_BARCODE_1")) throw IllegalStateException("PC-en svarte med ukjent strekkodeprotokoll.")
        }
    }

    fun sendBarcode(url: String, ean: String) {
        val clean = ean.filter(Char::isDigit)
        require(clean.length in setOf(8,12,13)) { "Ugyldig strekkode." }
        val json = gson.toJson(mapOf("type" to "barcode", "ean" to clean))
        val request = Request.Builder()
            .url(url)
            .header("Cache-Control", "no-store")
            .post(json.toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IllegalStateException(serverError(response.code))
        }
    }

    private fun serverError(code: Int): String = when (code) {
        404 -> "Strekkode-session finnes ikke eller QR-koden er ugyldig."
        410 -> "Strekkode-session er utløpt. Lag en ny QR-kode på PC-en."
        else -> "Strekkodeoverføringen feilet på PC-en (HTTP $code)."
    }

    private fun isPrivateIpv4(host: String): Boolean {
        val parts = host.split('.').mapNotNull { it.toIntOrNull() }
        if (parts.size != 4 || parts.any { it !in 0..255 }) return false
        val (a,b,_,_) = parts
        return a == 10 || (a == 172 && b in 16..31) || (a == 192 && b == 168) || (a == 169 && b == 254)
    }
}
