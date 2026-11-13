package io.github.kimbohy.mobilecontroller.model

import kotlinx.serialization.Serializable

/** Un preset : les valeurs d'écran (0..127) de tous les contrôles, par identifiant. */
@Serializable
data class Preset(
    val version: Int = CURRENT_VERSION,
    val name: String,
    val values: Map<String, Int>,
) {
    companion object {
        const val CURRENT_VERSION = 1
    }
}

object PresetOps {

    fun defaultName(slot: Int): String = "Preset $slot"

    /** Photographie l'état courant : une valeur pour chaque contrôle du layout. */
    fun capture(layout: Layout, values: Map<String, Int>, name: String): Preset =
        Preset(
            name = name,
            values = layout.controls.associate { spec ->
                spec.id to (values[spec.id] ?: spec.defaultValue()).coerceIn(0, 127)
            },
        )

    /** Nouvel état après rappel d'un preset. Absent → valeur actuelle ; inconnu → ignoré ; borné à 0..127. */
    fun apply(layout: Layout, current: Map<String, Int>, preset: Preset): Map<String, Int> =
        layout.controls.associate { spec ->
            val value = preset.values[spec.id] ?: current[spec.id] ?: spec.defaultValue()
            spec.id to value.coerceIn(0, 127)
        }
}