package io.github.kimbohy.mobilecontroller.core

import kotlin.math.roundToInt

/** Convertit entre une position d'écran (0.0 = bas, 1.0 = haut) et une valeur MIDI (0..127). */
object ValueQuantizer {
    const val MIN = 0
    const val MAX = 127

    /** Position normalisée → valeur MIDI. Toujours dans 0..127, même pour une entrée absurde. */
    fun toMidi(position: Float): Int {
        if (position.isNaN()) return MIN
        val clamped = position.coerceIn(0f, 1f)
        return (clamped * MAX).roundToInt()
    }

    /** Valeur MIDI → position normalisée 0.0..1.0. */
    fun toPosition(value: Int): Float = value.coerceIn(MIN, MAX) / MAX.toFloat()
}