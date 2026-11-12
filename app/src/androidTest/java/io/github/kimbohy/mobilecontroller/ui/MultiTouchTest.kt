package io.github.kimbohy.mobilecontroller.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import io.github.kimbohy.mobilecontroller.ui.theme.MobileControllerTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MultiTouchTest {

    @get:Rule
    val rule = createComposeRule()

    /** F3 : 5 tirettes manipulées en même temps, chacune suit SON doigt sans interférence. */
    @Test
    fun five_fingers_move_five_drawbars_independently() {
        val values = mutableStateListOf(0, 0, 0, 0, 0)
        rule.setContent {
            MobileControllerTheme {
                Row(Modifier.fillMaxWidth().height(400.dp)) {
                    for (i in 0 until 5) {
                        Fader(
                            label = "F$i",
                            value = values[i],
                            knobColor = Color.White,
                            onValueChange = { values[i] = it },
                            testTag = "f$i",
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                        )
                    }
                }
            }
        }

        val bounds = (0 until 5).map { rule.onNodeWithTag("f$it").fetchSemanticsNode().boundsInRoot }

        rule.onRoot().performTouchInput {
            // 5 doigts posent en bas de 5 tirettes différentes
            bounds.forEachIndexed { i, b -> down(i, Offset(b.center.x, b.bottom - 10f)) }
            // chaque doigt monte d'une distance différente, tous en même temps
            bounds.forEachIndexed { i, b ->
                updatePointerTo(i, Offset(b.center.x, b.bottom - 10f - (i + 1) * 80f))
            }
            move()
            bounds.indices.forEach { up(it) }
        }

        rule.runOnIdle {
            val v = values.toList()
            assertTrue("chaque tirette a bougé : $v", v.all { it > 0 })
            assertTrue("plus le doigt monte, plus la valeur est haute : $v", v.zipWithNext().all { (a, b) -> a < b })
        }
    }
}