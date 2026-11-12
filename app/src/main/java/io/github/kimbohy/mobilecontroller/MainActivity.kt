package io.github.kimbohy.mobilecontroller

import android.content.Context
import android.media.midi.MidiDeviceInfo
import android.media.midi.MidiInputPort
import android.media.midi.MidiManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/** SPIKE JETABLE : vérifie que le téléphone peut envoyer du MIDI par USB. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val midi = getSystemService(Context.MIDI_SERVICE) as MidiManager
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(Modifier.fillMaxSize()) { SpikeScreen(midi) }
            }
        }
    }
}

@Composable
private fun SpikeScreen(midi: MidiManager) {
    var report by remember { mutableStateOf("Appuie sur « 1. Lister les ports MIDI ».") }
    var port by remember { mutableStateOf<MidiInputPort?>(null) }
    var value by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(onClick = { report = describeDevices(midi) }) {
            Text("1. Lister les ports MIDI")
        }

        Button(onClick = {
            val info = pickDevice(midi)
            if (info == null) {
                report = "Aucun port MIDI avec entrée. Vérifie le mode USB = MIDI."
            } else {
                midi.openDevice(info, { device ->
                    val opened = device?.openInputPort(0)
                    port = opened
                    report = if (opened != null) {
                        "Port ouvert sur « ${nameOf(info)} ». Bouge le slider."
                    } else {
                        "Échec d'ouverture du port."
                    }
                }, Handler(Looper.getMainLooper()))
            }
        }) {
            Text("2. Ouvrir le port (préfère « Peripheral »)")
        }

        Slider(
            value = value,
            onValueChange = {
                value = it
                sendControlChange(port, cc = 20, value = it.roundToInt())
            },
            valueRange = 0f..127f,
        )
        Text("Envoi : canal 1, CC 20 = ${value.roundToInt()}")
        Text(report)
    }
}

private fun nameOf(info: MidiDeviceInfo): String =
    info.properties.getString(MidiDeviceInfo.PROPERTY_NAME) ?: "(sans nom)"

@Suppress("DEPRECATION")
private fun pickDevice(midi: MidiManager): MidiDeviceInfo? {
    val withInput = midi.devices.filter { it.inputPortCount > 0 }
    return withInput.firstOrNull { nameOf(it).contains("peripheral", ignoreCase = true) }
        ?: withInput.firstOrNull()
}

@Suppress("DEPRECATION")
private fun describeDevices(midi: MidiManager): String {
    val devices = midi.devices
    if (devices.isEmpty()) return "Aucun appareil MIDI visible par l'app."
    return devices.joinToString("\n\n") { d ->
        "id=${d.id}  type=${d.type}\nnom=${nameOf(d)}\nports: ${d.inputPortCount} entrée(s), ${d.outputPortCount} sortie(s)"
    }
}

private fun sendControlChange(port: MidiInputPort?, cc: Int, value: Int) {
    try {
        // 0xB0 = Control Change sur le canal 1
        port?.send(byteArrayOf(0xB0.toByte(), cc.toByte(), value.toByte()), 0, 3)
    } catch (e: Exception) {
        Log.w("Spike", "Envoi impossible", e)
    }
}