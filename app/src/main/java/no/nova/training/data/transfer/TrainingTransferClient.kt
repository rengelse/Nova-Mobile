package no.nova.training.data.transfer

import com.google.gson.Gson
import no.nova.training.data.model.Ntp1Payload
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URI
import java.util.concurrent.TimeUnit

class TrainingTransferClient(private val gson: Gson) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .callTimeout(20, TimeUnit.SECONDS)
        .build()

    companion object { const val MAX_PAYLOAD_BYTES = 5L * 1024L * 1024L }

    fun validateTransferUrl(raw: String): String {
        val uri = URI(raw.trim())
        require(uri.scheme == "http") { "NOVA-overføring må bruke lokal HTTP." }
        require(uri.userInfo == null && uri.fragment == null && uri.query == null) { "QR-adressen har ugyldig format." }
        val host = uri.host ?: error("QR-adressen mangler nettverksadresse.")
        require(isPrivateIpv4(host)) { "QR-adressen peker ikke til en lokal LAN-adresse." }
        require(Regex("^/nova/training/send/[A-Za-z0-9_-]{12,80}$").matches(uri.path.orEmpty())) { "QR-adressen er ikke en gyldig NOVA-treningssession." }
        return uri.toASCIIString()
    }

    fun fetch(url: String): Pair<Ntp1Payload, String> {
        val request = Request.Builder().url(url).header("Cache-Control", "no-store").get().build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IllegalStateException(serverError(response.code, response.body?.string()))
            val body = response.body ?: throw IllegalStateException("PC-en returnerte ingen programdata.")
            val length = body.contentLength()
            if (length > MAX_PAYLOAD_BYTES) throw IllegalStateException("Programmet er større enn tillatt størrelse.")
            val bytes = body.bytes()
            if (bytes.size > MAX_PAYLOAD_BYTES) throw IllegalStateException("Programmet er større enn tillatt størrelse.")
            val json = bytes.toString(Charsets.UTF_8)
            return gson.fromJson(json, Ntp1Payload::class.java) to json
        }
    }

    fun confirm(url: String, exportId: String) {
        val json = gson.toJson(mapOf("exportId" to exportId))
        val request = Request.Builder()
            .url("$url/confirm")
            .header("Cache-Control", "no-store")
            .post(json.toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IllegalStateException(serverError(response.code, response.body?.string()))
        }
    }

    private fun serverError(code: Int, text: String?): String = when (code) {
        404 -> "Overføringen finnes ikke eller QR-koden er ugyldig."
        409 -> "PC-en avviste bekreftelsen for dette programmet."
        410 -> "Overføringen er utløpt eller allerede brukt. Lag en ny QR-kode på PC-en."
        else -> "Overføringen feilet på PC-en (HTTP $code).${text?.takeIf { it.isNotBlank() }?.let { " $it" } ?: ""}"
    }

    private fun isPrivateIpv4(host: String): Boolean {
        val parts = host.split('.').mapNotNull { it.toIntOrNull() }
        if (parts.size != 4 || parts.any { it !in 0..255 }) return false
        val (a,b,_,_) = parts
        return a == 10 || (a == 172 && b in 16..31) || (a == 192 && b == 168) || (a == 169 && b == 254)
    }
}
