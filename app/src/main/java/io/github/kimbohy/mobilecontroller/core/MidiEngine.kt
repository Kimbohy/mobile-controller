package io.github.kimbohy.mobilecontroller.core

/**
 * Cerveau MIDI : filtre les doublons et construit les messages.
 * À appeler depuis le thread d'interface uniquement ; l'écriture sur le port
 * est déléguée au transport (qui ne bloque jamais).
 */
class MidiEngine(
    private val transport: MidiTransport,
    channel: Int = 1,
) {
    /** Dernière valeur envoyée pour chaque numéro de CC (-1 = jamais envoyé). */
    private val lastSent = IntArray(128) { -1 }

    var channel: Int = channel.coerceIn(1, 16)
        private set

    /** Change le canal ; on oublie les valeurs envoyées pour tout renvoyer proprement. */
    fun setChannel(newChannel: Int) {
        channel = newChannel.coerceIn(1, 16)
        resetFilter()
    }

    /** Oublie tout ce qui a été envoyé (changement de mapping, de canal...). */
    fun resetFilter() {
        lastSent.fill(-1)
    }

    /**
     * Envoie un Control Change seulement si la valeur a changé (F10).
     * @return true si un message est réellement parti.
     */
    fun sendControlChange(cc: Int, value: Int): Boolean = send(cc, value, force = false)

    /** Envoie un Control Change même si la valeur n'a pas changé. */
    fun forceControlChange(cc: Int, value: Int): Boolean = send(cc, value, force = true)

    /**
     * Renvoie toutes ces paires (cc, valeur) quoi qu'il arrive : Sync et rappel de preset.
     * @return le nombre de messages réellement partis (0 si le câble est débranché).
     */
    fun resendAll(entries: List<Pair<Int, Int>>): Int =
        entries.count { (cc, value) -> forceControlChange(cc, value) }

    private fun send(cc: Int, value: Int, force: Boolean): Boolean {
        val safeCc = cc.coerceIn(0, 127)
        val safeValue = value.coerceIn(ValueQuantizer.MIN, ValueQuantizer.MAX)
        if (!force && lastSent[safeCc] == safeValue) return false

        val sent = transport.send(MidiMessages.controlChange(channel, safeCc, safeValue))
        // On ne mémorise que ce qui est vraiment parti : si le câble était débranché,
        // la même valeur sera renvoyée au prochain mouvement après reconnexion.
        if (sent) lastSent[safeCc] = safeValue
        return sent
    }
}