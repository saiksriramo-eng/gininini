package app.ginininini

import app.ginininini.net.BackendApi
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class BackendApiMockTest {

    private lateinit var server: MockWebServer
    private lateinit var api: BackendApi

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val url = server.url("/").toString()
        api = BackendApi(url, "test-token")
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun testCheckHealthSuccess() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"ok": true, "ytm_auth": true}""")
        )

        val res = api.checkHealth()
        assertTrue(res.isSuccess)
        val data = res.getOrThrow()
        assertTrue(data.ok)
        assertTrue(data.ytm_auth)

        val recorded = server.takeRequest()
        assertEquals("/health", recorded.path)
        assertEquals("Bearer test-token", recorded.getHeader("Authorization"))
    }

    @Test
    fun testResolveText() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    {
                        "best_match": {
                            "videoId": "v123",
                            "title": "Namastute",
                            "artist": "Seedhe Maut"
                        },
                        "candidates": [],
                        "cleaned_query": "Namastute"
                    }
                    """.trimIndent()
                )
        )

        val res = api.resolve("Namastute", "song")
        assertTrue(res.isSuccess)
        val data = res.getOrThrow()
        assertEquals("v123", data.best_match?.videoId)
        assertEquals("Namastute", data.best_match?.title)
    }

    @Test
    fun testSaveSongSuccess() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    {
                        "playlistId": "pl_inbox",
                        "setVideoId": "set_1",
                        "already_saved": false,
                        "videoId": "v123"
                    }
                    """.trimIndent()
                )
        )

        val res = api.saveSong("v123", "Title", "Artist", "com.reddit.frontpage")
        assertTrue(res.isSuccess)
        val data = res.getOrThrow()
        assertEquals("pl_inbox", data.playlistId)
        assertFalse(data.already_saved)
    }
}
