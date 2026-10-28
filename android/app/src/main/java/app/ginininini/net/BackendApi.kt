package app.ginininini.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

class BackendApi(
    private var baseUrl: String,
    private var token: String,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun updateConfig(baseUrl: String, token: String) {
        this.baseUrl = baseUrl.trimEnd('/')
        this.token = token
    }

    suspend fun checkHealth(): Result<HealthResponse> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/health")
                .header("Authorization", "Bearer $token")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(IOException("Server error: ${response.code}"))
                }
                val body = response.body?.string() ?: ""
                Result.success(json.decodeFromString<HealthResponse>(body))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resolve(text: String, kind: String = "auto"): Result<ResolveResponse> = withContext(Dispatchers.IO) {
        try {
            val payload = json.encodeToString(ResolveRequest(text, kind))
            val request = Request.Builder()
                .url("$baseUrl/resolve")
                .header("Authorization", "Bearer $token")
                .post(payload.toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(IOException("Resolve failed: ${response.code}"))
                }
                val body = response.body?.string() ?: ""
                Result.success(json.decodeFromString<ResolveResponse>(body))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveSong(
        videoId: String,
        title: String? = null,
        artist: String? = null,
        sourcePkg: String = "unknown"
    ): Result<SaveSongResponse> = withContext(Dispatchers.IO) {
        try {
            val payload = json.encodeToString(SaveSongRequest(videoId, title, artist, sourcePkg))
            val request = Request.Builder()
                .url("$baseUrl/save/song")
                .header("Authorization", "Bearer $token")
                .post(payload.toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(IOException("Save failed: ${response.code}"))
                }
                val body = response.body?.string() ?: ""
                Result.success(json.decodeFromString<SaveSongResponse>(body))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveArtist(
        browseId: String,
        artist: String? = null,
        sourcePkg: String = "unknown"
    ): Result<SaveArtistResponse> = withContext(Dispatchers.IO) {
        try {
            val payload = json.encodeToString(SaveArtistRequest(browseId, artist, sourcePkg))
            val request = Request.Builder()
                .url("$baseUrl/save/artist")
                .header("Authorization", "Bearer $token")
                .post(payload.toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(IOException("Save artist failed: ${response.code}"))
                }
                val body = response.body?.string() ?: ""
                Result.success(json.decodeFromString<SaveArtistResponse>(body))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun undo(playlistId: String, items: List<UndoItem>): Result<UndoResponse> = withContext(Dispatchers.IO) {
        try {
            val payload = json.encodeToString(UndoRequest(playlistId, items))
            val request = Request.Builder()
                .url("$baseUrl/undo")
                .header("Authorization", "Bearer $token")
                .post(payload.toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(IOException("Undo failed: ${response.code}"))
                }
                val body = response.body?.string() ?: ""
                Result.success(json.decodeFromString<UndoResponse>(body))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun like(title: String, artist: String): Result<LikeResponse> = withContext(Dispatchers.IO) {
        try {
            val payload = json.encodeToString(LikeRequest(title, artist))
            val request = Request.Builder()
                .url("$baseUrl/like")
                .header("Authorization", "Bearer $token")
                .post(payload.toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(IOException("Like failed: ${response.code}"))
                }
                val body = response.body?.string() ?: ""
                Result.success(json.decodeFromString<LikeResponse>(body))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addCurrent(title: String, artist: String): Result<AddCurrentResponse> = withContext(Dispatchers.IO) {
        try {
            val payload = json.encodeToString(AddCurrentRequest(title, artist))
            val request = Request.Builder()
                .url("$baseUrl/add-current")
                .header("Authorization", "Bearer $token")
                .post(payload.toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(IOException("Add current failed: ${response.code}"))
                }
                val body = response.body?.string() ?: ""
                Result.success(json.decodeFromString<AddCurrentResponse>(body))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
