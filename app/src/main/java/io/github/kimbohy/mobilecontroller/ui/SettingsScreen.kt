package io.github.kimbohy.mobilecontroller.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kimbohy.mobilecontroller.R
import io.github.kimbohy.mobilecontroller.core.ConnectionState
import io.github.kimbohy.mobilecontroller.data.Settings
import io.github.kimbohy.mobilecontroller.midi.UsbHostState

@Composable
fun SettingsScreen(
    settings: Settings,
    connection: ConnectionState,
    usb: UsbHostState,
    onChannelChange: (Int) -> Unit,
    onRescan: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = onBack, modifier = Modifier.testTag("settings_back")) {
                Text(stringResource(R.string.back))
            }
            Spacer(Modifier.width(16.dp))
            Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineSmall)
        }

        // --- Canal MIDI (1..16) ---
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.settings_midi_channel),
                fontSize = 18.sp,
                modifier = Modifier.weight(1f),
            )
            OutlinedButton(
                onClick = { onChannelChange(settings.midiChannel - 1) },
                enabled = settings.midiChannel > 1,
                modifier = Modifier.testTag("channel_minus"),
            ) { Text("−", fontSize = 20.sp) }
            Text(
                text = settings.midiChannel.toString(),
                fontSize = 22.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(56.dp).testTag("channel_value"),
            )
            OutlinedButton(
                onClick = { onChannelChange(settings.midiChannel + 1) },
                enabled = settings.midiChannel < 16,
                modifier = Modifier.testTag("channel_plus"),
            ) { Text("+", fontSize = 20.sp) }
        }

        // --- Connexion détaillée ---
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.settings_connection), style = MaterialTheme.typography.titleMedium)
            ConnectionIndicator(connection)
            Text(stringResource(R.string.settings_usb_cable, detected(usb.cableConnected)), fontSize = 14.sp)
            Text(stringResource(R.string.settings_usb_midi, detected(usb.midiMode)), fontSize = 14.sp)
            Text(
                stringResource(R.string.settings_help),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onRescan, modifier = Modifier.testTag("settings_rescan")) {
                Text(stringResource(R.string.settings_rescan))
            }
        }

        // --- Langue ---
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stringResource(R.string.settings_language), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.settings_language_value), fontSize = 14.sp)
        }
    }
}

@Composable
private fun detected(value: Boolean): String =
    stringResource(if (value) R.string.detected else R.string.not_detected)