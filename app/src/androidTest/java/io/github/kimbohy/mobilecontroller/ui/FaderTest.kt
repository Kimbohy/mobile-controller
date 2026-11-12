package io.github.kimbohy.mobilecontroller.ui

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import io.github.kimbohy.mobilecontroller.ui.theme.MobileControllerTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FaderTest {

    @get:Rule
    val rule = createComposeRule()

    private fun showFader(initial: Int, onChange: (Int) -> Unit): () -> Int {
        var current by mutableIntStateOf(initial)
        rule.setContent {
            MobileControllerTheme {
                Fader(
                    label = "8'",
                    value = current,
                    knobColor = Color.White,
                    onValueChange = { current = it; onChange(it) },
                    testTag = "fader",
                    modifier = Modifier.width(72.dp).height(400.dp),
                )
            }
        }
        return { current }
    }

    @Test
    fun dragging_up_raises_the_value() {
        val value = showFader(initial = 0) {}
        rule.onNodeWithTag("fader").performTouchInput {
            down(Offset(centerX, height * 0.9f))
            moveTo(Offset(centerX, height * 0.1f))
            up()
        }
        rule.runOnIdle { assertTrue("valeur = ${value()}", value() > 100) }
    }

    @Test
    fun dragging_all_the_way_up_reaches_127() {
        val value = showFader(initial = 0) {}
        rule.onNodeWithTag("fader").performTouchInput {
            down(Offset(centerX, height * 0.95f))
            moveTo(Offset(centerX, 0f)) // tout en haut : dépasse la course utile, la valeur est bornée à 127
            up()
        }
        rule.runOnIdle { assertEquals(127, value()) }
    }

    @Test
    fun dragging_all_the_way_down_reaches_0() {
        val value = showFader(initial = 127) {}
        rule.onNodeWithTag("fader").performTouchInput {
            down(Offset(centerX, height * 0.05f))
            moveTo(Offset(centerX, height - 1f)) // tout en bas : la valeur est bornée à 0
            up()
        }
        rule.runOnIdle { assertEquals(0, value()) }
    }

    @Test
    fun touching_without_moving_does_not_jump_in_relative_mode() {
        val received = mutableListOf<Int>()
        val value = showFader(initial = 64) { received += it }
        rule.onNodeWithTag("fader").performTouchInput {
            down(Offset(centerX, height * 0.05f)) // tout en haut de la course
            up()
        }
        rule.runOnIdle {
            assertTrue("aucun message attendu, reçu : $received", received.isEmpty())
            assertEquals(64, value())
        }
    }
}