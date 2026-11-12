package io.github.kimbohy.mobilecontroller.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class JsonFileStoreTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private fun store(name: String = "state.json") =
        JsonFileStore(File(tmp.root, name), SavedState.serializer()) { SavedState() }

    @Test
    fun missing_file_gives_the_default() {
        assertEquals(SavedState(), store().load())
    }

    @Test
    fun save_then_load_round_trips() {
        val s = store()
        val value = SavedState(values = mapOf("db16" to 127, "volume" to 100))
        assertTrue(s.save(value))
        assertEquals(value, s.load())
    }

    @Test
    fun saving_twice_overwrites_the_previous_content() {
        val s = store()
        s.save(SavedState(values = mapOf("a" to 1)))
        assertTrue(s.save(SavedState(values = mapOf("a" to 2))))
        assertEquals(2, s.load().values["a"])
    }

    @Test
    fun corrupted_file_gives_the_default_instead_of_crashing() {
        File(tmp.root, "state.json").writeText("{ pas du json ]]")
        assertEquals(SavedState(), store().load())
    }

    @Test
    fun no_temporary_file_is_left_behind() {
        val s = store()
        s.save(SavedState())
        assertFalse(File(tmp.root, "state.json.tmp").exists())
    }

    @Test
    fun settings_defaults_are_channel_1_and_onboarding_not_done() {
        val settings = Settings()
        assertEquals(1, settings.midiChannel)
        assertFalse(settings.onboardingDone)
    }

    @Test
    fun settings_round_trip() {
        val s = JsonFileStore(File(tmp.root, "settings.json"), Settings.serializer()) { Settings() }
        val value = Settings(midiChannel = 7, onboardingDone = true)
        s.save(value)
        assertEquals(value, s.load())
    }
}