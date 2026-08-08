package info.cemu.cemu.emulation.externaldisplay

import android.app.Presentation
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Display
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

/**
 * Shows one of the emulation surfaces on an external display, such as a Miracast receiver.
 */
class EmulationPresentation(
    context: Context,
    display: Display,
    private val holderCallback: SurfaceHolder.Callback,
) : Presentation(context, display, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val surfaceView = SurfaceView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            holder.addCallback(holderCallback)
        }

        val container = FrameLayout(context).apply {
            setBackgroundColor(Color.BLACK)
            addView(surfaceView)
        }

        setContentView(container)
    }
}

/**
 * Renders the given emulation surface on [display] for as long as this composable is in the
 * composition.
 */
@Composable
fun ExternalDisplaySurface(
    display: Display,
    holderCallback: SurfaceHolder.Callback,
    onDismissed: () -> Unit,
) {
    val context = LocalContext.current

    DisposableEffect(display, holderCallback) {
        val presentation = EmulationPresentation(context, display, holderCallback)
        presentation.setOnDismissListener { onDismissed() }
        presentation.show()

        onDispose {
            presentation.setOnDismissListener(null)
            presentation.dismiss()
        }
    }
}
