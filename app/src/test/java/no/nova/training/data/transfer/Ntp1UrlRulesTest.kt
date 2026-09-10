package no.nova.training.data.transfer

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class Ntp1UrlRulesTest {
    private val client = TrainingTransferClient(Gson())
    @Test fun acceptsPrivateLanUrl() {
        val url = "http://192.168.1.10:53127/nova/training/send/Abcdefghijkl"
        assertEquals(url, client.validateTransferUrl(url))
    }
    @Test fun rejectsPublicHost() {
        assertThrows(IllegalArgumentException::class.java) { client.validateTransferUrl("http://8.8.8.8:53127/nova/training/send/Abcdefghijkl") }
    }
}
