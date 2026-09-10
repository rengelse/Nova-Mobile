package no.nova.training.data.exercises

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import no.nova.training.data.model.ExerciseCatalogEntry

class ExerciseCatalog(private val context: Context, private val gson: Gson) {
    private val entries: Map<String, ExerciseCatalogEntry> by lazy {
        val json = context.assets.open("exercise_catalog.json").bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<ExerciseCatalogEntry>>() {}.type
        val list: List<ExerciseCatalogEntry> = gson.fromJson(json, type)
        list.associateBy { it.id }
    }

    fun byId(id: String?): ExerciseCatalogEntry? = id?.let(entries::get)
    fun contains(id: String?): Boolean = id != null && entries.containsKey(id)
    fun size(): Int = entries.size

    fun framePath(artworkId: String, frame: Int): String = "file:///android_asset/exercises/$artworkId/frame-$frame.svg"

    fun artworkExists(artworkId: String?): Boolean {
        if (artworkId.isNullOrBlank()) return false
        return try {
            context.assets.open("exercises/$artworkId/frame-1.svg").close()
            true
        } catch (_: Exception) { false }
    }
}
