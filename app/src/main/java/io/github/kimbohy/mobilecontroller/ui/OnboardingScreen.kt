package io.github.kimbohy.mobilecontroller.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kimbohy.mobilecontroller.R
import io.github.kimbohy.mobilecontroller.core.ConnectionState
import io.github.kimbohy.mobilecontroller.midi.UsbHostState

@Composable
fun OnboardingScreen(
    connection: ConnectionState,
    usb: UsbHostState,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val connected = connection is ConnectionState.Connected
    // Si le port MIDI est ouvert, le câble et le mode MIDI sont forcément bons.
    val cableOk = usb.cableConnected || connected
    val midiModeOk = usb.midiMode || connected

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(stringResource(R.string.onboarding_title), style = MaterialTheme.typography.headlineMedium)
        StepRow(1, stringResource(R.string.onboarding_step1), cableOk)
        StepRow(2, stringResource(R.string.onboarding_step2), midiModeOk)
        StepRow(3, stringResource(R.string.onboarding_step3), connected)
        ConnectionIndicator(connection)
        TextButton(onClick = onSkip, modifier = Modifier.testTag("onboarding_skip")) {
            Text(stringResource(R.string.onboarding_skip))
        }
    }
}

@Composable
private fun StepRow(number: Int, text: String, done: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.testTag("onboarding_step_$number"),
    ) {
        Text(
            text = if (done) "✓" else number.toString(),
            fontSize = 22.sp,
            color = if (done) Color(0xFF3DDC84) else MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.width(40.dp),
        )
        Text(text = text, fontSize = 18.sp)
    }
}