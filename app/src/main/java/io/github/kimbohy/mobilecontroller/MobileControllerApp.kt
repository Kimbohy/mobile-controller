package io.github.kimbohy.mobilecontroller

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.github.kimbohy.mobilecontroller.ui.MainViewModel
import io.github.kimbohy.mobilecontroller.ui.MappingScreen
import io.github.kimbohy.mobilecontroller.ui.OnboardingScreen
import io.github.kimbohy.mobilecontroller.ui.PerformanceScreen
import io.github.kimbohy.mobilecontroller.ui.SettingsScreen
import io.github.kimbohy.mobilecontroller.ui.theme.MobileControllerTheme

private enum class Screen { PERFORMANCE, SETTINGS, MAPPING }

@Composable
fun MobileControllerApp(vm: MainViewModel) {
    val settings by vm.settings.collectAsState()
    val values by vm.values.collectAsState()
    val connection by vm.connection.collectAsState()
    val usb by vm.usbState.collectAsState()
    val mapping by vm.mapping.collectAsState()
    val usedPresets by vm.usedPresets.collectAsState()
    val activePreset by vm.activePreset.collectAsState()
    val locked by vm.locked.collectAsState()
    var screen by rememberSaveable { mutableStateOf(Screen.PERFORMANCE) }

    MobileControllerTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(
                Modifier
                    .fillMaxSize()
                    .systemGestureExclusion()
                    .windowInsetsPadding(WindowInsets.safeDrawing),
            ) {
                when {
                    !settings.onboardingDone -> OnboardingScreen(
                        connection = connection,
                        usb = usb,
                        onSkip = vm::skipOnboarding,
                    )

                    screen == Screen.MAPPING -> {
                        BackHandler { screen = Screen.SETTINGS }
                        MappingScreen(
                            layout = vm.layout,
                            profiles = vm.profiles.profiles,
                            selectedProfileId = vm.profiles.byId(settings.mappingProfileId).id,
                            mapping = mapping,
                            overrides = settings.ccOverrides,
                            onSelectProfile = vm::selectProfile,
                            onCcChange = vm::setCc,
                            onInvertChange = vm::setInvertDrawbars,
                            onReset = vm::resetMapping,
                            onBack = { screen = Screen.SETTINGS },
                        )
                    }

                    screen == Screen.SETTINGS -> {
                        BackHandler { screen = Screen.PERFORMANCE }
                        SettingsScreen(
                            settings = settings,
                            connection = connection,
                            usb = usb,
                            onChannelChange = vm::setMidiChannel,
                            onRescan = vm::onAppForeground,
                            onBack = { screen = Screen.PERFORMANCE },
                            onOpenMapping = { screen = Screen.MAPPING },
                        )
                    }

                    else -> {
                        BackHandler { /* Retour ignoré pendant la performance */ }
                        PerformanceScreen(
                            layout = vm.layout,
                            values = values,
                            connection = connection,
                            onValueChange = vm::onValueChange,
                            onToggle = vm::onToggle,
                            onOpenSettings = { screen = Screen.SETTINGS },
                            locked = locked,
                            activePreset = activePreset,
                            usedPresets = usedPresets,
                            onRecallPreset = vm::recallPreset,
                            onSavePreset = vm::savePreset,
                            onSync = vm::sync,
                            onToggleLock = vm::toggleLock,
                        )
                    }
                }
            }
        }
    }
}