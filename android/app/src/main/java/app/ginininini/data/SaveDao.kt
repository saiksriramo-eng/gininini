package app.ginininini.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SaveDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSave(entity: SaveEntity): Long

    @Query("SELECT * FROM saves ORDER BY timestamp DESC")
    fun getAllSaves(): Flow<List<SaveEntity>>

    @Query("SELECT * FROM saves WHERE syncStatus = 'pending' ORDER BY timestamp ASC")
    suspend fun getPendingSaves(): List<SaveEntity>

    @Query("UPDATE saves SET syncStatus = :status, playlistId = :playlistId, setVideoId = :setVideoId WHERE id = :id")
    suspend fun markSynced(id: Long, status: String, playlistId: String?, setVideoId: String?)

    @Query("UPDATE saves SET syncStatus = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("DELETE FROM saves WHERE id = :id")
    suspend fun deleteById(id: Long)
}
