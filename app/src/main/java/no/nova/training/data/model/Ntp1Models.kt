package no.nova.training.data.model

data class Ntp1Payload(
    val schema: String? = null,
    val version: Int? = null,
    val exportId: String? = null,
    val exportedAt: String? = null,
    val source: SourceInfo? = null,
    val program: ProgramInfo? = null,
    val manifest: ExerciseManifest? = null,
    val weeks: List<ProgramWeek>? = null
)

data class SourceInfo(val app: String? = null, val version: String? = null)

data class LocalizedText(val nb: String? = null, val en: String? = null) {
    fun preferred(): String = nb?.takeIf { it.isNotBlank() } ?: en.orEmpty()
}

data class ProgramInfo(
    val id: Long? = null,
    val templateId: String? = null,
    val name: LocalizedText? = null,
    val description: LocalizedText? = null,
    val daysPerWeek: Int? = null,
    val cycleWeeks: Int? = null,
    val progression: String? = null,
    val config: Map<String, Any?>? = null,
    val currentPosition: ProgramPosition? = null
)

data class ProgramPosition(val week: Int? = null, val day: Int? = null)

data class ExerciseManifest(
    val exerciseCount: Int? = null,
    val exerciseIds: List<String>? = null,
    val mediaBundledInMobileApp: Boolean? = null
)

data class ProgramWeek(val weekNumber: Int? = null, val days: List<ProgramDay>? = null)

data class ProgramDay(
    val dayNumber: Int? = null,
    val sessionNumber: Int? = null,
    val name: String? = null,
    val exercises: List<ProgramExercise>? = null
)

data class ProgramExercise(
    val exerciseId: String? = null,
    val artworkId: String? = null,
    val name: LocalizedText? = null,
    val primaryMuscles: List<String>? = null,
    val equipment: List<String>? = null,
    val sets: Int? = null,
    val reps: String? = null,
    val plannedLoadKg: Double? = null,
    val progression: Map<String, Any?>? = null
)
