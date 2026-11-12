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
        lastSent.fill(-1)
    }

    /**
     * Envoie un Control Change seulement si la valeur a changé (F10).
     * @return true si un message est réellement parti.
     */
    fun sendControlChange(cc: Int, value: Int): Boolean {
        val safeCc = cc.coerceIn(0, 127)
        val safeValue = value.coerceIn(ValueQuantizer.MIN, ValueQuantizer.MAX)
        if (lastSent[safeCc] == safeValue) return false

        val sent = transport.send(MidiMessages.controlChange(channel, safeCc, safeValue))
        // On ne mémorise que ce qui est vraiment parti : si le câble était débranché,
        // la même valeur sera renvoyée au prochain mouvement après reconnexion.
        if (sent) lastSent[safeCc] = safeValue
        return sent
    }
}