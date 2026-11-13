package io.github.kimbohy.mobilecontroller.data

import io.github.kimbohy.mobilecontroller.model.AppJson
import io.github.kimbohy.mobilecontroller.model.Preset
import java.io.File

/** Les 8 emplacements de presets, un fichier JSON par emplacement. */
class PresetStore(private val dir: File) {

    companion object {
        const val SLOTS = 8
    }

    private fun fileOf(slot: Int): File {
        require(slot in 1..SLOTS) { "Preset slot must be 1..$SLOTS but was $slot" }
        return File(dir, "preset_$slot.json")
    }

    /** Le preset de cet emplacement, ou null s'il est vide, illisible ou d'une version trop récente. */
    fun load(slot: Int): Preset? {
        val file = fileOf(slot)
        if (!file.exists()) return null
        return try {
            val preset = AppJson.decodeFromString(Preset.serializer(), file.readText())
            if (preset.version in 1..Preset.CURRENT_VERSION) preset else null
        } catch (e: Exception) {
            null
        }
    }

    /** Écrit (ou remplace) le preset de cet emplacement. @return true si réussi. */
    fun save(slot: Int, preset: Preset): Boolean =
        JsonFileStore(fileOf(slot), Preset.serializer()) { preset }.save(preset)

    /** Les emplacements qui contiennent un preset valide. */
    fun usedSlots(): Set<Int> = (1..SLOTS).filter { load(it) != null }.toSet()
}