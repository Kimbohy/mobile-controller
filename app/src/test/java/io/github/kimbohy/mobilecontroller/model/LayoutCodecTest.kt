package io.github.kimbohy.mobilecontroller.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class LayoutCodecTest {

    /** Les tests unitaires s'exécutent depuis le dossier app/. */
    private fun builtIn(): Layout =
        LayoutCodec.decode(File("src/main/res/raw/organ_layout.json").readText())

    @Test
    fun built_in_layout_has_the_13_controls_of_the_spec() {
        val controls = builtIn().controls
        assertEquals(13, controls.size)
        assertEquals(9, controls.count { it.type == ControlType.DRAWBAR })
        assertEquals(3, controls.count { it.type == ControlType.SWITCH })
        assertEquals(1, controls.count { it.type == ControlType.FADER })
    }

    @Test
    fun built_in_layout_follows_the_default_cc_mapping() {
        val byId = builtIn().controls.associateBy { it.id }
        val drawbarIds = listOf("db16", "db513", "db8", "db4", "db223", "db2", "db135", "db113", "db1")
        drawbarIds.forEachIndexed { index, id -> assertEquals(12 + index, byId.getValue(id).cc) }
        assertEquals(1, byId.getValue("leslie").cc)
        assertEquals(40, byId.getValue("vibrato").cc)
        assertEquals(36, byId.getValue("percussion").cc)
        assertEquals(11, byId.getValue("volume").cc)
    }

    @Test
    fun built_in_drawbars_use_the_hammond_colors() {
        val colors = builtIn().controls.filter { it.type == ControlType.DRAWBAR }.map { it.color }
        assertEquals(
            listOf("brown", "brown", "white", "white", "black", "white", "black", "white", "white"),
            colors,
        )
    }

    @Test
    fun built_in_layout_has_unique_ids_and_ccs() {
        val controls = builtIn().controls
        assertEquals(controls.size, controls.map { it.id }.toSet().size)
        assertEquals(controls.size, controls.map { it.cc }.toSet().size)
    }

    @Test
    fun encode_then_decode_gives_the_same_layout() {
        val layout = builtIn()
        assertEquals(layout, LayoutCodec.decode(LayoutCodec.encode(layout)))
    }

    @Test
    fun missing_version_is_read_as_version_1() {
        val layout = LayoutCodec.decode("""{"name":"x","controls":[]}""")
        assertEquals(1, layout.version)
    }

    @Test
    fun unknown_extra_fields_are_ignored() {
        val layout = LayoutCodec.decode("""{"version":1,"name":"x","futureField":42,"controls":[]}""")
        assertEquals("x", layout.name)
    }

    @Test
    fun switch_default_maps_to_0_or_127() {
        val off = ControlSpec("s", ControlType.SWITCH, "S", 1, states = listOf("off", "on"), default = 0)
        val on = off.copy(default = 1)
        assertEquals(0, off.defaultValue())
        assertEquals(127, on.defaultValue())
    }

    @Test
    fun fader_default_is_clamped() {
        val f = ControlSpec("f", ControlType.FADER, "F", 1, default = 999)
        assertEquals(127, f.defaultValue())
    }

    @Test(expected = LayoutException::class)
    fun future_version_is_rejected_with_a_clear_error() {
        LayoutCodec.decode("""{"version":2,"name":"x","controls":[]}""")
    }

    @Test(expected = LayoutException::class)
    fun garbage_is_rejected_without_crashing() {
        LayoutCodec.decode("ceci n'est pas du json")
    }

    @Test(expected = LayoutException::class)
    fun duplicate_ids_are_rejected() {
        LayoutCodec.decode(
            """{"name":"x","controls":[
                {"id":"a","type":"fader","label":"A","cc":1},
                {"id":"a","type":"fader","label":"B","cc":2}]}""",
        )
    }

    @Test(expected = LayoutException::class)
    fun cc_out_of_range_is_rejected() {
        LayoutCodec.decode("""{"name":"x","controls":[{"id":"a","type":"fader","label":"A","cc":200}]}""")
    }

    @Test(expected = LayoutException::class)
    fun switch_without_two_states_is_rejected() {
        LayoutCodec.decode(
            """{"name":"x","controls":[{"id":"s","type":"switch","label":"S","cc":1,"states":["on"]}]}""",
        )
    }

    @Test
    fun layout_error_message_is_meaningful() {
        val error = runCatching { LayoutCodec.decode("""{"version":9,"name":"x","controls":[]}""") }
            .exceptionOrNull()
        assertTrue(error is LayoutException)
        assertTrue(error!!.message!!.contains("9"))
    }
}