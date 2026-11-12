package io.github.kimbohy.mobilecontroller.midi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Ce que le système dit de la liaison USB (meilleur effort, sert uniquement à guider l'utilisateur). */
data class UsbHostState(
    val cableConnected: Boolean = false,
    val midiMode: Boolean = false,
)

class UsbStateMonitor(context: Context) {
    private val appContext = context.applicationContext
    private val _state = MutableStateFlow(UsbHostState())
    val state: StateFlow<UsbHostState> = _state.asStateFlow()
    private var registered = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            publish(intent)
        }
    }

    private fun publish(intent: Intent) {
        _state.value = UsbHostState(
            cableConnected = intent.getBooleanExtra("connected", false),
            midiMode = intent.getBooleanExtra("midi", false),
        )
    }

    fun start() {
        if (registered) return
        try {
            // Ce broadcast est « sticky » : on reçoit tout de suite le dernier état connu.
            val sticky = ContextCompat.registerReceiver(
                appContext,
                receiver,
                IntentFilter("android.hardware.usb.action.USB_STATE"),
                ContextCompat.RECEIVER_NOT_EXPORTED,
            )
            registered = true
            sticky?.let { publish(it) }
        } catch (e: Exception) {
            // Si le système refuse, on continue sans : l'assistant se base alors sur le port MIDI.
        }
    }

    fun stop() {
        if (!registered) return
        registered = false
        runCatching { appContext.unregisterReceiver(receiver) }
    }
}