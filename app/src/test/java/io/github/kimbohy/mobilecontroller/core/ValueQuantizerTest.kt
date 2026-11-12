package io.github.kimbohy.mobilecontroller.core

import org.junit.Assert.assertEquals
import org.junit.Test

class ValueQuantizerTest {

    @Test
    fun bottom_is_zero() = assertEquals(0, ValueQuantizer.toMidi(0f))

    @Test
    fun top_is_127() = assertEquals(127, ValueQuantizer.toMidi(1f))

    @Test
    fun middle_rounds_to_64() = assertEquals(64, ValueQuantizer.toMidi(0.5f))

    @Test
    fun one_step_is_one() = assertEquals(1, ValueQuantizer.toMidi(1f / 127f))

    @Test
    fun below_range_is_clamped_to_zero() = assertEquals(0, ValueQuantizer.toMidi(-3f))

    @Test
    fun above_range_is_clamped_to_127() = assertEquals(127, ValueQuantizer.toMidi(7f))

    @Test
    fun nan_is_zero() = assertEquals(0, ValueQuantizer.toMidi(Float.NaN))

    @Test
    fun every_midi_value_survives_a_round_trip() {
        for (v in 0..127) {
            assertEquals(v, ValueQuantizer.toMidi(ValueQuantizer.toPosition(v)))
        }
    }

    @Test
    fun toPosition_is_clamped() {
        assertEquals(0f, ValueQuantizer.toPosition(-10), 0f)
        assertEquals(1f, ValueQuantizer.toPosition(500), 0f)
    }
}