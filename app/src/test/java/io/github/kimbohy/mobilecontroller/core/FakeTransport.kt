package io.github.kimbohy.mobilecontroller.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Faux transport : enregistre ce qu'on lui envoie, sans aucun matériel. */
class FakeTransport(var accepting: Boolean = true) : MidiTransport {
    /** Chaque message reçu, en liste d'entiers 0..255 (ex. [0xB0, 20, 64]). */
    val sent = mutableListOf<List<Int>>()

    private val _state = MutableStateFlow<ConnectionState>(ConnectionState.Waiting)
    override val state: StateFlow<ConnectionState> = _state

    override fun start() = Unit
    override fun stop() = Unit
    override fun rescan() = Unit

    override fun send(message: ByteArray): Boolean {
        if (!accepting) return false
        sent += message.map { it.toInt() and 0xFF }
        return true
    }
}