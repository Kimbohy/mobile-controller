package io.github.kimbohy.mobilecontroller.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.kimbohy.mobilecontroller.core.ConnectionState
import io.github.kimbohy.mobilecontroller.core.MidiEngine
import io.github.kimbohy.mobilecontroller.core.MidiTransport
import io.github.kimbohy.mobilecontroller.core.ValueQuantizer
import io.github.kimbohy.mobilecontroller.data.JsonFileStore
import io.github.kimbohy.mobilecontroller.data.SavedState
import io.github.kimbohy.mobilecontroller.data.Settings
import io.github.kimbohy.mobilecontroller.midi.UsbHostState
import io.github.kimbohy.mobilecontroller.midi.UsbMidiTransport
import io.github.kimbohy.mobilecontroller.midi.UsbStateMonitor
import io.github.kimbohy.mobilecontroller.model.BuiltInLayout
import io.github.kimbohy.mobilecontroller.model.Layout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val layout: Layout = BuiltInLayout.load(application)
    private val specsById = layout.controls.associateBy { it.id }

    private val settingsStore = JsonFileStore(
        File(application.filesDir, "settings.json"), Settings.serializer(),
    ) { Settings() }
    private val stateStore = JsonFileStore(
        File(application.filesDir, "state.json"), SavedState.serializer(),
    ) { SavedState() }

    private val transport: MidiTransport = UsbMidiTransport(application)
    private val usbMonitor = UsbStateMonitor(application)
    private val engine = MidiEngine(transport)

    private val _settings = MutableStateFlow(settingsStore.load())
    val settings: StateFlow<Settings> = _settings.asStateFlow()

    /** SOURCE DE VÉRITÉ : id du contrôle → valeur 0..127. L'interface ne fait que l'afficher. */
    private val _values = MutableStateFlow(restore(stateStore.load()))
    val values: StateFlow<Map<String, Int>> = _values.asStateFlow()

    val connection: StateFlow<ConnectionState> = transport.state
    val usbState: StateFlow<UsbHostState> = usbMonitor.state

    private var saveJob: Job? = null

    init {
        engine.setChannel(_settings.value.midiChannel)
        usbMonitor.start()
        transport.start()

        // Dès que la liaison fonctionne une première fois, l'assistant disparaît pour toujours.
        viewModelScope.launch {
            transport.state.collect { state ->
                if (state is ConnectionState.Connected && !_settings.value.onboardingDone) {
                    updateSettings { it.copy(onboardingDone = true) }
                }
            }
        }
    }

    /** Valeurs sauvegardées, complétées par les valeurs par défaut du layout, bornées à 0..127. */
    private fun restore(saved: SavedState): Map<String, Int> =
        layout.controls.associate { spec ->
            spec.id to (saved.values[spec.id] ?: spec.defaultValue())
                .coerceIn(ValueQuantizer.MIN, ValueQuantizer.MAX)
        }

    /** Une tirette ou un fader a bougé. Appelé sur le thread d'interface. */
    fun onValueChange(controlId: String, value: Int) {
        val spec = specsById[controlId] ?: return
        val safe = value.coerceIn(ValueQuantizer.MIN, ValueQuantizer.MAX)
        if (_values.value[controlId] == safe) return

        engine.sendControlChange(spec.cc, safe) // d'abord le MIDI (latence), ensuite l'affichage
        _values.update { it + (controlId to safe) }
        scheduleSave()
    }

    /** Un switch a été touché : 0 ↔ 127. */
    fun onToggle(controlId: String) {
        val current = _values.value[controlId] ?: 0
        onValueChange(controlId, if (current >= 64) 0 else 127)
    }

    fun setMidiChannel(channel: Int) {
        val safe = channel.coerceIn(1, 16)
        engine.setChannel(safe)
        updateSettings { it.copy(midiChannel = safe) }
    }

    fun skipOnboarding() = updateSettings { it.copy(onboardingDone = true) }

    /** L'app revient au premier plan : on relance une recherche de port par sécurité. */
    fun onAppForeground() = transport.rescan()

    /** Écriture immédiate (appelée quand l'app quitte le premier plan). */
    fun saveNow() {
        saveJob?.cancel()
        stateStore.save(SavedState(values = _values.value))
    }

    private fun scheduleSave() {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(500) // on attend que le doigt s'arrête
            val snapshot = SavedState(values = _values.value)
            withContext(Dispatchers.IO) { stateStore.save(snapshot) }
        }
    }

    private fun updateSettings(transform: (Settings) -> Settings) {
        val updated = transform(_settings.value)
        _settings.value = updated
        settingsStore.save(updated) // fichier minuscule : écriture immédiate, ordre garanti
    }

    override fun onCleared() {
        saveNow()
        transport.stop()
        usbMonitor.stop()
        super.onCleared()
    }
}