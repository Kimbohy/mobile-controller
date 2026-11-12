package io.github.kimbohy.mobilecontroller.ui


import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import io.github.kimbohy.mobilecontroller.R
import io.github.kimbohy.mobilecontroller.core.ConnectionState
import io.github.kimbohy.mobilecontroller.model.BuiltInLayout
import io.github.kimbohy.mobilecontroller.model.ControlType
import io.github.kimbohy.mobilecontroller.ui.theme.MobileControllerTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class PerformanceScreenTest {

    @get:Rule
    val rule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val layout = BuiltInLayout.load(context)

    private fun show(
        connection: ConnectionState,
        onToggle: (String) -> Unit = {},
        onSettings: () -> Unit = {},
    ) {
        rule.setContent {
            MobileControllerTheme {
                PerformanceScreen(
                    layout = layout,
                    values = layout.controls.associate { it.id to it.defaultValue() },
                    connection = connection,
                    onValueChange = { _, _ -> },
                    onToggle = onToggle,
                    onOpenSettings = onSettings,
                )
            }
        }
    }

    @Test
    fun all_13_controls_are_on_screen() {
        show(ConnectionState.Connected("Test"))
        layout.controls.forEach { spec ->
            val tag = if (spec.type == ControlType.SWITCH) "switch_${spec.id}" else "fader_${spec.id}"
            rule.onNodeWithTag(tag).assertExists()
        }
        rule.onNodeWithTag("connection_indicator").assertExists()
    }

    @Test
    fun tapping_the_leslie_switch_reports_its_id() {
        val toggled = mutableListOf<String>()
        show(ConnectionState.Connected("Test"), onToggle = { toggled += it })
        rule.onNodeWithTag("switch_leslie").performClick()
        rule.runOnIdle { assertEquals(listOf("leslie"), toggled) }
    }

    @Test
    fun settings_icon_opens_settings() {
        var opened = 0
        show(ConnectionState.Connected("Test"), onSettings = { opened++ })
        rule.onNodeWithTag("open_settings").performClick()
        rule.runOnIdle { assertEquals(1, opened) }
    }

    @Test
    fun help_sentence_is_shown_only_while_waiting_for_a_port() {
        val hint = context.getString(R.string.hint_no_port)

        show(ConnectionState.Waiting)
        rule.onNodeWithText(hint).assertExists()
    }

    @Test
    fun help_sentence_is_hidden_when_connected() {
        val hint = context.getString(R.string.hint_no_port)

        show(ConnectionState.Connected("Test"))
        rule.onNodeWithText(hint).assertDoesNotExist()
    }
}