package no.nova.training.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import no.nova.training.data.exercises.ExerciseCatalog

@Composable fun ExerciseAnimation(catalog: ExerciseCatalog, artworkId: String, modifier: Modifier = Modifier) {
    var frame by remember(artworkId) { mutableIntStateOf(1) }
    var direction by remember(artworkId) { mutableIntStateOf(1) }
    LaunchedEffect(artworkId) {
        while (true) {
            delay(800)
            val next = frame + direction
            if (next >= 3) { frame = 3; direction = -1 }
            else if (next <= 1) { frame = 1; direction = 1 }
            else frame = next
        }
    }
    Box(modifier.fillMaxWidth().height(245.dp), contentAlignment = Alignment.Center) {
        AsyncImage(model = catalog.framePath(artworkId, frame), contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxWidth().height(230.dp))
    }
}
