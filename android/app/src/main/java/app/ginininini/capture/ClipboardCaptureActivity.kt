package app.ginininini.capture

import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast

class ClipboardCaptureActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val text = clip.getItemAt(0).coerceToText(this).toString()
            if (text.isNotBlank()) {
                SelectionCache.update(text, "clipboard_fallback")
                Toast.makeText(this, "Captured from clipboard", Toast.LENGTH_SHORT).show()
            }
        }

        finish()
        overridePendingTransition(0, 0)
    }
}
