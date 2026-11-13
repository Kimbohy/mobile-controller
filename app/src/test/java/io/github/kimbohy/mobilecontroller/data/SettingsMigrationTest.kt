package io.github.kimbohy.mobilecontroller.data

import io.github.kimbohy.mobilecontroller.model.DEFAULT_PROFILE_ID
import io.github.kimbohy.mobilecontroller.model.LEGACY_PROFILE_ID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class SettingsMigrationTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private val file get() = File(tmp.root, "settings.json")

    private fun store() = JsonFileStore(file, Settings.serializer(), migrate = SettingsMigration::migrate) { Settings() }

    @Test
    fun a_new_installation_starts_on_the_new_default_profile() {
        val s = store().load()
        assertEquals(DEFAULT_PROFILE_ID, s.mappingProfileId)
        assertEquals(Settings.CURRENT_VERSION, s.version)
    }

    @Test
    fun a_1_0_settings_file_keeps_the_legacy_mapping() {
        file.writeText("""{ "version": 1, "midiChannel": 7, "onboardingDone": true }""")
        val s = store().load()
        assertEquals(LEGACY_PROFILE_ID, s.mappingProfileId)
    }

    @Test
    fun migration_preserves_the_existing_values() {
        file.writeText("""{ "version": 1, "midiChannel": 7, "onboardingDone": true }""")
        val s = store().load()
        assertEquals(7, s.midiChannel)
        assertTrue(s.onboardingDone)
        assertEquals(2, s.version)
        assertTrue(s.ccOverrides.isEmpty())
        assertNull(s.invertDrawbars)
    }

    @Test
    fun a_file_without_a_version_is_treated_as_1_0() {
        file.writeText("""{ "midiChannel": 3 }""")
        assertEquals(LEGACY_PROFILE_ID, store().load().mappingProfileId)
    }

    @Test
    fun a_version_2_file_is_left_untouched() {
        file.writeText(
            """{ "version": 2, "midiChannel": 4, "mappingProfileId": "setbfree",
                 "ccOverrides": { "db16": 90 }, "invertDrawbars": false }""",
        )
        val s = store().load()
        assertEquals("setbfree", s.mappingProfileId)
        assertEquals(mapOf("db16" to 90), s.ccOverrides)
        assertEquals(false, s.invertDrawbars)
    }

    @Test
    fun after_migration_a_save_writes_version_2_and_it_stays_stable() {
        file.writeText("""{ "version": 1, "midiChannel": 7 }""")
        val s = store()
        s.save(s.load())
        val reloaded = store().load()
        assertEquals(2, reloaded.version)
        assertEquals(LEGACY_PROFILE_ID, reloaded.mappingProfileId)
    }

    @Test
    fun a_corrupted_file_gives_the_defaults_without_crashing() {
        file.writeText("{ pas du json ]]")
        assertEquals(Settings(), store().load())
    }
}