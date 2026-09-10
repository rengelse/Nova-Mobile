package no.nova.training.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import no.nova.training.data.exercises.ExerciseCatalog
import no.nova.training.data.model.ProgramExercise
import no.nova.training.data.model.StoredProgram
import no.nova.training.ui.components.*
import no.nova.training.ui.theme.*

@Composable fun ProgramScreen(program: StoredProgram?, catalog: ExerciseCatalog, padding: PaddingValues, onExercise: (ProgramExercise) -> Unit, onTransfer: () -> Unit) {
    if (program == null) {
        Column(Modifier.fillMaxSize().padding(padding).padding(20.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text("N O V A", fontSize = 28.sp, fontWeight = FontWeight.Light)
            Spacer(Modifier.height(20.dp))
            Text("Ingen program på telefonen", fontSize = 21.sp, fontWeight = FontWeight.SemiBold)
            Text("Skann QR-koden fra NOVA Desktop for å hente første treningsprogram.", color = NovaMuted, modifier = Modifier.padding(top = 8.dp, bottom = 22.dp))
            Button(onClick = onTransfer) { Icon(Icons.Default.QrCodeScanner, null); Spacer(Modifier.width(8.dp)); Text("Overfør program") }
        }
        return
    }
    val payload = program.payload
    val weeks = payload.weeks.orEmpty().sortedBy { it.weekNumber }
    var weekNo by remember(program.exportId) { mutableIntStateOf(payload.program?.currentPosition?.week?.coerceIn(1, weeks.size.coerceAtLeast(1)) ?: 1) }
    val week = weeks.firstOrNull { it.weekNumber == weekNo } ?: weeks.firstOrNull()
    val days = week?.days.orEmpty().sortedBy { it.dayNumber }
    var dayNo by remember(program.exportId, weekNo) { mutableIntStateOf(payload.program?.currentPosition?.day?.coerceIn(1, days.size.coerceAtLeast(1)) ?: 1) }
    val day = days.firstOrNull { it.dayNumber == dayNo } ?: days.firstOrNull()

    LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = 18.dp)) {
        item {
            NovaHeader(trailing = {
                Surface(color = NovaSurface2, shape = MaterialTheme.shapes.large) {
                    Row(Modifier.padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudOff, null, tint = NovaGreen, modifier = Modifier.size(15.dp)); Spacer(Modifier.width(5.dp)); Text("Offline", color = NovaMuted, fontSize = 12.sp)
                    }
                }
            })
            Column(Modifier.padding(horizontal = 20.dp)) {
                Text(program.name, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                Text("Uke ${week?.weekNumber ?: 1} av ${weeks.size}", color = NovaMuted, fontSize = 14.sp)
                Spacer(Modifier.height(15.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    weeks.take(4).forEach { w -> SegmentedPill("Uke ${w.weekNumber}", w.weekNumber == week?.weekNumber, { weekNo = w.weekNumber ?: 1 }, Modifier.weight(1f)) }
                }
                if (weeks.size > 4) {
                    Text("${weeks.size} uker totalt · sveip/ukevelger utvides i neste UI-pass", color = NovaMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 6.dp))
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    days.forEach { d -> SegmentedPill("Dag ${d.dayNumber}", d.dayNumber == day?.dayNumber, { dayNo = d.dayNumber ?: 1 }, Modifier.weight(1f)) }
                }
                Spacer(Modifier.height(14.dp))
            }
        }
        items(day?.exercises.orEmpty()) { exercise ->
            val details = catalog.byId(exercise.exerciseId)
            NovaCard(Modifier.padding(horizontal = 20.dp, vertical = 5.dp), onClick = { onExercise(exercise) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = NovaSurface2, shape = MaterialTheme.shapes.medium, modifier = Modifier.size(92.dp)) {
                        AsyncImage(model = catalog.framePath(exercise.artworkId.orEmpty(), 2), contentDescription = exercise.name?.preferred(), contentScale = ContentScale.Fit)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(exercise.name?.preferred().orEmpty().ifBlank { details?.names?.preferred().orEmpty() }, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                        Text("${exercise.sets} × ${exercise.reps}", color = NovaText, modifier = Modifier.padding(top = 5.dp))
                        Text(exercise.plannedLoadKg?.let { formatKg(it) } ?: "Egen kroppsvekt", color = NovaMuted, fontSize = 13.sp)
                        val muscles = (exercise.primaryMuscles ?: details?.primary).orEmpty().take(3)
                        if (muscles.isNotEmpty()) Text(muscles.joinToString(" · "), color = NovaBlue, fontSize = 11.sp, modifier = Modifier.padding(top = 5.dp))
                    }
                    Text("›", color = NovaText, fontSize = 26.sp)
                }
            }
        }
    }
}

private fun formatKg(value: Double): String = if (value % 1.0 == 0.0) "${value.toInt()} kg" else String.format(java.util.Locale("nb", "NO"), "%.1f kg", value)
