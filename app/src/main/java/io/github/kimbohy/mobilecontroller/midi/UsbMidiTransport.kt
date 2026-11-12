package io.github.kimbohy.mobilecontroller.midi

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.media.midi.MidiDevice
import android.media.midi.MidiDeviceInfo
import android.media.midi.MidiInputPort
import android.media.midi.MidiManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import io.github.kimbohy.mobilecontroller.core.ConnectionState
import io.github.kimbohy.mobilecontroller.core.ErrorReason
import io.github.kimbohy.mobilecontroller.core.MidiTransport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.Executors

private const val TAG = "MobileController"
private const val RETRY_DELAY_MS = 1000L

/**
 * Transport MIDI par USB (téléphone en mode périphérique MIDI).
 *
 * Règle de thread : tout l'état interne est manipulé sur le thread PRINCIPAL.
 * Seule l'écriture sur le port se fait sur le thread "midi-writer".
 */
class UsbMidiTransport(context: Context) : MidiTransport {

    private val appContext = context.applicationContext
    private val isDebuggable =
        (appContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    private val midiManager: MidiManager? =
        if (appContext.packageManager.hasSystemFeature(PackageManager.FEATURE_MIDI)) {
            appContext.getSystemService(Context.MIDI_SERVICE) as? MidiManager
        } else {
            null
        }

    private val mainHandler = Handler(Looper.getMainLooper())
    private val writer = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "midi-writer")
    }

    private val _state = MutableStateFlow<ConnectionState>(ConnectionState.Waiting)
    override val state: StateFlow<ConnectionState> = _state.asStateFlow()

    // --- état interne : thread principal uniquement ---
    private var started = false
    private var opening = false
    private var device: MidiDevice? = null
    private var deviceId = -1
    private var port: MidiInputPort? = null

    private val callback = object : MidiManager.DeviceCallback() {
        override fun onDeviceAdded(info: MidiDeviceInfo) {
            tryConnect()
        }

        override fun onDeviceRemoved(info: MidiDeviceInfo) {
            if (info.id == deviceId) {
                Log.i(TAG, "USB MIDI disconnected")
                disconnect()
            }
            tryConnect() // un autre port est peut-être disponible
        }
    }

    override fun start() {
        val manager = midiManager
        if (manager == null) {
            _state.value = ConnectionState.Error(ErrorReason.NOT_SUPPORTED)
            return
        }
        if (started) return
        started = true
        manager.registerDeviceCallback(callback, mainHandler)
        tryConnect()
    }

    override fun stop() {
        if (!started) return
        started = false
        midiManager?.unregisterDeviceCallback(callback)
        mainHandler.removeCallbacksAndMessages(null)
        disconnect()
        writer.shutdown()
    }

    override fun rescan() {
        val manager = midiManager ?: return
        if (!started) return
        if (port != null) {
            @Suppress("DEPRECATION")
            val stillThere = manager.devices.any { it.id == deviceId }
            if (!stillThere) disconnect()
        }
        tryConnect()
    }

    /** Non bloquant : met le message en file, le thread "midi-writer" l'écrit. */
    override fun send(message: ByteArray): Boolean {
        val currentPort = port ?: return false
        val enqueuedAt = System.nanoTime()
        try {
            writer.execute {
                try {
                    currentPort.send(message, 0, message.size)
                    if (isDebuggable) {
                        val micros = (System.nanoTime() - enqueuedAt) / 1000
                        Log.d(TAG, "latency=$micros")
                    }
                } catch (e: Exception) {
                    mainHandler.post { onWriteFailed(currentPort) }
                }
            }
        } catch (e: Exception) {
            return false // executor arrêté
        }
        return true
    }

    // ------------------------------------------------------------------

    private fun tryConnect() {
        val manager = midiManager ?: return
        if (!started || opening || port != null) return

        val info = pickDevice(manager)
        if (info == null) {
            _state.value = ConnectionState.Waiting
            return
        }
        opening = true
        manager.openDevice(
            info,
            MidiManager.OnDeviceOpenedListener { opened -> onDeviceOpened(info, opened) },
            mainHandler,
        )
    }

    private fun onDeviceOpened(info: MidiDeviceInfo, opened: MidiDevice?) {
        opening = false
        if (!started) {
            runCatching { opened?.close() }
            return
        }
        val newPort = opened?.openInputPort(0)
        if (opened == null || newPort == null) {
            runCatching { opened?.close() }
            Log.w(TAG, "Could not open MIDI port of '${nameOf(info)}'")
            _state.value = ConnectionState.Error(ErrorReason.OPEN_FAILED)
            scheduleRetry()
            return
        }
        device = opened
        deviceId = info.id
        port = newPort
        Log.i(TAG, "USB MIDI connected: ${nameOf(info)}")
        _state.value = ConnectionState.Connected(nameOf(info))
    }

    private fun onWriteFailed(failedPort: MidiInputPort) {
        if (port !== failedPort) return // déjà géré
        Log.w(TAG, "Write failed, closing the port")
        disconnect()
        scheduleRetry()
    }

    private fun disconnect() {
        val oldPort = port
        val oldDevice = device
        port = null
        device = null
        deviceId = -1
        runCatching { oldPort?.close() }
        runCatching { oldDevice?.close() }
        _state.value = ConnectionState.Waiting
    }

    private fun scheduleRetry() {
        mainHandler.postDelayed({ tryConnect() }, RETRY_DELAY_MS)
    }

    /** Préfère le port « Peripheral » (liaison vers le PC), sinon un autre port USB. */
    private fun pickDevice(manager: MidiManager): MidiDeviceInfo? {
        @Suppress("DEPRECATION")
        val candidates = manager.devices.filter {
            it.inputPortCount > 0 && it.type != MidiDeviceInfo.TYPE_VIRTUAL
        }
        return candidates.firstOrNull { nameOf(it).contains("peripheral", ignoreCase = true) }
            ?: candidates.firstOrNull { it.type == MidiDeviceInfo.TYPE_USB }
    }

    private fun nameOf(info: MidiDeviceInfo): String =
        info.properties.getString(MidiDeviceInfo.PROPERTY_NAME) ?: "MIDI"
}