package io.github.kimbohy.mobilecontroller

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.kimbohy.mobilecontroller.core.MidiEngine
import io.github.kimbohy.mobilecontroller.midi.UsbMidiTransport
import kotlin.math.roundToInt

/** ÉCRAN DE TEST TEMPORAIRE (remplacé à la phase 3). */
class MainActivity : ComponentActivity() {
    private lateinit var transport: UsbMidiTransport
    private lateinit var engine: MidiEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transport = UsbMidiTransport(this)
        engine = MidiEngine(transport)
        transport.start()
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(Modifier.fillMaxSize()) { DebugScreen(transport, engine) }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        transport.rescan()
    }

    override fun onDestroy() {
        transport.stop()
        super.onDestroy()
    }
}

@Composable
private fun DebugScreen(transport: UsbMidiTransport, engine: MidiEngine) {
    val state by transport.state.collectAsState()
    var value by remember { mutableFloatStateOf(0f) }

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("État : $state")
        Slider(
            value = value,
            onValueChange = {
                value = it
                engine.sendControlChange(cc = 20, value = it.roundToInt())
            },
            valueRange = 0f..127f,
        )
        Text("CC 20 = ${value.roundToInt()}")
    }
}