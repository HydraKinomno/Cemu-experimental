package info.cemu.cemu.emulation.externaldisplay

import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.provider.Settings
import android.view.Display
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.content.getSystemService

private fun DisplayManager.getPresentationDisplay(): Display? =
    getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION).firstOrNull { it.isValid }

/**
 * Tracks the display Cemu can present a second screen on, which is any presentation display
 * reported by the system: Miracast/wireless display, cast or a wired external screen.
 * The value is null while no such display is connected.
 */
@Composable
fun rememberExternalDisplay(context: Context): State<Display?> {
    val displayManager = remember(context) { context.getSystemService<DisplayManager>() }
    val externalDisplay = remember { mutableStateOf(displayManager?.getPresentationDisplay()) }

    DisposableEffect(displayManager) {
        if (displayManager == null) {
            return@DisposableEffect onDispose {}
        }

        val listener = object : DisplayManager.DisplayListener {
            private fun update() {
                externalDisplay.value = displayManager.getPresentationDisplay()
            }

            override fun onDisplayAdded(displayId: Int) = update()
            override fun onDisplayRemoved(displayId: Int) = update()
            override fun onDisplayChanged(displayId: Int) = update()
        }

        displayManager.registerDisplayListener(listener, null)
        externalDisplay.value = displayManager.getPresentationDisplay()

        onDispose { displayManager.unregisterDisplayListener(listener) }
    }

    return externalDisplay
}

fun Context.openWirelessDisplaySettings() {
    val intents = listOf(
        Intent(Settings.ACTION_CAST_SETTINGS),
        Intent(Settings.ACTION_DISPLAY_SETTINGS),
    )

    for (intent in intents) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
            return
        }
    }
}
