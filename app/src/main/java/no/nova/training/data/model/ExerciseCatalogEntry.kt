package no.nova.training.data.model

data class ExerciseCatalogEntry(
    val id: String,
    val names: LocalizedText,
    val primary: List<String> = emptyList(),
    val secondary: List<String> = emptyList(),
    val equipment: List<String> = emptyList(),
    val pattern: String = "",
    val difficulty: String = "",
    val start: String = "",
    val end: String = "",
    val tips: List<String> = emptyList(),
    val mistakes: List<String> = emptyList(),
    val artworkId: String,
    val attribution: ExerciseAttribution? = null
) {
    fun techniqueLines(): List<String> = buildList {
        if (start.isNotBlank()) add(start)
        if (end.isNotBlank()) add(end)
        addAll(tips.filter { it.isNotBlank() })
    }
}

data class ExerciseAttribution(
    val creator: String = "",
    val license: String = "",
    val source: ExerciseAttributionSource? = null
)

data class ExerciseAttributionSource(
    val name: String = ""
)


data class StoredProgram(
    val exportId: String,
    val programId: Long,
    val name: String,
    val importedAt: Long,
    val payload: Ntp1Payload
)
