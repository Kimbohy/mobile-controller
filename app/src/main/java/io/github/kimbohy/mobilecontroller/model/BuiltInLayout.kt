package io.github.kimbohy.mobilecontroller.model

import android.content.Context
import io.github.kimbohy.mobilecontroller.R

/** Le layout de l'orgue embarqué dans l'APK (res/raw/organ_layout.json). */
object BuiltInLayout {
    fun load(context: Context): Layout {
        val text = context.resources.openRawResource(R.raw.organ_layout)
            .bufferedReader()
            .use { it.readText() }
        return LayoutCodec.decode(text)
    }
}