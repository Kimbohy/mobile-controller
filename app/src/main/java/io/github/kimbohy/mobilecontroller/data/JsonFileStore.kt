package io.github.kimbohy.mobilecontroller.data

import io.github.kimbohy.mobilecontroller.model.AppJson
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import java.io.File
import java.io.IOException

/** Lit/écrit un objet dans un fichier JSON. Robuste : ne lève jamais d'exception. */
class JsonFileStore<T>(
    private val file: File,
    private val serializer: KSerializer<T>,
    /** Adapte un ancien fichier au format courant avant le décodage. */
    private val migrate: (JsonObject) -> JsonObject = { it },
    private val defaultValue: () -> T,
) {
    /** Fichier absent ou illisible → valeur par défaut (on ne plante jamais au démarrage). */
    fun load(): T = try {
        if (file.exists()) {
            val root = AppJson.parseToJsonElement(file.readText()).jsonObject
            AppJson.decodeFromJsonElement(serializer, migrate(root))
        } else {
            defaultValue()
        }
    } catch (e: Exception) {
        defaultValue()
    }

    /** Écriture atomique : fichier temporaire, puis renommage. @return true si réussi. */
    @Synchronized
    fun save(value: T): Boolean = try {
        file.parentFile?.mkdirs()
        val tmp = File(file.parentFile, file.name + ".tmp")
        tmp.writeText(AppJson.encodeToString(serializer, value))
        if (!tmp.renameTo(file)) {
            file.delete()
            tmp.renameTo(file)
        } else {
            true
        }
    } catch (e: IOException) {
        false
    }
}