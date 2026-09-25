package no.nova.training.data.transfer

import com.google.gson.Gson
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BarcodeUrlRulesTest {
    private val client = BarcodeTransferClient(Gson())

    @Test fun acceptsPrivateLanBarcodeSession() {
        assertTrue(client.isBarcodePairingUrl("http://192.168.1.44:39211/nova/intake/barcode/abcdefghijklmnop"))
    }

    @Test fun rejectsTrainingAndPublicUrls() {
        assertFalse(client.isBarcodePairingUrl("http://192.168.1.44:39211/nova/training/send/abcdefghijkl"))
        assertFalse(client.isBarcodePairingUrl("https://example.com/nova/intake/barcode/abcdefghijklmnop"))
    }
}
