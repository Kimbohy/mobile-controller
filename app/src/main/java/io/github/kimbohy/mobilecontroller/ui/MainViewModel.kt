package io.github.kimbohy.mobilecontroller.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.kimbohy.mobilecontroller.core.ConnectionState
import io.github.kimbohy.mobilecontroller.core.MidiEngine
import io.github.kimbohy.mobilecontroller.core.MidiTransport
import io.github.kimbohy.mobilecontroller.core.ValueQuantizer
import io.github.kimbohy.mobilecontroller.data.JsonFileStore
import io.github.kimbohy.mobilecontroller.data.PresetStore
import io.github.kimbohy.mobilecontroller.data.SavedState
import io.github.kimbohy.mobilecontroller.data.Settings
import io.github.kimbohy.mobilecontroller.data.SettingsMigration
import io.github.kimbohy.mobilecontroller.midi.UsbHostState
import io.github.kimbohy.mobilecontroller.midi.UsbMidiTransport
import io.github.kimbohy.mobilecontroller.midi.UsbStateMonitor
import io.github.kimbohy.mobilecontroller.model.BuiltInLayout
import io.github.kimbohy.mobilecontroller.model.BuiltInProfiles
import io.github.kimbohy.mobilecontroller.model.Layout
import io.github.kimbohy.mobilecontroller.model.MappingProfiles
import io.github.kimbohy.mobilecontroller.model.MappingResolver
import io.github.kimbohy.mobilecontroller.model.PresetOps
import io.github.kimbohy.mobilecontroller.model.ResolvedMapping
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
    val profiles: MappingProfiles = BuiltInProfiles.load(application)
    private val specsById = layout.controls.associateBy { it.id }

    private val settingsStore = JsonFileStore(
        File(application.filesDir, "settings.json"),
        Settings.serializer(),
        migrate = SettingsMigration::migrate,
    ) { Settings() }
    private val stateStore = JsonFileStore(
        File(application.filesDir, "state.json"), SavedState.serializer(),
    ) { SavedState() }
    private val presetStore = PresetStore(File(application.filesDir, "presets"))

    private val transport: MidiTransport = UsbMidiTransport(application)
    private val usbMonitor = UsbStateMonitor(application)
    private val engine = MidiEngine(transport)

    private val _settings = MutableStateFlow(settingsStore.load())
    val settings: StateFlow<Settings> = _settings.asStateFlow()

    /** Mapping effectif : profil + CC modifiés + inversion. Recalculé à chaque changement de réglage. */
    private val _mapping = MutableStateFlow(resolve(_settings.value))
    val mapping: StateFlow<ResolvedMapping> = _mapping.asStateFlow()

    /** SOURCE DE VÉRITÉ : id du contrôle → valeur d'écran 0..127. L'interface ne fait que l'afficher. */
    private val _values = MutableStateFlow(restore(stateStore.load()))
    val values: StateFlow<Map<String, Int>> = _values.asStateFlow()

    private val _usedPresets = MutableStateFlow(presetStore.usedSlots())
    val usedPresets: StateFlow<Set<Int>> = _usedPresets.asStateFlow()

    /** Dernier preset rappelé ou sauvegardé, tant que rien n'a bougé depuis. */
    private val _activePreset = MutableStateFlow<Int?>(null)
    val activePreset: StateFlow<Int?> = _activePreset.asStateFlow()

    /** Verrou tactile. Volontairement non sauvegardé : l'app redémarre toujours déverrouillée. */
    private val _locked = MutableStateFlow(false)
    val locked: StateFlow<Boolean> = _locked.asStateFlow()

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

    // ------------------------------------------------------------------
    // Mapping

    private fun resolve(s: Settings): ResolvedMapping =
        MappingResolver.resolve(layout, profiles.byId(s.mappingProfileId), s.ccOverrides, s.invertDrawbars)

    /** Valeurs sauvegardées, complétées par les valeurs par défaut du layout, bornées à 0..127. */
    private fun restore(saved: SavedState): Map<String, Int> =
        layout.controls.associate { spec ->
            spec.id to (saved.values[spec.id] ?: spec.defaultValue())
                .coerceIn(ValueQuantizer.MIN, ValueQuantizer.MAX)
        }

    /** Les paires (cc, valeur MIDI) de tous les contrôles pour un état donné. */
    private fun entriesFor(values: Map<String, Int>): List<Pair<Int, Int>> {
        val m = _mapping.value
        return layout.controls.map { spec ->
            m.mappingFor(spec).cc to m.ccValue(spec, values[spec.id] ?: spec.defaultValue())
        }
    }

    /** CC du profil choisi pour ce contrôle, sans les modifications de l'utilisateur. */
    fun profileCcFor(controlId: String): Int {
        val spec = specsById[controlId] ?: return 0
        return MappingResolver.profileCc(profiles.byId(_settings.value.mappingProfileId), spec)
    }

    /** Choisit un profil. Les CC modifiés et l'inversion personnalisée sont effacés. */
    fun selectProfile(profileId: String) {
        updateSettings { it.copy(mappingProfileId = profileId, ccOverrides = emptyMap(), invertDrawbars = null) }
    }

    /** F12 : change le CC d'un contrôle. Si c'est celui du profil, la modification est retirée. */
    fun setCc(controlId: String, cc: Int) {
        if (controlId !in specsById) return
        val safe = cc.coerceIn(0, 127)
        val base = profileCcFor(controlId)
        updateSettings { s ->
            s.copy(ccOverrides = if (safe == base) s.ccOverrides - controlId else s.ccOverrides + (controlId to safe))
        }
    }

    fun setInvertDrawbars(invert: Boolean) {
        val profileValue = profiles.byId(_settings.value.mappingProfileId).invertDrawbars
        updateSettings { it.copy(invertDrawbars = if (invert == profileValue) null else invert) }
    }

    /** Revient au profil tel quel : plus aucun CC modifié, inversion du profil. */
    fun resetMapping() {
        updateSettings { it.copy(ccOverrides = emptyMap(), invertDrawbars = null) }
    }

    // ------------------------------------------------------------------
    // Contrôles

    /** Une tirette ou un fader a bougé. Appelé sur le thread d'interface. */
    fun onValueChange(controlId: String, value: Int) {
        if (_locked.value) return
        val spec = specsById[controlId] ?: return
        val safe = value.coerceIn(ValueQuantizer.MIN, ValueQuantizer.MAX)
        if (_values.value[controlId] == safe) return

        // D'abord le MIDI (latence), ensuite l'affichage.
        val m = _mapping.value
        engine.sendControlChange(m.mappingFor(spec).cc, m.ccValue(spec, safe))
        _values.update { it + (controlId to safe) }
        _activePreset.value = null
        scheduleSave()
    }

    /** Un switch a été touché : 0 ↔ 127. */
    fun onToggle(controlId: String) {
        val current = _values.value[controlId] ?: 0
        onValueChange(controlId, if (current >= 64) 0 else 127)
    }

    // ------------------------------------------------------------------
    // F13 Sync

    /** Renvoie toutes les valeurs courantes au VST, même si elles n'ont pas changé. */
    fun sync() {
        if (_locked.value) return
        engine.resendAll(entriesFor(_values.value))
    }

    // ------------------------------------------------------------------
    // F20 Presets

    /** Sauvegarde l'état courant dans l'emplacement [slot] (1..8). Aucune confirmation (pas de fenêtre en live). */
    fun savePreset(slot: Int) {
        if (_locked.value || slot !in 1..PresetStore.SLOTS) return
        val preset = PresetOps.capture(layout, _values.value, PresetOps.defaultName(slot))
        if (presetStore.save(slot, preset)) {
            _usedPresets.update { it + slot }
            _activePreset.value = slot
        }
    }

    /** Rappelle le preset [slot] : les contrôles reprennent leurs positions et le VST suit (tout est renvoyé). */
    fun recallPreset(slot: Int) {
        if (_locked.value || slot !in 1..PresetStore.SLOTS) return
        val preset = presetStore.load(slot) ?: return
        val updated = PresetOps.apply(layout, _values.value, preset)
        _values.value = updated
        engine.resendAll(entriesFor(updated))
        _activePreset.value = slot
        scheduleSave()
    }

    // ------------------------------------------------------------------
    // F21 Verrou

    fun toggleLock() {
        _locked.update { !it }
    }

    // ------------------------------------------------------------------
    // Réglages et cycle de vie

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
        _mapping.value = resolve(updated)
        engine.resetFilter() // les numéros ont peut-être changé : on repart d'un filtre vierge
        settingsStore.save(updated) // fichier minuscule : écriture immédiate, ordre garanti
    }

    override fun onCleared() {
        saveNow()
        transport.stop()
        usbMonitor.stop()
        super.onCleared()
    }
}