package io.github.kimbohy.mobilecontroller.model

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class LayoutException(message: String, cause: Throwable? = null) : Exception(message, cause)

/** Lecture / écriture / validation des layouts JSON. Ne plante jamais : lève LayoutException. */
object LayoutCodec {

    fun decode(text: String): Layout {
        val root = try {
            AppJson.parseToJsonElement(text).jsonObject
        } catch (e: Exception) {
            throw LayoutException("Not a valid JSON object", e)
        }

        val version = root["version"]?.jsonPrimitive?.intOrNull ?: 1
        if (version < 1 || version > Layout.CURRENT_VERSION) {
            throw LayoutException("Unsupported layout version: $version")
        }
        // Quand une version 2 existera, c'est ICI qu'on migrera `root` de `version`
        // vers CURRENT_VERSION avant de décoder, et on ajoutera un test par migration.

        val layout = try {
            AppJson.decodeFromJsonElement(Layout.serializer(), root)
        } catch (e: SerializationException) {
            throw LayoutException("Invalid layout content: ${e.message}", e)
        }
        validate(layout)
        return layout
    }

    fun encode(layout: Layout): String = AppJson.encodeToString(Layout.serializer(), layout)

    private fun validate(layout: Layout) {
        if (layout.midiChannel !in 1..16) throw LayoutException("midiChannel must be 1..16")
        val ids = layout.controls.map { it.id }
        if (ids.size != ids.toSet().size) throw LayoutException("Two controls share the same id")
        for (c in layout.controls) {
            if (c.cc !in 0..127) throw LayoutException("Invalid CC for '${c.id}': ${c.cc}")
            if (c.type == ControlType.SWITCH && c.states?.size != 2) {
                throw LayoutException("Switch '${c.id}' needs exactly 2 states")
            }
        }
    }
}