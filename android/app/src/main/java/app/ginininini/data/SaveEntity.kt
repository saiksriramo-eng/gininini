package app.ginininini.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saves")
data class SaveEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val videoId: String? = null,
    val title: String? = null,
    val artist: String? = null,
    val playlistId: String? = null,
    val setVideoId: String? = null,
    val sourcePkg: String = "unknown",
    val kind: String = "song",
    val timestamp: Long = System.currentTimeMillis(),
    val syncStatus: String = "pending" // pending, synced, failed
)
