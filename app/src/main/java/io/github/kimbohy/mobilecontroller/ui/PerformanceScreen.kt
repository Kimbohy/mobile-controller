package io.github.kimbohy.mobilecontroller.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kimbohy.mobilecontroller.R
import io.github.kimbohy.mobilecontroller.core.ConnectionState
import io.github.kimbohy.mobilecontroller.core.ErrorReason
import io.github.kimbohy.mobilecontroller.data.PresetStore
import io.github.kimbohy.mobilecontroller.model.ControlSpec
import io.github.kimbohy.mobilecontroller.model.ControlType
import io.github.kimbohy.mobilecontroller.model.Layout

/**
 * Écran principal : tout se joue ici, en paysage. Sans état propre → facile à tester.
 * Les paramètres v1.1 (presets, Sync, verrou) ont des valeurs par défaut neutres.
 */
@Composable
fun PerformanceScreen(
    layout: Layout,
    values: Map<String, Int>,
    connection: ConnectionState,
    onValueChange: (String, Int) -> Unit,
    onToggle: (String) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    locked: Boolean = false,
    activePreset: Int? = null,
    usedPresets: Set<Int> = emptySet(),
    onRecallPreset: (Int) -> Unit = {},
    onSavePreset: (Int) -> Unit = {},
    onSync: () -> Unit = {},
    onToggleLock: () -> Unit = {},
) {
    val drawbars = layout.controls.filter { it.type == ControlType.DRAWBAR }
    val switches = layout.controls.filter { it.type == ControlType.SWITCH }
    val faders = layout.controls.filter { it.type == ControlType.FADER }

    Column(modifier = modifier.fillMaxSize()) {
        TopBar(
            connection = connection,
            locked = locked,
            activePreset = activePreset,
            usedPresets = usedPresets,
            onRecallPreset = onRecallPreset,
            onSavePreset = onSavePreset,
            onSync = onSync,
            onToggleLock = onToggleLock,
            onOpenSettings = onOpenSettings,
        )

        if (connection is ConnectionState.Waiting) {
            // F18 : une phrase d'aide précise, jamais un code d'erreur.
            Text(
                text = stringResource(R.string.hint_no_port),
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Tirettes
            Row(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                drawbars.forEach { spec ->
                    Fader(
                        label = spec.label,
                        value = values[spec.id] ?: spec.defaultValue(),
                        knobColor = drawbarColor(spec.color),
                        onValueChange = { onValueChange(spec.id, it) },
                        testTag = "fader_${spec.id}",
                        enabled = !locked,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                }
            }

            // Switches Leslie / vibrato / percussion
            Column(
                modifier = Modifier.width(132.dp).fillMaxHeight().padding(top = 24.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                switches.forEach { spec ->
                    val on = (values[spec.id] ?: spec.defaultValue()) >= 64
                    SwitchButton(
                        text = switchText(spec, on),
                        on = on,
                        onToggle = { onToggle(spec.id) },
                        testTag = "switch_${spec.id}",
                        enabled = !locked,
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                    )
                }
            }

            // Volume
            faders.forEach { spec ->
                Fader(
                    label = spec.label,
                    value = values[spec.id] ?: spec.defaultValue(),
                    knobColor = MaterialTheme.colorScheme.primary,
                    onValueChange = { onValueChange(spec.id, it) },
                    testTag = "fader_${spec.id}",
                    enabled = !locked,
                    modifier = Modifier.width(72.dp).fillMaxHeight(),
                )
            }
        }
    }
}

@Composable
private fun TopBar(
    connection: ConnectionState,
    locked: Boolean,
    activePreset: Int?,
    usedPresets: Set<Int>,
    onRecallPreset: (Int) -> Unit,
    onSavePreset: (Int) -> Unit,
    onSync: () -> Unit,
    onToggleLock: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ConnectionIndicator(connection, Modifier.weight(1f))

        for (slot in 1..PresetStore.SLOTS) {
            PresetButton(
                slot = slot,
                used = slot in usedPresets,
                active = slot == activePreset,
                enabled = !locked,
                onRecall = { onRecallPreset(slot) },
                onSave = { onSavePreset(slot) },
            )
        }

        // Sync : un tap renvoie toutes les valeurs au VST.
        val syncLabel = stringResource(R.string.sync)
        val syncDescription = stringResource(R.string.sync_description)
        Box(
            modifier = Modifier
                .height(44.dp)
                .width(64.dp)
                .alpha(if (locked) 0.4f else 1f)
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                .testTag("sync_button")
                .semantics { contentDescription = syncDescription }
                .clickable(enabled = !locked, onClick = onSync),
            contentAlignment = Alignment.Center,
        ) {
            Text(syncLabel, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        }

        LockButton(locked = locked, onToggle = onToggleLock)

        // Icône réglages, discrète, dans le coin.
        val settingsLabel = stringResource(R.string.open_settings)
        Box(
            modifier = Modifier
                .size(40.dp)
                .alpha(if (locked) 0.4f else 1f)
                .testTag("open_settings")
                .semantics { contentDescription = settingsLabel }
                .clickable(enabled = !locked, onClick = onOpenSettings),
            contentAlignment = Alignment.Center,
        ) {
            Text("⚙", fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Preset : toucher = rappeler (si l'emplacement est plein), appui long = sauvegarder. */
@Composable
private fun PresetButton(
    slot: Int,
    used: Boolean,
    active: Boolean,
    enabled: Boolean,
    onRecall: () -> Unit,
    onSave: () -> Unit,
) {
    val currentRecall by rememberUpdatedState(onRecall)
    val currentSave by rememberUpdatedState(onSave)
    val shape = RoundedCornerShape(10.dp)
    val background = when {
        active -> MaterialTheme.colorScheme.primary
        used -> MaterialTheme.colorScheme.surfaceVariant
        else -> Color.Transparent
    }
    val foreground = when {
        active -> MaterialTheme.colorScheme.onPrimary
        used -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val description = stringResource(R.string.preset_description, slot)

    Box(
        modifier = Modifier
            .size(44.dp)
            .alpha(if (enabled) 1f else 0.4f)
            .clip(shape)
            .background(background)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .testTag("preset_$slot")
            .semantics { contentDescription = description }
            .pointerInput(enabled, used) {
                if (!enabled) return@pointerInput // verrouillé : on n'écoute rien
                detectTapGestures(
                    onTap = { if (used) currentRecall() },
                    onLongPress = { currentSave() },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(text = slot.toString(), fontSize = 18.sp, color = foreground, textAlign = TextAlign.Center)
    }
}

/** Verrou tactile : UNIQUEMENT par appui long (verrouiller comme déverrouiller). */
@Composable
private fun LockButton(locked: Boolean, onToggle: () -> Unit) {
    val currentToggle by rememberUpdatedState(onToggle)
    val shape = RoundedCornerShape(10.dp)
    val description = stringResource(R.string.lock_description)

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(shape)
            .background(if (locked) MaterialTheme.colorScheme.primary else Color.Transparent)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .testTag("lock_button")
            .semantics { contentDescription = description }
            .pointerInput(Unit) {
                detectTapGestures(onLongPress = { currentToggle() })
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(if (locked) "🔒" else "🔓", fontSize = 20.sp)
    }
}

/** Indicateur permanent d'état : point de couleur + texte (F17). */
@Composable
fun ConnectionIndicator(connection: ConnectionState, modifier: Modifier = Modifier) {
    val (color, text) = when (connection) {
        is ConnectionState.Connected ->
            Color(0xFF3DDC84) to stringResource(R.string.status_connected, connection.deviceName)
        ConnectionState.Waiting ->
            Color(0xFFFFB300) to stringResource(R.string.status_waiting)
        is ConnectionState.Error -> Color(0xFFFF5252) to stringResource(
            when (connection.reason) {
                ErrorReason.NOT_SUPPORTED -> R.string.status_error_unsupported
                ErrorReason.OPEN_FAILED -> R.string.status_error_open
            },
        )
    }
    Row(
        modifier = modifier.testTag("connection_indicator"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(12.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
    }
}

/** « Leslie : Rapide » : le nom du switch + son état traduit selon la langue du système. */
@Composable
private fun switchText(spec: ControlSpec, on: Boolean): String {
    val key = spec.states?.getOrNull(if (on) 1 else 0).orEmpty()
    return stringResource(R.string.switch_state_format, spec.label, stateLabel(key))
}

/** Traduit les clés d'état du layout intégré ; un texte libre (layout importé) est affiché tel quel. */
@Composable
private fun stateLabel(key: String): String = when (key) {
    "slow" -> stringResource(R.string.state_slow)
    "fast" -> stringResource(R.string.state_fast)
    "off" -> stringResource(R.string.state_off)
    "on" -> stringResource(R.string.state_on)
    else -> key
}