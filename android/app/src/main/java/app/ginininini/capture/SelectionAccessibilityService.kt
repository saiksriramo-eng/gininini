package app.ginininini.capture

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class SelectionAccessibilityService : AccessibilityService() {

    private var currentForegroundPackage: String = "unknown"

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                event.packageName?.let {
                    currentForegroundPackage = it.toString()
                }
            }
            AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED -> {
                if (event.isPassword) return

                val sourceNode: AccessibilityNodeInfo? = event.source
                if (sourceNode != null && sourceNode.isPassword) {
                    sourceNode.recycle()
                    return
                }

                val pkg = event.packageName?.toString() ?: currentForegroundPackage
                val textList = event.text
                if (textList.isNotEmpty()) {
                    val fullText = textList.joinToString(" ")
                    val fromIndex = event.fromIndex
                    val toIndex = event.toIndex

                    val selected = if (fromIndex >= 0 && toIndex > fromIndex && toIndex <= fullText.length) {
                        fullText.substring(fromIndex, toIndex)
                    } else {
                        fullText
                    }

                    if (selected.isNotBlank()) {
                        SelectionCache.update(selected, pkg)
                    }
                }
                sourceNode?.recycle()
            }
        }
    }

    override fun onInterrupt() {
        // Accessibility service interrupted
    }
}
