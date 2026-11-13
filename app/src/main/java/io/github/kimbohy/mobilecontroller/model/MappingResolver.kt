package io.github.kimbohy.mobilecontroller.model

object ControlMapper {
    /**
     * Valeur d'écran (0..127) → valeur MIDI à envoyer.
     * - tirette : inversée si demandé ;
     * - switch : valeur « off » ou « on » du mapping ;
     * - fader : inchangé.
     */
    fun toCcValue(type: ControlType, uiValue: Int, mapping: ControlMapping, invertDrawbars: Boolean): Int {
        val v = uiValue.coerceIn(0, 127)
        return when (type) {
            ControlType.SWITCH -> if (v >= 64) mapping.onValue else mapping.offValue
            ControlType.DRAWBAR -> if (invertDrawbars) 127 - v else v
            ControlType.FADER -> v
        }
    }
}

/** Mapping effectif pour tous les contrôles du layout. */
data class ResolvedMapping(
    val invertDrawbars: Boolean,
    val controls: Map<String, ControlMapping>,
) {
    fun mappingFor(spec: ControlSpec): ControlMapping = controls.getValue(spec.id)

    /** Valeur MIDI à envoyer pour la valeur d'écran [uiValue] de ce contrôle. */
    fun ccValue(spec: ControlSpec, uiValue: Int): Int =
        ControlMapper.toCcValue(spec.type, uiValue, mappingFor(spec), invertDrawbars)
}

object MappingResolver {

    /** CC d'un contrôle d'après le profil seul (le layout sert de secours si le profil l'ignore). */
    fun profileCc(profile: MappingProfile, spec: ControlSpec): Int =
        profile.controls[spec.id]?.cc ?: spec.cc

    fun resolve(
        layout: Layout,
        profile: MappingProfile,
        ccOverrides: Map<String, Int>,
        invertOverride: Boolean?,
    ): ResolvedMapping {
        val controls = layout.controls.associate { spec ->
            val base = profile.controls[spec.id] ?: ControlMapping(cc = spec.cc)
            val cc = ccOverrides[spec.id]?.coerceIn(0, 127) ?: base.cc
            spec.id to base.copy(cc = cc)
        }
        return ResolvedMapping(
            invertDrawbars = invertOverride ?: profile.invertDrawbars,
            controls = controls,
        )
    }
}