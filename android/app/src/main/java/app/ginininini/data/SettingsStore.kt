package app.ginininini.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "ginininini_settings")

class SettingsStore(private val context: Context) {

    companion object {
        val KEY_BACKEND_URL = stringPreferencesKey("backend_url")
        val KEY_BACKEND_TOKEN = stringPreferencesKey("backend_token")
        val KEY_BUBBLE_SIZE = intPreferencesKey("bubble_size")
        val KEY_BUBBLE_SIDE = stringPreferencesKey("bubble_side")
        val KEY_EXPIRY_SECONDS = intPreferencesKey("expiry_seconds")
        val KEY_INBOX_PLAYLIST = stringPreferencesKey("inbox_playlist")
        val KEY_ARTISTS_PLAYLIST = stringPreferencesKey("artists_playlist")
        val KEY_HIDE_FULLSCREEN = booleanPreferencesKey("hide_fullscreen")
    }

    val backendUrl: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_BACKEND_URL] ?: "http://192.168.1.100:8000"
    }

    val backendToken: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_BACKEND_TOKEN] ?: "dev-token"
    }

    val bubbleSize: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_BUBBLE_SIZE] ?: 56
    }

    val bubbleSide: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_BUBBLE_SIDE] ?: "right"
    }

    val expirySeconds: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_EXPIRY_SECONDS] ?: 60
    }

    val inboxPlaylist: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_INBOX_PLAYLIST] ?: "Discovery Inbox"
    }

    val artistsPlaylist: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_ARTISTS_PLAYLIST] ?: "Artists to Explore"
    }

    val hideInFullscreen: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_HIDE_FULLSCREEN] ?: true
    }

    suspend fun setBackendUrl(url: String) {
        context.dataStore.edit { it[KEY_BACKEND_URL] = url }
    }

    suspend fun setBackendToken(token: String) {
        context.dataStore.edit { it[KEY_BACKEND_TOKEN] = token }
    }

    suspend fun setBubbleSize(size: Int) {
        context.dataStore.edit { it[KEY_BUBBLE_SIZE] = size }
    }

    suspend fun setBubbleSide(side: String) {
        context.dataStore.edit { it[KEY_BUBBLE_SIDE] = side }
    }

    suspend fun setExpirySeconds(seconds: Int) {
        context.dataStore.edit { it[KEY_EXPIRY_SECONDS] = seconds }
    }

    suspend fun setInboxPlaylist(name: String) {
        context.dataStore.edit { it[KEY_INBOX_PLAYLIST] = name }
    }

    suspend fun setArtistsPlaylist(name: String) {
        context.dataStore.edit { it[KEY_ARTISTS_PLAYLIST] = name }
    }

    suspend fun setHideInFullscreen(hide: Boolean) {
        context.dataStore.edit { it[KEY_HIDE_FULLSCREEN] = hide }
    }
}
