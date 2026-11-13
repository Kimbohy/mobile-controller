package io.github.kimbohy.mobilecontroller.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kimbohy.mobilecontroller.core.ValueQuantizer

private val KnobHeight = 56.dp
private const val DISABLED_ALPHA = 0.4f

/** Couleur d'une tirette, comme sur un orgue Hammond (F2). */
fun drawbarColor(name: String?): Color = when (name) {
    "brown" -> Color(0xFF7B4A2D)
    "white" -> Color(0xFFF1EFE8)
    "black" -> Color(0xFF1B1B1D)
    else -> Color(0xFFB0B4BA)
}

/**
 * Tirette verticale (ou fader de volume) : valeur MIDI 0..127.
 * Ne stocke AUCUN état de valeur : elle affiche [value] et signale les changements.
 * Avec [enabled] = false (verrou tactile) elle ignore tous les touchers et s'affiche grisée.
 */
@Composable
fun Fader(
    label: String,
    value: Int,
    knobColor: Color,
    onValueChange: (Int) -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val currentValue by rememberUpdatedState(value)
    val currentOnChange by rememberUpdatedState(onValueChange)
    var touching by remember { mutableStateOf(false) }

    val slotColor = MaterialTheme.colorScheme.surfaceVariant
    val outline = Color(0xFF9AA0A6)
    val grip = if (knobColor.luminance() > 0.5f) Color.Black.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.55f)

    Column(
        modifier = modifier.alpha(if (enabled) 1f else DISABLED_ALPHA),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            maxLines = 1,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.height(24.dp),
        )
        Canvas(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag(testTag)
                .semantics { contentDescription = label }
                .pointerInput(enabled) {
                    if (!enabled) return@pointerInput // verrouillé : on n'écoute rien
                    val knobHeightPx = KnobHeight.toPx()
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        down.consume()
                        touching = true
                        // Mode relatif : on part de la position ACTUELLE de la tirette.
                        var position = ValueQuantizer.toPosition(currentValue)
                        val travel = (size.height - knobHeightPx).coerceAtLeast(1f)
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id }
                            if (change == null || !change.pressed) break
                            val dy = change.position.y - change.previousPosition.y
                            if (dy != 0f) {
                                position = (position - dy / travel).coerceIn(0f, 1f)
                                currentOnChange(ValueQuantizer.toMidi(position))
                            }
                            change.consume()
                        }
                        touching = false
                    }
                },
        ) {
            val slotWidth = 8.dp.toPx()
            val knobH = KnobHeight.toPx()
            val knobW = size.width - 8.dp.toPx()
            val travel = (size.height - knobH).coerceAtLeast(0f)
            val knobTop = (1f - ValueQuantizer.toPosition(value)) * travel
            val knobLeft = (size.width - knobW) / 2f

            // Rail
            drawRoundRect(
                color = slotColor,
                topLeft = Offset((size.width - slotWidth) / 2f, 0f),
                size = Size(slotWidth, size.height),
                cornerRadius = CornerRadius(slotWidth / 2f),
            )
            // Poignée
            val knobTopLeft = Offset(knobLeft, knobTop)
            val knobSize = Size(knobW, knobH)
            val corner = CornerRadius(8.dp.toPx())
            drawRoundRect(color = knobColor, topLeft = knobTopLeft, size = knobSize, cornerRadius = corner)
            drawRoundRect(
                color = outline, topLeft = knobTopLeft, size = knobSize, cornerRadius = corner,
                style = Stroke(width = 2.dp.toPx()),
            )
            // Repère central
            drawLine(
                color = grip,
                start = Offset(knobLeft + 12.dp.toPx(), knobTop + knobH / 2f),
                end = Offset(knobLeft + knobW - 12.dp.toPx(), knobTop + knobH / 2f),
                strokeWidth = 3.dp.toPx(),
            )
        }
        // La valeur n'apparaît que pendant le toucher (la place est réservée : rien ne bouge).
        Text(
            text = if (touching) value.toString() else "",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.height(24.dp),
        )
    }
}

/** Gros bouton à deux états. Lisible par la couleur ET par le texte (« Leslie : rapide »). */
@Composable
fun SwitchButton(
    text: String,
    on: Boolean,
    onToggle: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val currentOnToggle by rememberUpdatedState(onToggle)
    val shape = RoundedCornerShape(12.dp)
    val background = if (on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val foreground = if (on) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    Column(
        modifier = modifier
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .heightIn(min = 56.dp)
            .clip(shape)
            .background(background)
            .border(2.dp, MaterialTheme.colorScheme.outline, shape)
            .testTag(testTag)
            .semantics { contentDescription = text }
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput // verrouillé : on n'écoute rien
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()
                    currentOnToggle() // dès le contact : plus réactif en live
                    waitForUpOrCancellation()
                }
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = text,
            color = foreground,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
        )
    }
}