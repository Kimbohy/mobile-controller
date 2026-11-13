package io.github.kimbohy.mobilecontroller.data

import io.github.kimbohy.mobilecontroller.model.DEFAULT_PROFILE_ID
import kotlinx.serialization.Serializable

/** Réglages de l'utilisateur (settings.json). Version 2 depuis la 1.1. */
@Serializable
data class Settings(
    val version: Int = CURRENT_VERSION,
    val midiChannel: Int = 1,
    /** Passe à true dès que la liaison a fonctionné une fois : l'assistant disparaît pour toujours. */
    val onboardingDone: Boolean = false,
    /** Profil de mapping choisi (voir mapping_profiles.json). */
    val mappingProfileId: String = DEFAULT_PROFILE_ID,
    /** CC modifiés par l'utilisateur : id du contrôle → CC (F12). Prioritaires sur le profil. */
    val ccOverrides: Map<String, Int> = emptyMap(),
    /** null = suivre le profil ; true/false = choix explicite de l'utilisateur. */
    val invertDrawbars: Boolean? = null,
) {
    companion object {
        const val CURRENT_VERSION = 2
    }
}

/** Dernier état des contrôles (state.json) : id du contrôle → valeur d'écran 0..127. */
@Serializable
data class SavedState(
    val version: Int = 1,
    val values: Map<String, Int> = emptyMap(),
)