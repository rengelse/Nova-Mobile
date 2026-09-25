package no.nova.training.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import no.nova.training.data.NovaRepository
import no.nova.training.data.model.ProgramExercise
import no.nova.training.data.model.StoredProgram
import no.nova.training.ui.components.NovaBottomBar
import no.nova.training.ui.screens.*

private sealed interface Route {
    data object Program : Route
    data object Transfer : Route
    data object Food : Route
    data object More : Route
    data class Exercise(val exercise: ProgramExercise) : Route
    data object Programs : Route
}

@Composable fun NovaApp(repository: NovaRepository) {
    var active by remember { mutableStateOf(repository.activeProgram()) }
    var route by remember { mutableStateOf<Route>(if (active == null) Route.Transfer else Route.Program) }

    fun refresh() { active = repository.activeProgram() }
    val bottomKey = when (route) { Route.Transfer -> "transfer"; Route.Food -> "food"; Route.More, Route.Programs -> "more"; else -> "program" }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (route !is Route.Exercise) NovaBottomBar(bottomKey) { key ->
                route = when (key) { "food" -> Route.Food; "transfer" -> Route.Transfer; "more" -> Route.More; else -> Route.Program }
            }
        }
    ) { padding ->
        when (val r = route) {
            Route.Program -> ProgramScreen(active, repository.catalog, padding, onExercise = { route = Route.Exercise(it) }, onTransfer = { route = Route.Transfer })
            Route.Transfer -> TransferScreen(repository, padding, onStored = { refresh() }, onOpenProgram = { refresh(); route = Route.Program })
            Route.Food -> FoodScreen(repository, padding)
            Route.More -> MoreScreen(active, repository, padding, onPrograms = { route = Route.Programs })
            Route.Programs -> ProgramsScreen(repository, padding, onBack = { route = Route.More }, onActivated = { refresh(); route = Route.Program })
            is Route.Exercise -> ExerciseScreen(r.exercise, repository.catalog, padding, onBack = { route = Route.Program })
        }
    }
}
