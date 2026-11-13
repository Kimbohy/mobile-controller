package io.github.kimbohy.mobilecontroller.ui

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.platform.app.InstrumentationRegistry
import io.github.kimbohy.mobilecontroller.core.ConnectionState
import io.github.kimbohy.mobilecontroller.data.Settings
import io.github.kimbohy.mobilecontroller.midi.UsbHostState
import io.github.kimbohy.mobilecontroller.model.BuiltInLayout
import io.github.kimbohy.mobilecontroller.model.BuiltInProfiles
import io.github.kimbohy.mobilecontroller.model.ControlMapping
import io.github.kimbohy.mobilecontroller.model.DEFAULT_PROFILE_ID
import io.github.kimbohy.mobilecontroller.model.MappingResolver
import io.github.kimbohy.mobilecontroller.model.ResolvedMapping
import io.github.kimbohy.mobilecontroller.ui.theme.MobileControllerTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MappingScreenTest {

    @get:Rule
    val rule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val layout = BuiltInLayout.load(context)
    private val profiles = BuiltInProfiles.load(context)

    private class Calls {
        val selected = mutableListOf<String>()
        val ccChanges = mutableListOf<Pair<String, Int>>()
        val inverts = mutableListOf<Boolean>()
        var resets = 0
        var backs = 0
    }

    private fun show(
        calls: Calls,
        mapping: ResolvedMapping = MappingResolver.resolve(layout, profiles.byId(DEFAULT_PROFILE_ID), emptyMap(), null),
        overrides: Map<String, Int> = emptyMap(),
    ) {
        rule.setContent {
            MobileControllerTheme {
                MappingScreen(
                    layout = layout,
                    profiles = profiles.profiles,
                    selectedProfileId = DEFAULT_PROFILE_ID,
                    mapping = mapping,
                    overrides = overrides,
                    onSelectProfile = { calls.selected += it },
                    onCcChange = { id, cc -> calls.ccChanges += id to cc },
                    onInvertChange = { calls.inverts += it },
                    onReset = { calls.resets++ },
                    onBack = { calls.backs++ },
                )
            }
        }
    }

    @Test
    fun choosing_a_profile_reports_its_id() {
        val calls = Calls()
        show(calls)
        rule.onNodeWithTag("profile_setbfree").performScrollTo().performClick()
        rule.runOnIdle { assertEquals(listOf("setbfree"), calls.selected) }
    }

    @Test
    fun plus_and_minus_ask_for_the_neighbouring_cc() {
        val calls = Calls()
        show(calls) // db16 = CC 12 dans le profil par défaut
        rule.onNodeWithTag("cc_plus_db16").performScrollTo().performClick()
        rule.onNodeWithTag("cc_minus_db16").performScrollTo().performClick()
        rule.runOnIdle { assertEquals(listOf("db16" to 13, "db16" to 11), calls.ccChanges) }
    }

    @Test
    fun the_cc_buttons_are_disabled_at_the_limits() {
        val calls = Calls()
        val zero = ResolvedMapping(false, layout.controls.associate { it.id to ControlMapping(cc = 0) })
        show(calls, mapping = zero)
        rule.onNodeWithTag("cc_minus_db16").performScrollTo().assertIsNotEnabled()
        rule.onNodeWithTag("cc_plus_db16").performScrollTo().assertIsEnabled()
    }

    @Test
    fun the_invert_switch_reports_the_new_state() {
        val calls = Calls()
        show(calls) // le profil par défaut n'inverse pas
        rule.onNodeWithTag("invert_toggle").performScrollTo().performClick()
        rule.runOnIdle { assertEquals(listOf(true), calls.inverts) }
    }

    @Test
    fun reset_calls_back() {
        val calls = Calls()
        show(calls, overrides = mapOf("db16" to 90))
        rule.onNodeWithTag("mapping_reset").performScrollTo().performClick()
        rule.runOnIdle { assertEquals(1, calls.resets) }
    }

    @Test
    fun back_calls_back() {
        val calls = Calls()
        show(calls)
        rule.onNodeWithTag("mapping_back").performClick()
        rule.runOnIdle { assertTrue(calls.backs == 1) }
    }

    @Test
    fun the_settings_screen_has_a_button_to_open_the_mapping() {
        var opened = 0
        rule.setContent {
            MobileControllerTheme {
                SettingsScreen(
                    settings = Settings(),
                    connection = ConnectionState.Waiting,
                    usb = UsbHostState(),
                    onChannelChange = {},
                    onRescan = {},
                    onBack = {},
                    onOpenMapping = { opened++ },
                )
            }
        }
        rule.onNodeWithTag("open_mapping").performScrollTo().performClick()
        rule.runOnIdle { assertEquals(1, opened) }
    }
}