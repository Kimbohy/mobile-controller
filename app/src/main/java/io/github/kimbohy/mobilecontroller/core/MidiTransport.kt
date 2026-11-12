package io.github.kimbohy.mobilecontroller.core

import kotlinx.coroutines.flow.StateFlow

/**
 * Tuyau par lequel partent les messages MIDI.
 * MVP : USB uniquement. Le pont Wi-Fi de la v2 sera une autre implémentation.
 */
interface MidiTransport {
    /** État courant de la liaison. */
    val state: StateFlow<ConnectionState>

    /** Commence à chercher un port et à se (re)connecter tout seul. Thread principal. */
    fun start()

    /** Arrête tout et libère les ressources. Thread principal. Ne pas rappeler start() après. */
    fun stop()

    /** Redemande une recherche (ex. quand l'app revient au premier plan). */
    fun rescan()

    /**
     * Envoie un message brut. **Ne bloque jamais** (l'écriture réelle se fait ailleurs).
     * @return false si aucune liaison n'est disponible (le message est alors perdu).
     */
    fun send(message: ByteArray): Boolean
}