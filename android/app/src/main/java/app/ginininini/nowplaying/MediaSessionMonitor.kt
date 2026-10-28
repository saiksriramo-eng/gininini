package app.ginininini.nowplaying

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState

class MediaSessionMonitor(private val context: Context) {

    data class TrackInfo(
        val title: String,
        val artist: String,
        val isPlaying: Boolean
    )

    private val sessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager

    fun getActiveYtMusicTrack(listenerComponent: ComponentName): TrackInfo? {
        try {
            val controllers = sessionManager.getActiveSessions(listenerComponent)
            for (controller in controllers) {
                if (controller.packageName == "com.google.android.apps.youtube.music") {
                    val metadata = controller.metadata
                    val playbackState = controller.playbackState
                    if (metadata != null) {
                        val title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE) ?: ""
                        val artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: ""
                        val isPlaying = playbackState?.state == PlaybackState.STATE_PLAYING
                        if (title.isNotEmpty()) {
                            return TrackInfo(title, artist, isPlaying)
                        }
                    }
                }
            }
        } catch (e: SecurityException) {
            // Notification listener access not granted yet
        }
        return null
    }
}
