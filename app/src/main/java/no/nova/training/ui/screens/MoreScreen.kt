package no.nova.training.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp
import no.nova.training.BuildConfig
import no.nova.training.data.NovaRepository
import no.nova.training.data.model.StoredProgram
import no.nova.training.ui.components.NovaCard
import no.nova.training.ui.components.NovaHeader
import no.nova.training.ui.theme.NovaMuted

@Composable fun MoreScreen(active: StoredProgram?, repository: NovaRepository, padding: PaddingValues, onPrograms: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(padding)) {
        NovaHeader("Mer")
        Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            NovaCard(onClick = onPrograms) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.List, null); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text("Lagrede programmer", fontWeight = FontWeight.SemiBold); Text("${repository.allPrograms().size} program · aktivt: ${active?.name ?: "ingen"}", color = NovaMuted) }; Icon(Icons.Default.ChevronRight, null) } }
            NovaCard { Row(verticalAlignment = Alignment.Top) { Icon(Icons.Default.Info, null); Spacer(Modifier.width(12.dp)); Column { Text("NOVA Mobile", fontWeight = FontWeight.SemiBold); Text("Versjon ${BuildConfig.VERSION_NAME}\nOffline treningsprogram companion for NOVA Desktop.", color = NovaMuted) } } }
            Text("Øvelsesillustrasjoner: Workout Guide / Everkinetic, CC BY-SA 4.0. Attribution følger med appens assets.", color = NovaMuted, style = MaterialTheme.typography.bodySmall)
        }
    }
}
