package app.ginininini.overlay

import android.content.Context
import android.graphics.Rect
import android.view.WindowManager
import android.view.WindowMetrics

class FullscreenWatcher(private val context: Context) {

    fun isFullscreenActive(): Boolean {
        // Simple check based on available display bounds vs screen metrics
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            val metrics: WindowMetrics = wm.currentWindowMetrics
            val bounds: Rect = metrics.bounds
            // If insets indicate hidden system bars, it's fullscreen
            val insets = metrics.windowInsets.getInsetsIgnoringVisibility(
                android.view.WindowInsets.Type.systemBars()
            )
            return insets.top == 0 || insets.bottom == 0
        }
        return false
    }
}
