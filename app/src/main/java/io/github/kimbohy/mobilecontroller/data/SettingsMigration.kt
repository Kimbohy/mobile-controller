package io.github.kimbohy.mobilecontroller.data

import io.github.kimbohy.mobilecontroller.model.LEGACY_PROFILE_ID
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

object SettingsMigration {

    /** Transforme un settings.json ancien en version courante, avant décodage. */
    fun migrate(root: JsonObject): JsonObject {
        val version = root["version"]?.jsonPrimitive?.intOrNull ?: 1
        if (version >= Settings.CURRENT_VERSION) return root

        // 1 → 2 : la 1.0 utilisait le mapping CC 20–31. On le garde pour ne pas casser le MIDI Learn existant.
        return JsonObject(
            root + mapOf(
                "version" to JsonPrimitive(2),
                "mappingProfileId" to JsonPrimitive(LEGACY_PROFILE_ID),
            ),
        )
        // Quand une version 3 existera : enchaîner ici les migrations 2 → 3, etc.
    }
}