package io.github.kimbohy.mobilecontroller.model

import kotlinx.serialization.SerializationException

class ProfileException(message: String, cause: Throwable? = null) : Exception(message, cause)

/** Lecture et validation du fichier de profils. Lève ProfileException, ne plante jamais autrement. */
object MappingProfilesCodec {

    fun decode(text: String): MappingProfiles {
        val parsed = try {
            AppJson.decodeFromString(MappingProfiles.serializer(), text)
        } catch (e: SerializationException) {
            throw ProfileException("Invalid profiles file: ${e.message}", e)
        } catch (e: IllegalArgumentException) {
            throw ProfileException("Invalid profiles file: ${e.message}", e)
        }
        validate(parsed)
        return parsed
    }

    private fun validate(p: MappingProfiles) {
        if (p.version < 1 || p.version > MappingProfiles.CURRENT_VERSION) {
            throw ProfileException("Unsupported profiles version: ${p.version}")
        }
        if (p.profiles.isEmpty()) throw ProfileException("No profile defined")
        val ids = p.profiles.map { it.id }
        if (ids.size != ids.toSet().size) throw ProfileException("Two profiles share the same id")
        if (DEFAULT_PROFILE_ID !in ids) throw ProfileException("Default profile '$DEFAULT_PROFILE_ID' is missing")
        for (profile in p.profiles) {
            if (profile.id.isBlank()) throw ProfileException("A profile has an empty id")
            if (profile.controls.isEmpty()) throw ProfileException("Profile '${profile.id}' has no control")
            for ((controlId, m) in profile.controls) {
                if (m.cc !in 0..127) throw ProfileException("Invalid CC for '$controlId' in '${profile.id}': ${m.cc}")
                if (m.offValue !in 0..127 || m.onValue !in 0..127) {
                    throw ProfileException("Invalid on/off value for '$controlId' in '${profile.id}'")
                }
            }
        }
    }
}