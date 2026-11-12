package io.github.kimbohy.mobilecontroller.model

import kotlinx.serialization.json.Json

/** Configuration JSON partagée par tous les fichiers de l'app. */
val AppJson = Json {
    ignoreUnknownKeys = true   // tolère les champs ajoutés par des versions futures
    encodeDefaults = true      // écrit aussi les valeurs par défaut (ex. "version")
    prettyPrint = true         // fichiers lisibles et versionnables
}