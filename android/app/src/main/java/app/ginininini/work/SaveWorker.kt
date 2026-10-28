package app.ginininini.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.ginininini.data.AppDatabase
import app.ginininini.data.SettingsStore
import app.ginininini.net.BackendApi
import kotlinx.coroutines.flow.first

class SaveWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getInstance(applicationContext)
        val settingsStore = SettingsStore(applicationContext)

        val url = settingsStore.backendUrl.first()
        val token = settingsStore.backendToken.first()
        val backendApi = BackendApi(url, token)

        val pending = database.saveDao().getPendingSaves()
        if (pending.isEmpty()) {
            return Result.success()
        }

        var anyFailed = false

        for (item in pending) {
            val vid = item.videoId
            if (vid != null) {
                val res = backendApi.saveSong(vid, item.title, item.artist, item.sourcePkg)
                res.onSuccess {
                    database.saveDao().markSynced(item.id, "synced", it.playlistId, it.setVideoId)
                }.onFailure {
                    anyFailed = true
                }
            }
        }

        return if (anyFailed) Result.retry() else Result.success()
    }
}
