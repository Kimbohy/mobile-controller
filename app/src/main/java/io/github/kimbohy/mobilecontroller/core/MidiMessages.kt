package io.github.kimbohy.mobilecontroller.core

object MidiMessages {
    private const val CONTROL_CHANGE_STATUS = 0xB0

    /**
     * Construit un Control Change : [statut + canal, numéro de CC, valeur].
     * @param channel canal MIDI de 1 à 16 (affiché à l'utilisateur)
     */
    fun controlChange(channel: Int, cc: Int, value: Int): ByteArray {
        require(channel in 1..16) { "MIDI channel must be 1..16 but was $channel" }
        require(cc in 0..127) { "CC number must be 0..127 but was $cc" }
        require(value in 0..127) { "CC value must be 0..127 but was $value" }
        return byteArrayOf(
            (CONTROL_CHANGE_STATUS or (channel - 1)).toByte(),
            cc.toByte(),
            value.toByte(),
        )
    }
}