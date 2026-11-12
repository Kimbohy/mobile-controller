package io.github.kimbohy.mobilecontroller.ui

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import io.github.kimbohy.mobilecontroller.core.ConnectionState
import io.github.kimbohy.mobilecontroller.data.Settings
import io.github.kimbohy.mobilecontroller.midi.UsbHostState
import io.github.kimbohy.mobilecontroller.ui.theme.MobileControllerTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SettingsAndOnboardingTest {

    @get:Rule
    val rule = createComposeRule()

    private fun showSettings(channel: Int, onChannel: (Int) -> Unit = {}, onBack: () -> Unit = {}) {
        rule.setContent {
            MobileControllerTheme {
                SettingsScreen(
                    settings = Settings(midiChannel = channel),
                    connection = ConnectionState.Waiting,
                    usb = UsbHostState(),
                    onChannelChange = onChannel,
                    onRescan = {},
                    onBack = onBack,
                )
            }
        }
    }

    @Test
    fun plus_button_asks_for_the_next_channel() {
        val asked = mutableListOf<Int>()
        showSettings(channel = 4, onChannel = { asked += it })
        rule.onNodeWithTag("channel_plus").performClick()
        rule.runOnIdle { assertEquals(listOf(5), asked) }
    }

    @Test
    fun minus_button_asks_for_the_previous_channel() {
        val asked = mutableListOf<Int>()
        showSettings(channel = 4, onChannel = { asked += it })
        rule.onNodeWithTag("channel_minus").performClick()
        rule.runOnIdle { assertEquals(listOf(3), asked) }
    }

    @Test
    fun channel_buttons_are_disabled_at_the_limits() {
        showSettings(channel = 1)
        rule.onNodeWithTag("channel_minus").assertIsNotEnabled()
        rule.onNodeWithTag("channel_plus").assertIsEnabled()
    }

    @Test
    fun channel_16_cannot_go_higher() {
        showSettings(channel = 16)
        rule.onNodeWithTag("channel_plus").assertIsNotEnabled()
    }

    @Test
    fun back_button_goes_back() {
        var back = 0
        showSettings(channel = 1, onBack = { back++ })
        rule.onNodeWithTag("settings_back").performClick()
        rule.runOnIdle { assertEquals(1, back) }
    }

    @Test
    fun onboarding_shows_three_steps_and_a_skip_button() {
        var skipped = 0
        rule.setContent {
            MobileControllerTheme {
                OnboardingScreen(
                    connection = ConnectionState.Waiting,
                    usb = UsbHostState(),
                    onSkip = { skipped++ },
                )
            }
        }
        for (n in 1..3) rule.onNodeWithTag("onboarding_step_$n").assertExists()
        rule.onNodeWithTag("onboarding_skip").performClick()
        rule.runOnIdle { assertEquals(1, skipped) }
    }
}