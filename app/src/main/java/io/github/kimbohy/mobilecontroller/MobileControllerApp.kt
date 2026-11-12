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
import androidx.compose.ui.Modifier
import io.github.kimbohy.mobilecontroller.ui.MainViewModel
import io.github.kimbohy.mobilecontroller.ui.PerformanceScreen
import io.github.kimbohy.mobilecontroller.ui.theme.MobileControllerTheme

@Composable
fun MobileControllerApp(vm: MainViewModel) {
    val values by vm.values.collectAsState()
    val connection by vm.connection.collectAsState()

    MobileControllerTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(
                Modifier
                    .fillMaxSize()
                    .systemGestureExclusion() // gestes système exclus sur les bords (cahier §6)
                    .windowInsetsPadding(WindowInsets.safeDrawing), // évite l'encoche de la caméra
            ) {
                BackHandler { /* Retour ignoré pendant la performance */ }
                PerformanceScreen(
                    layout = vm.layout,
                    values = values,
                    connection = connection,
                    onValueChange = vm::onValueChange,
                    onToggle = vm::onToggle,
                    onOpenSettings = { /* branché à l'étape 8 */ },
                )
            }
        }
    }
}