package no.nova.training.data.transfer

import no.nova.training.data.exercises.ExerciseCatalog
import no.nova.training.data.model.Ntp1Payload

class Ntp1Validator(private val catalog: ExerciseCatalog) {
    fun validate(payload: Ntp1Payload): Ntp1Payload {
        require(payload.schema == "NTP1") { "QR-dataene inneholder ikke et NOVA NTP1-program." }
        require(payload.version == 1) { "Denne NTP1-versjonen støttes ikke av appen." }
        require(!payload.exportId.isNullOrBlank()) { "Programmet mangler exportId." }
        require((payload.program?.id ?: 0L) > 0) { "Programmet mangler program-ID." }
        require(!payload.weeks.isNullOrEmpty()) { "Programmet inneholder ingen uker." }

        var exerciseRows = 0
        payload.weeks.forEach { week ->
            require((week.weekNumber ?: 0) > 0) { "Ugyldig ukenummer i programmet." }
            require(week.days != null) { "En uke mangler treningsdager." }
            week.days.orEmpty().forEach { day ->
                require((day.dayNumber ?: 0) > 0) { "Ugyldig dagnummer i programmet." }
                require(day.exercises != null) { "En treningsdag mangler øvelsesliste." }
                day.exercises.orEmpty().forEach { exercise ->
                    val id = exercise.exerciseId.orEmpty()
                    val art = exercise.artworkId.orEmpty()
                    require(id.isNotBlank()) { "En øvelse mangler exerciseId." }
                    require((exercise.sets ?: 0) >= 1) { "Øvelsen $id har ugyldig antall sett." }
                    require(exercise.reps != null) { "Øvelsen $id mangler reps." }
                    require(catalog.contains(id)) { "Øvelsen $id finnes ikke i mobilens øvelseskatalog." }
                    require(catalog.artworkExists(art)) { "Illustrasjonen $art mangler på mobilen." }
                    exerciseRows++
                }
            }
        }
        require(exerciseRows > 0) { "Programmet inneholder ingen øvelser." }
        return payload
    }
}
