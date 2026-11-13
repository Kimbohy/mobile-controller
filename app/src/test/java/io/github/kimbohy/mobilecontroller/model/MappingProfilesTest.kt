package io.github.kimbohy.mobilecontroller.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class MappingProfilesTest {

    private fun profiles() =
        MappingProfilesCodec.decode(File("src/main/res/raw/mapping_profiles.json").readText())

    private fun layout() =
        LayoutCodec.decode(File("src/main/res/raw/organ_layout.json").readText())

    private val drawbarIds = listOf("db16", "db513", "db8", "db4", "db223", "db2", "db135", "db113", "db1")

    @Test
    fun the_four_built_in_profiles_exist_and_the_default_is_among_them() {
        val ids = profiles().profiles.map { it.id }
        assertEquals(setOf("hammond-b3x", "ni-b4", "setbfree", "legacy-1.0"), ids.toSet())
        assertTrue(DEFAULT_PROFILE_ID in ids)
    }

    @Test
    fun hammond_profile_uses_cc_12_to_20_for_the_drawbars() {
        val p = profiles().byId("hammond-b3x")
        drawbarIds.forEachIndexed { i, id -> assertEquals(12 + i, p.controls.getValue(id).cc) }
        assertEquals(1, p.controls.getValue("leslie").cc)
        assertEquals(36, p.controls.getValue("percussion").cc)
        assertEquals(11, p.controls.getValue("volume").cc)
        assertFalse(p.invertDrawbars)
    }

    @Test
    fun ni_profile_uses_cc_31_for_vibrato_and_66_for_percussion() {
        val p = profiles().byId("ni-b4")
        drawbarIds.forEachIndexed { i, id -> assertEquals(12 + i, p.controls.getValue(id).cc) }
        assertEquals(31, p.controls.getValue("vibrato").cc)
        assertEquals(66, p.controls.getValue("percussion").cc)
    }

    @Test
    fun setbfree_profile_uses_cc_70_to_78_and_inverts_the_drawbars() {
        val p = profiles().byId("setbfree")
        drawbarIds.forEachIndexed { i, id -> assertEquals(70 + i, p.controls.getValue(id).cc) }
        assertTrue(p.invertDrawbars)
    }

    @Test
    fun setbfree_percussion_never_sends_127_which_would_switch_it_off() {
        val m = profiles().byId("setbfree").controls.getValue("percussion")
        assertEquals(0, m.offValue)
        assertTrue("onValue doit rester dans la zone « normale » (16..62)", m.onValue in 16..62)
    }

    @Test
    fun legacy_profile_is_exactly_the_1_0_mapping() {
        val p = profiles().byId("legacy-1.0")
        drawbarIds.forEachIndexed { i, id -> assertEquals(20 + i, p.controls.getValue(id).cc) }
        assertEquals(29, p.controls.getValue("leslie").cc)
        assertEquals(30, p.controls.getValue("vibrato").cc)
        assertEquals(31, p.controls.getValue("percussion").cc)
        assertEquals(11, p.controls.getValue("volume").cc)
    }

    @Test
    fun the_default_profile_matches_the_cc_written_in_the_layout() {
        val byId = layout().controls.associateBy { it.id }
        val p = profiles().byId(DEFAULT_PROFILE_ID)
        p.controls.forEach { (id, m) -> assertEquals("CC de $id", byId.getValue(id).cc, m.cc) }
    }

    @Test
    fun every_profile_covers_every_control_of_the_layout() {
        val ids = layout().controls.map { it.id }.toSet()
        profiles().profiles.forEach { p ->
            assertEquals("profil ${p.id}", ids, p.controls.keys)
        }
    }

    @Test
    fun no_profile_uses_the_same_cc_twice() {
        profiles().profiles.forEach { p ->
            val ccs = p.controls.values.map { it.cc }
            assertEquals("profil ${p.id}", ccs.size, ccs.toSet().size)
        }
    }

    @Test
    fun an_unknown_profile_id_falls_back_to_the_default_profile() {
        assertEquals(DEFAULT_PROFILE_ID, profiles().byId("n-importe-quoi").id)
    }

    @Test(expected = ProfileException::class)
    fun garbage_is_rejected() {
        MappingProfilesCodec.decode("pas du json")
    }

    @Test(expected = ProfileException::class)
    fun a_future_version_is_rejected() {
        MappingProfilesCodec.decode("""{"version":9,"profiles":[]}""")
    }

    @Test(expected = ProfileException::class)
    fun a_file_without_the_default_profile_is_rejected() {
        MappingProfilesCodec.decode(
            """{"version":1,"profiles":[{"id":"x","name":"X","controls":{"a":{"cc":1}}}]}""",
        )
    }

    @Test(expected = ProfileException::class)
    fun a_cc_out_of_range_is_rejected() {
        MappingProfilesCodec.decode(
            """{"version":1,"profiles":[{"id":"hammond-b3x","name":"X","controls":{"a":{"cc":300}}}]}""",
        )
    }

    @Test(expected = ProfileException::class)
    fun duplicate_profile_ids_are_rejected() {
        MappingProfilesCodec.decode(
            """{"version":1,"profiles":[
                {"id":"hammond-b3x","name":"A","controls":{"a":{"cc":1}}},
                {"id":"hammond-b3x","name":"B","controls":{"a":{"cc":2}}}]}""",
        )
    }
}