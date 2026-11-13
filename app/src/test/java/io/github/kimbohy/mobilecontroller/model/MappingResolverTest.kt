package io.github.kimbohy.mobilecontroller.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class MappingResolverTest {

    private val layout = LayoutCodec.decode(File("src/main/res/raw/organ_layout.json").readText())
    private val profiles = MappingProfilesCodec.decode(File("src/main/res/raw/mapping_profiles.json").readText())
    private fun spec(id: String) = layout.controls.first { it.id == id }

    // ---------- ControlMapper ----------

    private val plain = ControlMapping(cc = 1)

    @Test
    fun drawbar_value_is_unchanged_when_not_inverted() {
        assertEquals(100, ControlMapper.toCcValue(ControlType.DRAWBAR, 100, plain, invertDrawbars = false))
    }

    @Test
    fun inverted_drawbar_sends_127_minus_the_value() {
        assertEquals(127, ControlMapper.toCcValue(ControlType.DRAWBAR, 0, plain, invertDrawbars = true))
        assertEquals(0, ControlMapper.toCcValue(ControlType.DRAWBAR, 127, plain, invertDrawbars = true))
        assertEquals(27, ControlMapper.toCcValue(ControlType.DRAWBAR, 100, plain, invertDrawbars = true))
    }

    @Test
    fun the_fader_is_never_inverted() {
        assertEquals(100, ControlMapper.toCcValue(ControlType.FADER, 100, plain, invertDrawbars = true))
    }

    @Test
    fun a_switch_sends_its_off_or_on_value() {
        val perc = ControlMapping(cc = 80, offValue = 0, onValue = 32)
        assertEquals(0, ControlMapper.toCcValue(ControlType.SWITCH, 0, perc, invertDrawbars = false))
        assertEquals(32, ControlMapper.toCcValue(ControlType.SWITCH, 127, perc, invertDrawbars = false))
    }

    @Test
    fun a_switch_is_on_from_64() {
        val m = ControlMapping(cc = 1, offValue = 10, onValue = 20)
        assertEquals(10, ControlMapper.toCcValue(ControlType.SWITCH, 63, m, false))
        assertEquals(20, ControlMapper.toCcValue(ControlType.SWITCH, 64, m, false))
    }

    @Test
    fun a_switch_is_not_affected_by_the_drawbar_inversion() {
        assertEquals(127, ControlMapper.toCcValue(ControlType.SWITCH, 127, plain, invertDrawbars = true))
    }

    @Test
    fun out_of_range_input_is_clamped() {
        assertEquals(127, ControlMapper.toCcValue(ControlType.FADER, 999, plain, false))
        assertEquals(0, ControlMapper.toCcValue(ControlType.FADER, -5, plain, false))
    }

    // ---------- MappingResolver ----------

    @Test
    fun resolving_the_default_profile_gives_cc_12_for_the_16_foot_drawbar() {
        val r = MappingResolver.resolve(layout, profiles.byId(DEFAULT_PROFILE_ID), emptyMap(), null)
        assertEquals(12, r.mappingFor(spec("db16")).cc)
        assertFalse(r.invertDrawbars)
    }

    @Test
    fun a_cc_override_beats_the_profile() {
        val r = MappingResolver.resolve(layout, profiles.byId("hammond-b3x"), mapOf("db16" to 90), null)
        assertEquals(90, r.mappingFor(spec("db16")).cc)
        assertEquals(13, r.mappingFor(spec("db513")).cc)
    }

    @Test
    fun an_override_keeps_the_on_off_values_of_the_profile() {
        val r = MappingResolver.resolve(layout, profiles.byId("setbfree"), mapOf("percussion" to 99), null)
        assertEquals(99, r.mappingFor(spec("percussion")).cc)
        assertEquals(32, r.mappingFor(spec("percussion")).onValue)
    }

    @Test
    fun an_override_out_of_range_is_clamped() {
        val r = MappingResolver.resolve(layout, profiles.byId("hammond-b3x"), mapOf("db16" to 500), null)
        assertEquals(127, r.mappingFor(spec("db16")).cc)
    }

    @Test
    fun the_invert_override_beats_the_profile() {
        val setbfree = profiles.byId("setbfree")
        assertTrue(MappingResolver.resolve(layout, setbfree, emptyMap(), null).invertDrawbars)
        assertFalse(MappingResolver.resolve(layout, setbfree, emptyMap(), false).invertDrawbars)
        assertTrue(MappingResolver.resolve(layout, profiles.byId("hammond-b3x"), emptyMap(), true).invertDrawbars)
    }

    @Test
    fun a_control_unknown_to_the_profile_falls_back_to_the_layout_cc() {
        val sparse = MappingProfile("p", "P", controls = mapOf("db16" to ControlMapping(cc = 55)))
        val r = MappingResolver.resolve(layout, sparse, emptyMap(), null)
        assertEquals(55, r.mappingFor(spec("db16")).cc)
        assertEquals(spec("db513").cc, r.mappingFor(spec("db513")).cc)
    }

    @Test
    fun ccValue_combines_profile_inversion_and_switch_values() {
        val r = MappingResolver.resolve(layout, profiles.byId("setbfree"), emptyMap(), null)
        assertEquals(27, r.ccValue(spec("db16"), 100))      // tirette inversée
        assertEquals(0, r.ccValue(spec("percussion"), 0))   // percussion éteinte
        assertEquals(32, r.ccValue(spec("percussion"), 127)) // percussion « normale »
        assertEquals(64, r.ccValue(spec("leslie"), 0))      // Leslie lent
        assertEquals(127, r.ccValue(spec("leslie"), 127))   // Leslie rapide
        assertEquals(100, r.ccValue(spec("volume"), 100))   // volume jamais inversé
    }

    @Test
    fun profileCc_ignores_the_user_overrides() {
        val p = profiles.byId("hammond-b3x")
        assertEquals(12, MappingResolver.profileCc(p, spec("db16")))
    }

    @Test
    fun the_legacy_profile_reproduces_the_1_0_numbers_end_to_end() {
        val r = MappingResolver.resolve(layout, profiles.byId(LEGACY_PROFILE_ID), emptyMap(), null)
        assertEquals(20, r.mappingFor(spec("db16")).cc)
        assertEquals(29, r.mappingFor(spec("leslie")).cc)
        assertEquals(127, r.ccValue(spec("leslie"), 127))
    }
}