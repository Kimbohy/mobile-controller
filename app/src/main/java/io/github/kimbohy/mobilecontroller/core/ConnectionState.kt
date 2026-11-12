package io.github.kimbohy.mobilecontroller.core

/** État de la liaison MIDI affiché en permanence à l'utilisateur (F17). */
sealed interface ConnectionState {
    /** Aucun port MIDI USB trouvé : on attend (et on réessaie tout seul). */
    data object Waiting : ConnectionState

    /** Port ouvert : les messages partent vers le PC. */
    data class Connected(val deviceName: String) : ConnectionState

    /** Quelque chose a échoué. L'app réessaie automatiquement. */
    data class Error(val reason: ErrorReason) : ConnectionState
}

enum class ErrorReason {
    /** Le téléphone n'a pas de service MIDI. */
    NOT_SUPPORTED,

    /** Le port existe mais n'a pas pu être ouvert (déjà utilisé, etc.). */
    OPEN_FAILED,
}