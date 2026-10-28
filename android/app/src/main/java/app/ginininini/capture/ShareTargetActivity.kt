package app.ginininini.capture

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast

class ShareTargetActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!sharedText.isNullOrBlank()) {
                SelectionCache.update(sharedText, "share_sheet")
                Toast.makeText(this, "Captured from share", Toast.LENGTH_SHORT).show()
            }
        }

        finish()
        overridePendingTransition(0, 0)
    }
}
