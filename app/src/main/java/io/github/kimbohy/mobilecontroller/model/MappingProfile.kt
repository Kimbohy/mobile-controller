package io.github.kimbohy.mobilecontroller.model

import kotlinx.serialization.Serializable

/** Profil choisi par défaut pour une nouvelle installation. */
const val DEFAULT_PROFILE_ID = "hammond-b3x"

/** Ancien mapping de la v1.0 (CC 20–31), conservé pour ne rien casser chez ceux qui ont fait leur MIDI Learn. */
const val LEGACY_PROFILE_ID = "legacy-1.0"

/** CC d'un contrôle, plus les valeurs envoyées par un switch (0 et 127 par défaut). */
@Serializable
data class ControlMapping(
    val cc: Int,
    val offValue: Int = 0,
    val onValue: Int = 127,
)

@Serializable
data class MappingProfile(
    val id: String,
    val name: String,
    /** true : une tirette en haut (valeur d'écran 127) envoie 0, comme setBfree. */
    val invertDrawbars: Boolean = false,
    /** id de contrôle du layout → mapping. */
    val controls: Map<String, ControlMapping>,
)

@Serializable
data class MappingProfiles(
    val version: Int = CURRENT_VERSION,
    val profiles: List<MappingProfile>,
) {
    /** Le profil demandé, ou le profil par défaut si l'identifiant est inconnu (jamais d'exception). */
    fun byId(id: String): MappingProfile =
        profiles.firstOrNull { it.id == id } ?: profiles.first { it.id == DEFAULT_PROFILE_ID }

    companion object {
        const val CURRENT_VERSION = 1
    }
}