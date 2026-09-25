package no.nova.training.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import no.nova.training.data.NovaRepository
import no.nova.training.data.model.FoodBarcodeScan
import no.nova.training.scanner.ProductBarcodeAnalyzer
import no.nova.training.scanner.QrCodeAnalyzer
import no.nova.training.ui.components.NovaCard
import no.nova.training.ui.components.NovaHeader
import no.nova.training.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

@Composable
fun FoodScreen(repository: NovaRepository, padding: PaddingValues) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var scans by remember { mutableStateOf(repository.foodBarcodeScans()) }
    var scannerMode by remember { mutableStateOf<String?>(null) }
    var status by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf("product") }

    fun refresh() { scans = repository.foodBarcodeScans() }
    fun openScanner(mode: String) {
        pendingAction = mode
        scannerMode = mode
    }

    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) scannerMode = pendingAction else status = "Kameratilgang er nødvendig for scanning."
    }
    fun requestScanner(mode: String) {
        pendingAction = mode
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) openScanner(mode)
        else permission.launch(Manifest.permission.CAMERA)
    }

    Column(Modifier.fillMaxSize().padding(padding)) {
        NovaHeader("Mat")
        when (scannerMode) {
            "product" -> ProductScanner(
                modifier = Modifier.weight(1f),
                onBarcode = { ean ->
                    scannerMode = null
                    runCatching { repository.addFoodBarcodeScan(ean) }
                        .onSuccess { status = "Strekkode $ean lagret lokalt."; refresh() }
                        .onFailure { status = it.message ?: "Kunne ikke lagre strekkoden." }
                },
                onClose = { scannerMode = null }
            )
            "transfer" -> QrImportScanner(
                modifier = Modifier.weight(1f),
                onQr = { raw ->
                    scannerMode = null
                    scope.launch {
                        busy = true
                        try {
                            val url = repository.validateIntakeImportUrl(raw)
                            val count = withContext(Dispatchers.IO) { repository.transferPendingFoodBarcodes(url) }
                            status = "$count strekkoder overført til NOVA Desktop."
                            refresh()
                        } catch (e: Exception) { status = e.message ?: "Overføringen feilet." }
                        finally { busy = false }
                    }
                },
                onClose = { scannerMode = null }
            )
            else -> FoodContent(
                scans = scans,
                status = status,
                busy = busy,
                onScan = { requestScanner("product") },
                onTransfer = { requestScanner("transfer") },
                onDelete = { repository.deleteFoodBarcodeScan(it); refresh() },
                onClearSynced = { repository.clearSyncedFoodBarcodeScans(); refresh() }
            )
        }
    }
}

@Composable
private fun FoodContent(
    scans: List<FoodBarcodeScan>, status: String?, busy: Boolean,
    onScan: () -> Unit, onTransfer: () -> Unit, onDelete: (String) -> Unit, onClearSynced: () -> Unit
) {
    val pending = scans.count { it.syncStatus != "synced" }
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        NovaCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = NovaSurface2, shape = RoundedCornerShape(14.dp), modifier = Modifier.size(58.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Restaurant, null, tint = NovaBlue, modifier = Modifier.size(30.dp)) }
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Faktisk matinntak", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                    Text("Skann produktets strekkode. Produktdata hentes først på desktop.", color = NovaMuted, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(14.dp))
            Button(onClick = onScan, modifier = Modifier.fillMaxWidth().height(50.dp)) { Text("Skann produkt") }
        }

        NovaCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Klar for overføring", color = NovaMuted, fontSize = 12.sp)
                    Text("$pending usynkroniserte strekkoder", fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                }
                Icon(Icons.Default.Sync, null, tint = if (pending > 0) NovaBlue else NovaMuted)
            }
            Spacer(Modifier.height(10.dp))
            Button(onClick = onTransfer, enabled = pending > 0 && !busy, modifier = Modifier.fillMaxWidth()) {
                if (busy) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp) else Text("Overfør til desktop")
            }
            Text("Desktop viser en QR-kode kun for selve overføringen. Produktene skannes alltid som vanlig strekkode.", color = NovaMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp))
        }

        if (!status.isNullOrBlank()) Text(status, color = if (status.contains("feil", true)) NovaDanger else NovaGreen, fontSize = 12.sp)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Skannede produkter", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, modifier = Modifier.weight(1f))
            if (scans.any { it.syncStatus == "synced" }) TextButton(onClick = onClearSynced) { Text("Fjern overførte") }
        }

        if (scans.isEmpty()) {
            NovaCard { Text("Ingen produkter skannet ennå.", color = NovaMuted) }
        } else {
            LazyColumn(Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(scans, key = { it.scanId }) { scan -> ScanRow(scan, onDelete) }
            }
        }
    }
}

