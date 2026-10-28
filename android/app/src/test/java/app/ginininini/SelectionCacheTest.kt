package app.ginininini

import app.ginininini.capture.SelectionCache
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SelectionCacheTest {

    @Before
    fun setUp() {
        SelectionCache.clear()
    }

    @Test
    fun testUpdateAndRetrieveValidSelection() {
        SelectionCache.update("Seedhe Maut - 101", "com.reddit.frontpage")
        val cached = SelectionCache.getValid(60)

        assertNotNull(cached)
        assertEquals("Seedhe Maut - 101", cached?.text)
        assertEquals("com.reddit.frontpage", cached?.sourcePkg)
    }

    @Test
    fun testBlankSelectionIgnored() {
        SelectionCache.update("   ", "com.reddit.frontpage")
        val cached = SelectionCache.getValid(60)
        assertNull(cached)
    }

    @Test
    fun testExpiredSelectionReturnsNull() {
        SelectionCache.update("Track Title", "com.android.chrome")
        // Expiry of 0 seconds should immediately consider it expired
        val cached = SelectionCache.getValid(0)
        assertNull(cached)
    }
}
