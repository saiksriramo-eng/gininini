package app.ginininini.net

import kotlinx.serialization.Serializable

@Serializable
data class HealthResponse(
    val ok: Boolean,
    val ytm_auth: Boolean
)

@Serializable
data class CandidateTrack(
    val videoId: String,
    val title: String,
    val artist: String,
    val album: String? = null,
    val duration: String? = null,
    val thumbnailUrl: String? = null
)

@Serializable
data class ResolveRequest(
    val text: String,
    val kind: String = "auto"
)

@Serializable
data class ResolveResponse(
    val best_match: CandidateTrack? = null,
    val candidates: List<CandidateTrack> = emptyList(),
    val cleaned_query: String
)

@Serializable
data class SaveSongRequest(
    val videoId: String,
    val title: String? = null,
    val artist: String? = null,
    val source_pkg: String = "unknown"
)

@Serializable
data class SaveSongResponse(
    val playlistId: String,
    val setVideoId: String? = null,
    val already_saved: Boolean = false,
    val videoId: String
)

@Serializable
data class SaveArtistRequest(
    val browseId: String,
    val artist: String? = null,
    val source_pkg: String = "unknown"
)

@Serializable
data class SaveArtistResponse(
    val playlistId: String,
    val artist: String,
    val songs_added: List<CandidateTrack> = emptyList()
)

@Serializable
data class UndoItem(
    val setVideoId: String? = null,
    val videoId: String? = null
)

@Serializable
data class UndoRequest(
    val playlistId: String,
    val items: List<UndoItem>
)

@Serializable
data class UndoResponse(
    val ok: Boolean,
    val removed_count: Int
)

@Serializable
data class LikeRequest(
    val title: String,
    val artist: String
)

@Serializable
data class LikeResponse(
    val ok: Boolean,
    val videoId: String? = null,
    val title: String,
    val artist: String
)

@Serializable
data class AddCurrentRequest(
    val title: String,
    val artist: String,
    val source_pkg: String = "youtube_music"
)

@Serializable
data class AddCurrentResponse(
    val playlistId: String,
    val setVideoId: String? = null,
    val videoId: String,
    val title: String,
    val artist: String
)
