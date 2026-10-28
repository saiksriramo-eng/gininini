package app.ginininini.capture

object SelectionCache {
    data class CachedSelection(
        val text: String,
        val sourcePkg: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    @Volatile
    private var currentSelection: CachedSelection? = null

    fun update(text: String, sourcePkg: String) {
        val trimmed = text.trim()
        if (trimmed.isNotEmpty()) {
            currentSelection = CachedSelection(trimmed, sourcePkg)
        }
    }

    fun getValid(expirySeconds: Int): CachedSelection? {
        val sel = currentSelection ?: return null
        val age = (System.currentTimeMillis() - sel.timestamp) / 1000
        return if (age <= expirySeconds) sel else null
    }

    fun clear() {
        currentSelection = null
    }
}
