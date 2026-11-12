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
import io.github.kimbohy.mobilecontroller.ui.OnboardingScreen
import io.github.kimbohy.mobilecontroller.ui.PerformanceScreen
import io.github.kimbohy.mobilecontroller.ui.SettingsScreen
import io.github.kimbohy.mobilecontroller.ui.theme.MobileControllerTheme

@Composable
fun MobileControllerApp(vm: MainViewModel) {
    val settings by vm.settings.collectAsState()
    val values by vm.values.collectAsState()
    val connection by vm.connection.collectAsState()
    val usb by vm.usbState.collectAsState()
    var inSettings by rememberSaveable { mutableStateOf(false) }

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

                    inSettings -> {
                        BackHandler { inSettings = false }
                        SettingsScreen(
                            settings = settings,
                            connection = connection,
                            usb = usb,
                            onChannelChange = vm::setMidiChannel,
                            onRescan = vm::onAppForeground,
                            onBack = { inSettings = false },
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
                            onOpenSettings = { inSettings = true },
                        )
                    }
                }
            }
        }
    }
}