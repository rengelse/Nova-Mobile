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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import no.nova.training.data.NovaRepository
import no.nova.training.data.transfer.TransferState
import no.nova.training.scanner.QrCodeAnalyzer
import no.nova.training.ui.components.NovaCard
import no.nova.training.ui.components.NovaHeader
import no.nova.training.ui.theme.*
import java.util.concurrent.Executors

@Composable fun TransferScreen(repository: NovaRepository, padding: PaddingValues, onStored: () -> Unit, onOpenProgram: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var scanning by remember { mutableStateOf(false) }
    var state by remember { mutableStateOf<TransferState>(TransferState.Idle) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> scanning = granted }

    fun receive(rawUrl: String) {
        scanning = false
        scope.launch {
            try {
                val url = repository.validateUrl(rawUrl)
                state = TransferState.Fetching
                val (payload, json) = withContext(Dispatchers.IO) { repository.fetch(url) }
                state = TransferState.Validating
                withContext(Dispatchers.Default) { repository.validate(payload) }
                state = TransferState.Saving
                withContext(Dispatchers.IO) { repository.saveValidated(payload, json) }
                onStored()
                state = TransferState.Confirming
                withContext(Dispatchers.IO) { repository.confirm(url, payload.exportId.orEmpty()) }
                val weeks = payload.weeks.orEmpty().size
                val days = payload.weeks.orEmpty().sumOf { it.days.orEmpty().size }
                state = TransferState.Success(payload.program?.name?.preferred().orEmpty().ifBlank { "Treningsprogram" }, weeks, days)
            } catch (e: Exception) { state = TransferState.Error(e.message ?: "Overføringen feilet.") }
        }
    }

    Column(Modifier.fillMaxSize().padding(padding)) {
        NovaHeader("Overfør program")
        if (scanning) {
            QrScannerView(Modifier.weight(1f), onQr = ::receive, onClose = { scanning = false })
        } else {
            Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
                NovaCard(onClick = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) scanning = true else permission.launch(Manifest.permission.CAMERA)
                }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = NovaSurface2, shape = RoundedCornerShape(14.dp), modifier = Modifier.size(62.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.QrCodeScanner, null, tint = NovaText, modifier = Modifier.size(34.dp)) } }
                        Spacer(Modifier.width(14.dp)); Column(Modifier.weight(1f)) { Text("Skann QR-kode", fontWeight = FontWeight.SemiBold, fontSize = 17.sp); Text("Skann koden som vises på PC-en for å hente programmet.", color = NovaMuted, fontSize = 12.sp) }; Text("›", fontSize = 28.sp)
                    }
                }
                Row(Modifier.padding(vertical = 14.dp, horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Wifi, null, tint = NovaBlue); Spacer(Modifier.width(8.dp)); Text("Telefon og PC må være på samme Wi-Fi/LAN.", color = NovaMuted, fontSize = 12.sp) }
                TransferProgress(state)
                if (state is TransferState.Success) {
                    val s = state as TransferState.Success
                    Spacer(Modifier.height(12.dp))
                    NovaCard {
                        Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.CheckCircle, null, tint = NovaGreen, modifier = Modifier.size(42.dp)); Spacer(Modifier.width(12.dp)); Column { Text("Program lagret", color = NovaGreen, fontWeight = FontWeight.Bold, fontSize = 20.sp); Text("${s.programName}\n${s.weeks} uker · ${s.days} treningsdager", color = NovaMuted) } }
                        Spacer(Modifier.height(14.dp)); Button(onClick = onOpenProgram, modifier = Modifier.fillMaxWidth().height(50.dp)) { Text("Åpne program") }
                    }
                }
                if (state is TransferState.Error) {
                    Spacer(Modifier.height(12.dp)); Text((state as TransferState.Error).message, color = NovaDanger, fontSize = 13.sp)
                    TextButton(onClick = { state = TransferState.Idle }) { Text("Prøv igjen") }
                }
            }
        }
    }
}

@Composable private fun TransferProgress(state: TransferState) {
    fun rank(s: TransferState): Int = when (s) { TransferState.Idle -> 0; TransferState.Fetching -> 1; TransferState.Validating -> 2; TransferState.Saving -> 3; TransferState.Confirming -> 4; is TransferState.Success -> 5; is TransferState.Error -> -1 }
    val r = rank(state)
    NovaCard {
        listOf("Henter program" to 1, "Validerer" to 2, "Lagrer lokalt" to 3, "Bekrefter til PC" to 4).forEach { (label, step) ->
            Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                when {
                    r > step -> Icon(Icons.Default.CheckCircle, null, tint = NovaGreen, modifier = Modifier.size(22.dp))
                    r == step -> CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    else -> Surface(color = NovaBorder, shape = RoundedCornerShape(100), modifier = Modifier.size(22.dp)) {}
                }
                Spacer(Modifier.width(11.dp)); Text(if (r == step) "$label …" else label, color = if (r >= step) NovaText else NovaMuted)
            }
        }
    }
}

@Composable private fun QrScannerView(modifier: Modifier, onQr: (String) -> Unit, onClose: () -> Unit) {
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
                val analysis = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build().also { it.setAnalyzer(executor, QrCodeAnalyzer { value -> previewView.post { onQr(value) } }) }
                runCatching { provider.unbindAll(); provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis) }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        }, modifier = Modifier.fillMaxSize())
        Surface(color = Color(0xAA08111B), shape = RoundedCornerShape(18.dp), modifier = Modifier.align(Alignment.TopCenter).padding(20.dp)) { Text("Hold NOVA QR-koden innenfor kameraet", modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)) }
        IconButton(onClick = onClose, modifier = Modifier.align(Alignment.TopEnd).padding(14.dp)) { Icon(Icons.Default.Close, "Lukk", tint = Color.White) }
        Box(Modifier.align(Alignment.Center).size(250.dp).background(Color.Transparent, RoundedCornerShape(24.dp)))
    }
}
