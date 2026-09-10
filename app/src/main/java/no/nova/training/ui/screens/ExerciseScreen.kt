package no.nova.training.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.PaddingValues
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.nova.training.data.exercises.ExerciseCatalog
import no.nova.training.data.model.ProgramExercise
import no.nova.training.ui.components.ExerciseAnimation
import no.nova.training.ui.components.NovaCard
import no.nova.training.ui.theme.*

@Composable fun ExerciseScreen(exercise: ProgramExercise, catalog: ExerciseCatalog, padding: PaddingValues, onBack: () -> Unit) {
    val details = catalog.byId(exercise.exerciseId)
    val title = exercise.name?.preferred().orEmpty().ifBlank { details?.names?.preferred().orEmpty() }
    LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Tilbake") }
                Text(title, fontSize = 21.sp, fontWeight = FontWeight.SemiBold)
            }
            ExerciseAnimation(catalog, exercise.artworkId.orEmpty(), Modifier.padding(horizontal = 18.dp))
            NovaCard(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Row(Modifier.fillMaxWidth()) {
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) { Text("${exercise.sets} sett × ${exercise.reps} reps", fontSize = 17.sp, fontWeight = FontWeight.SemiBold); Text("PLAN", color = NovaMuted, fontSize = 10.sp) }
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) { Text(exercise.plannedLoadKg?.let(::kg) ?: "Kroppsvekt", fontSize = 17.sp, fontWeight = FontWeight.SemiBold); Text("ARBEIDSVEKT", color = NovaMuted, fontSize = 10.sp) }
                }
            }
            InfoCard("Primære muskler", (exercise.primaryMuscles ?: details?.primary).orEmpty().joinToString(" · "), Icons.Outlined.Info)
            InfoCard("Sekundære muskler", details?.secondary.orEmpty().joinToString(" · ").ifBlank { "—" }, Icons.Outlined.Info)
            InfoCard("Utstyr", (exercise.equipment ?: details?.equipment).orEmpty().joinToString(" · ").ifBlank { "Kroppsvekt" }, Icons.Outlined.Build)
            InfoCard("Teknikk", details?.techniqueLines().orEmpty().joinToString("\n") { "• $it" }.ifBlank { "Ingen ekstra teknikktekst registrert." }, Icons.Outlined.Info)
            InfoCard("Vanlige feil", details?.mistakes.orEmpty().joinToString("\n") { "• $it" }.ifBlank { "Ingen spesifikke feil registrert." }, Icons.Outlined.Warning)
        }
    }
}

@Composable private fun InfoCard(title: String, text: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    NovaCard(Modifier.padding(horizontal = 20.dp, vertical = 5.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(icon, null, tint = NovaBlue, modifier = Modifier.padding(top = 2.dp)); Spacer(Modifier.width(12.dp))
            Column { Text(title, fontWeight = FontWeight.SemiBold); Text(text, color = NovaMuted, fontSize = 13.sp, lineHeight = 19.sp, modifier = Modifier.padding(top = 4.dp)) }
        }
    }
}
private fun kg(v: Double): String = if (v % 1.0 == 0.0) "${v.toInt()} kg" else String.format(java.util.Locale("nb", "NO"), "%.1f kg", v)
