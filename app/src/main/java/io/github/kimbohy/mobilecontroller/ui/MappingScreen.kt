package io.github.kimbohy.mobilecontroller.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kimbohy.mobilecontroller.R
import io.github.kimbohy.mobilecontroller.model.Layout
import io.github.kimbohy.mobilecontroller.model.MappingProfile
import io.github.kimbohy.mobilecontroller.model.ResolvedMapping

@Composable
fun MappingScreen(
    layout: Layout,
    profiles: List<MappingProfile>,
    selectedProfileId: String,
    mapping: ResolvedMapping,
    overrides: Map<String, Int>,
    onSelectProfile: (String) -> Unit,
    onCcChange: (String, Int) -> Unit,
    onInvertChange: (Boolean) -> Unit,
    onReset: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = onBack, modifier = Modifier.testTag("mapping_back")) {
                Text(stringResource(R.string.back))
            }
            Spacer(Modifier.width(16.dp))
            Text(stringResource(R.string.mapping_title), style = MaterialTheme.typography.headlineSmall)
        }

        // --- Profil ---
        Text(stringResource(R.string.mapping_profile), style = MaterialTheme.typography.titleMedium)
        profiles.forEach { profile ->
            val tag = "profile_${profile.id}"
            if (profile.id == selectedProfileId) {
                Button(
                    onClick = { onSelectProfile(profile.id) },
                    modifier = Modifier.fillMaxWidth().testTag(tag),
                ) { Text(profile.name) }
            } else {
                OutlinedButton(
                    onClick = { onSelectProfile(profile.id) },
                    modifier = Modifier.fillMaxWidth().testTag(tag),
                ) { Text(profile.name) }
            }
        }

        // --- Inversion des tirettes ---
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.mapping_invert),
                fontSize = 16.sp,
                modifier = Modifier.weight(1f),
            )
            Switch(
                checked = mapping.invertDrawbars,
                onCheckedChange = onInvertChange,
                modifier = Modifier.testTag("invert_toggle"),
            )
        }

        // --- CC de chaque contrôle ---
        Text(stringResource(R.string.mapping_controls), style = MaterialTheme.typography.titleMedium)
        layout.controls.forEach { spec ->
            val cc = mapping.mappingFor(spec).cc
            val modified = spec.id in overrides
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (modified) "${spec.label} •" else spec.label,
                    fontSize = 18.sp,
                    color = if (modified) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                OutlinedButton(
                    onClick = { onCcChange(spec.id, cc - 1) },
                    enabled = cc > 0,
                    modifier = Modifier.testTag("cc_minus_${spec.id}"),
                ) { Text("−", fontSize = 20.sp) }
                Text(
                    text = cc.toString(),
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(60.dp).testTag("cc_value_${spec.id}"),
                )
                OutlinedButton(
                    onClick = { onCcChange(spec.id, cc + 1) },
                    enabled = cc < 127,
                    modifier = Modifier.testTag("cc_plus_${spec.id}"),
                ) { Text("+", fontSize = 20.sp) }
            }
        }

        Text(
            text = stringResource(R.string.mapping_sync_hint),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(onClick = onReset, modifier = Modifier.testTag("mapping_reset")) {
            Text(stringResource(R.string.mapping_reset))
        }
    }
}