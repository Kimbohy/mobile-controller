package io.github.kimbohy.mobilecontroller.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.io.File

class PresetOpsTest {

    private val layout = LayoutCodec.decode(File("src/main/res/raw/organ_layout.json").readText())
    private val defaults = layout.controls.associate { it.id to it.defaultValue() }

    @Test
    fun capture_contains_one_value_per_control() {
        val preset = PresetOps.capture(layout, defaults, "Gospel")
        assertEquals(layout.controls.map { it.id }.toSet(), preset.values.keys)
        assertEquals("Gospel", preset.name)
    }

    @Test
    fun capture_keeps_the_current_values() {
        val current = defaults + mapOf("db16" to 127, "leslie" to 127)
        val preset = PresetOps.capture(layout, current, "x")
        assertEquals(127, preset.values["db16"])
        assertEquals(127, preset.values["leslie"])
    }

    @Test
    fun capture_fills_a_missing_control_with_its_default() {
        val preset = PresetOps.capture(layout, emptyMap(), "x")
        assertEquals(100, preset.values["volume"])
    }

    @Test
    fun capture_then_apply_gives_back_the_same_state() {
        val current = defaults + mapOf("db8" to 90, "vibrato" to 127, "volume" to 42)
        val preset = PresetOps.capture(layout, current, "x")
        assertEquals(current, PresetOps.apply(layout, defaults, preset))
    }

    @Test
    fun a_control_missing_from_the_preset_keeps_its_current_value() {
        val current = defaults + mapOf("db16" to 55, "volume" to 77)
        val preset = Preset(name = "partiel", values = mapOf("db16" to 127))
        val result = PresetOps.apply(layout, current, preset)
        assertEquals(127, result["db16"])
        assertEquals(77, result["volume"])
    }

    @Test
    fun an_unknown_control_id_is_ignored() {
        val preset = Preset(name = "futur", values = mapOf("db16" to 10, "reverb" to 99))
        val result = PresetOps.apply(layout, defaults, preset)
        assertFalse(result.containsKey("reverb"))
        assertEquals(10, result["db16"])
    }

    @Test
    fun values_are_clamped_to_0_127() {
        val preset = Preset(name = "fou", values = mapOf("db16" to 500, "db8" to -20))
        val result = PresetOps.apply(layout, defaults, preset)
        assertEquals(127, result["db16"])
        assertEquals(0, result["db8"])
    }

    @Test
    fun apply_always_returns_every_control() {
        val result = PresetOps.apply(layout, emptyMap(), Preset(name = "vide", values = emptyMap()))
        assertEquals(layout.controls.size, result.size)
    }

    @Test
    fun default_names_are_numbered() {
        assertEquals("Preset 3", PresetOps.defaultName(3))
    }
}