package io.github.kimbohy.mobilecontroller.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ControlType {
    @SerialName("drawbar") DRAWBAR,
    @SerialName("switch") SWITCH,
    @SerialName("fader") FADER,
}

@Serializable
data class ControlSpec(
    val id: String,
    val type: ControlType,
    val label: String,
    val cc: Int,
    val color: String? = null,
    /** Pour un switch : exactement 2 clés d'état, ex. ["slow","fast"]. */
    val states: List<String>? = null,
    /** Tirette/fader : valeur 0..127. Switch : index d'état (0 ou 1). */
    val default: Int = 0,
) {
    /** Valeur MIDI initiale (0..127). Un switch vaut 0 ou 127 (cahier §9). */
    fun defaultValue(): Int = when (type) {
        ControlType.SWITCH -> if (default > 0) 127 else 0
        else -> default.coerceIn(0, 127)
    }
}

@Serializable
data class Layout(
    val version: Int = CURRENT_VERSION,
    val name: String,
    val midiChannel: Int = 1,
    val controls: List<ControlSpec>,
) {
    companion object {
        const val CURRENT_VERSION = 1
    }
}