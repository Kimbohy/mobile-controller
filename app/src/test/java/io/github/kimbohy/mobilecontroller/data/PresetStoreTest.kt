package io.github.kimbohy.mobilecontroller.data

import io.github.kimbohy.mobilecontroller.model.Preset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class PresetStoreTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private fun store() = PresetStore(File(tmp.root, "presets"))
    private fun preset(name: String, v: Int) = Preset(name = name, values = mapOf("db16" to v))

    @Test
    fun an_empty_slot_gives_null() {
        assertNull(store().load(1))
        assertTrue(store().usedSlots().isEmpty())
    }

    @Test
    fun save_then_load_round_trips() {
        val s = store()
        assertTrue(s.save(3, preset("Gospel", 127)))
        assertEquals(preset("Gospel", 127), s.load(3))
    }

    @Test
    fun slots_are_independent() {
        val s = store()
        s.save(1, preset("A", 10))
        s.save(2, preset("B", 20))
        assertEquals("A", s.load(1)?.name)
        assertEquals("B", s.load(2)?.name)
        assertNull(s.load(3))
    }

    @Test
    fun saving_again_overwrites_the_slot() {
        val s = store()
        s.save(1, preset("A", 10))
        s.save(1, preset("B", 99))
        assertEquals(99, s.load(1)?.values?.get("db16"))
    }

    @Test
    fun usedSlots_lists_exactly_the_filled_slots() {
        val s = store()
        s.save(2, preset("A", 1))
        s.save(8, preset("B", 2))
        assertEquals(setOf(2, 8), s.usedSlots())
    }

    @Test
    fun a_new_store_instance_sees_what_was_saved_before() {
        store().save(5, preset("Persistant", 64))
        assertEquals("Persistant", store().load(5)?.name)
    }

    @Test
    fun a_corrupted_slot_is_seen_as_empty() {
        val dir = File(tmp.root, "presets").apply { mkdirs() }
        File(dir, "preset_4.json").writeText("{ pas du json ]]")
        assertNull(store().load(4))
        assertTrue(store().usedSlots().isEmpty())
    }

    @Test
    fun a_preset_from_a_future_version_is_seen_as_empty() {
        val dir = File(tmp.root, "presets").apply { mkdirs() }
        File(dir, "preset_6.json").writeText("""{ "version": 99, "name": "futur", "values": {} }""")
        assertNull(store().load(6))
    }

    @Test(expected = IllegalArgumentException::class)
    fun slot_0_is_rejected() {
        store().load(0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun slot_9_is_rejected() {
        store().save(9, preset("x", 1))
    }
}