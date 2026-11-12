package io.github.kimbohy.mobilecontroller.data

import kotlinx.serialization.Serializable

/** Réglages de l'utilisateur (settings.json). */
@Serializable
data class Settings(
    val version: Int = 1,
    val midiChannel: Int = 1,
    /** Passe à true dès que la liaison a fonctionné une fois : l'assistant disparaît pour toujours. */
    val onboardingDone: Boolean = false,
)

/** Dernier état des contrôles (state.json) : id du contrôle → valeur 0..127. */
@Serializable
data class SavedState(
    val version: Int = 1,
    val values: Map<String, Int> = emptyMap(),
)