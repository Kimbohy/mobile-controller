package io.github.kimbohy.mobilecontroller.model

import android.content.Context
import io.github.kimbohy.mobilecontroller.R

/** Les profils de mapping embarqués dans l'APK (res/raw/mapping_profiles.json). */
object BuiltInProfiles {
    fun load(context: Context): MappingProfiles {
        val text = context.resources.openRawResource(R.raw.mapping_profiles)
            .bufferedReader()
            .use { it.readText() }
        return MappingProfilesCodec.decode(text)
    }
}