package io.github.kimbohy.mobilecontroller.ui

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import io.github.kimbohy.mobilecontroller.core.ConnectionState
import io.github.kimbohy.mobilecontroller.model.BuiltInLayout
import io.github.kimbohy.mobilecontroller.ui.theme.MobileControllerTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.Before

class LockPresetSyncTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setLandscapeOrientation() {
        rule.activity.requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        rule.waitForIdle()
    }

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val layout = BuiltInLayout.load(context)

    private class Calls {
        val recalled = mutableListOf<Int>()
        val saved = mutableListOf<Int>()
        val toggled = mutableListOf<String>()
        var syncs = 0
        var lockToggles = 0
        var settings = 0
    }

    private fun show(calls: Calls, locked: Boolean = false, used: Set<Int> = emptySet()) {
        rule.setContent {
            MobileControllerTheme {
                PerformanceScreen(
                    layout = layout,
                    values = layout.controls.associate { it.id to it.defaultValue() },
                    connection = ConnectionState.Connected("Test"),
                    onValueChange = { _, _ -> },
                    onToggle = { calls.toggled += it },
                    onOpenSettings = { calls.settings++ },
                    locked = locked,
                    usedPresets = used,
                    onRecallPreset = { calls.recalled += it },
                    onSavePreset = { calls.saved += it },
                    onSync = { calls.syncs++ },
                    onToggleLock = { calls.lockToggles++ },
                )
            }
        }
    }

    // ---------- Sync (F13) ----------

    @Test
    fun the_sync_button_calls_back() {
        val calls = Calls()
        show(calls)
        rule.onNodeWithTag("sync_button").performClick()
        rule.runOnIdle { assertEquals(1, calls.syncs) }
    }

    // ---------- Presets (F20) ----------

    @Test
    fun tapping_a_filled_preset_recalls_it() {
        val calls = Calls()
        show(calls, used = setOf(2))
        rule.onNodeWithTag("preset_2").performClick()
        rule.runOnIdle { assertEquals(listOf(2), calls.recalled) }
    }

    @Test
    fun tapping_an_empty_preset_does_nothing() {
        val calls = Calls()
        show(calls, used = setOf(2))
        rule.onNodeWithTag("preset_3").performClick()
        rule.runOnIdle {
            assertTrue(calls.recalled.isEmpty())
            assertTrue(calls.saved.isEmpty())
        }
    }

    @Test
    fun a_long_press_saves_the_preset_even_on_an_empty_slot() {
        val calls = Calls()
        show(calls, used = emptySet())
        rule.onNodeWithTag("preset_3").performTouchInput { longClick() }
        rule.runOnIdle { assertEquals(listOf(3), calls.saved) }
    }

    @Test
    fun a_long_press_on_a_filled_slot_saves_and_does_not_recall() {
        val calls = Calls()
        show(calls, used = setOf(5))
        rule.onNodeWithTag("preset_5").performTouchInput { longClick() }
        rule.runOnIdle {
            assertEquals(listOf(5), calls.saved)
            assertTrue(calls.recalled.isEmpty())
        }
    }

    // ---------- Verrou (F21) ----------

    @Test
    fun a_long_press_on_the_lock_toggles_it() {
        val calls = Calls()
        show(calls)
        rule.onNodeWithTag("lock_button").performTouchInput { longClick() }
        rule.runOnIdle { assertEquals(1, calls.lockToggles) }
    }

    @Test
    fun a_simple_tap_on_the_lock_does_nothing() {
        val calls = Calls()
        show(calls)
        rule.onNodeWithTag("lock_button").performClick()
        rule.runOnIdle { assertEquals(0, calls.lockToggles) }
    }

    @Test
    fun when_locked_the_lock_itself_still_works_to_unlock() {
        val calls = Calls()
        show(calls, locked = true)
        rule.onNodeWithTag("lock_button").performTouchInput { longClick() }
        rule.runOnIdle { assertEquals(1, calls.lockToggles) }
    }

    @Test
    fun when_locked_sync_presets_and_settings_are_ignored() {
        val calls = Calls()
        show(calls, locked = true, used = setOf(2))
        rule.onNodeWithTag("sync_button").performClick()
        rule.onNodeWithTag("preset_2").performClick()
        rule.onNodeWithTag("preset_2").performTouchInput { longClick() }
        rule.onNodeWithTag("open_settings").performClick()
        rule.runOnIdle {
            assertEquals(0, calls.syncs)
            assertTrue(calls.recalled.isEmpty())
            assertTrue(calls.saved.isEmpty())
            assertEquals(0, calls.settings)
        }
    }

    @Test
    fun when_locked_a_switch_ignores_touches() {
        val calls = Calls()
        show(calls, locked = true)
        rule.onNodeWithTag("switch_leslie").performClick()
        rule.runOnIdle { assertTrue(calls.toggled.isEmpty()) }
    }

    @Test
    fun when_not_locked_a_switch_still_works() {
        val calls = Calls()
        show(calls, locked = false)
        rule.onNodeWithTag("switch_leslie").performClick()
        rule.runOnIdle { assertEquals(listOf("leslie"), calls.toggled) }
    }

    @Test
    fun a_disabled_fader_ignores_a_drag() {
        var value by mutableIntStateOf(10)
        rule.setContent {
            MobileControllerTheme {
                Fader(
                    label = "8'",
                    value = value,
                    knobColor = Color.White,
                    onValueChange = { value = it },
                    testTag = "fader",
                    enabled = false,
                    modifier = Modifier.width(72.dp).height(400.dp),
                )
            }
        }
        rule.onNodeWithTag("fader").performTouchInput {
            down(Offset(centerX, height * 0.9f))
            moveTo(Offset(centerX, height * 0.1f))
            up()
        }
        rule.runOnIdle { assertEquals(10, value) }
    }
}