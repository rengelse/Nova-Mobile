package no.nova.training.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp
import no.nova.training.data.NovaRepository
import no.nova.training.ui.components.NovaCard
import no.nova.training.ui.theme.NovaMuted

@Composable fun ProgramsScreen(repository: NovaRepository, padding: PaddingValues, onBack: () -> Unit, onActivated: () -> Unit) {
    val programs = repository.allPrograms()
    Column(Modifier.fillMaxSize().padding(padding)) {
        Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Tilbake") }; Text("Lagrede programmer", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold) }
        Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            programs.forEach { p -> NovaCard(onClick = { repository.activate(p.exportId); onActivated() }) { Text(p.name, fontWeight = FontWeight.SemiBold); Text("${p.payload.weeks.orEmpty().size} uker · ${p.payload.weeks.orEmpty().sumOf { it.days.orEmpty().size }} treningsdager", color = NovaMuted) } }
            if (programs.isEmpty()) Text("Ingen programmer er lagret ennå.", color = NovaMuted)
        }
    }
}