@Composable
private fun ScanRow(scan: FoodBarcodeScan, onDelete: (String) -> Unit) {
    val formatter = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }
    NovaCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(scan.ean, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                Text(formatter.format(Date(scan.scannedAt)), color = NovaMuted, fontSize = 11.sp)
            }
            if (scan.syncStatus == "synced") {
                Icon(Icons.Default.CheckCircle, "Overført", tint = NovaGreen, modifier = Modifier.size(21.dp))
                Spacer(Modifier.width(8.dp)); Text("Overført", color = NovaGreen, fontSize = 11.sp)
            } else {
                Text(if (scan.syncStatus == "error") "Feil" else "Ikke overført", color = if (scan.syncStatus == "error") NovaDanger else NovaMuted, fontSize = 11.sp)
                IconButton(onClick = { onDelete(scan.scanId) }) { Icon(Icons.Default.DeleteOutline, "Slett", tint = NovaMuted) }
            }
        }
        if (!scan.errorMessage.isNullOrBlank()) Text(scan.errorMessage, color = NovaDanger, fontSize = 11.sp)
    }
}

@Composable
private fun ProductScanner(modifier: Modifier, onBarcode: (String) -> Unit, onClose: () -> Unit) {
    CameraScanner(modifier, "Hold produktets strekkode innenfor feltet", false, onBarcode, onClose)
}

@Composable
private fun QrImportScanner(modifier: Modifier, onQr: (String) -> Unit, onClose: () -> Unit) {
    CameraScanner(modifier, "Skann import-QR fra NOVA Desktop", true, onQr, onClose)
}

@Composable
private fun CameraScanner(modifier: Modifier, hint: String, qr: Boolean, onValue: (String) -> Unit, onClose: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }
    DisposableEffect(Unit) { onDispose { executor.shutdown() } }
    Box(modifier.background(Color.Black)) {
        AndroidView(factory = { ctx ->
            val previewView = PreviewView(ctx).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
            val providerFuture = ProcessCameraProvider.getInstance(ctx)
            providerFuture.addListener({
                val provider = providerFuture.get()
                val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
                val analysis = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
                if (qr) analysis.setAnalyzer(executor, QrCodeAnalyzer { value -> previewView.post { onValue(value) } })
                else analysis.setAnalyzer(executor, ProductBarcodeAnalyzer { value -> previewView.post { onValue(value) } })
                runCatching { provider.unbindAll(); provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis) }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        }, modifier = Modifier.fillMaxSize())
        Surface(color = Color(0xAA08111B), shape = RoundedCornerShape(18.dp), modifier = Modifier.align(Alignment.TopCenter).padding(20.dp)) {
            Text(hint, modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp))
        }
        IconButton(onClick = onClose, modifier = Modifier.align(Alignment.TopEnd).padding(14.dp)) { Icon(Icons.Default.Close, "Lukk", tint = Color.White) }
        Box(Modifier.align(Alignment.Center).then(if (qr) Modifier.size(250.dp) else Modifier.width(310.dp).height(150.dp)).background(Color.Transparent, RoundedCornerShape(24.dp)))
    }
}
