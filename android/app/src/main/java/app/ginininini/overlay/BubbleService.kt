package app.ginininini.overlay

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import app.ginininini.GininininiApp
import app.ginininini.R
import app.ginininini.capture.SelectionCache
import app.ginininini.data.AppDatabase
import app.ginininini.data.SaveEntity
import app.ginininini.data.SettingsStore
import app.ginininini.net.BackendApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BubbleService : Service() {

    companion object {
        const val ACTION_START = "app.ginininini.action.START"
        const val ACTION_STOP = "app.ginininini.action.STOP"
        private const val NOTIFICATION_ID = 1001
    }

    private lateinit var windowManager: WindowManager
    private var bubbleView: BubbleView? = null
    private var arcMenuView: ArcMenuView? = null
    private lateinit var bubbleParams: WindowManager.LayoutParams
    private lateinit var menuParams: WindowManager.LayoutParams

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var settingsStore: SettingsStore
    private lateinit var database: AppDatabase
    private lateinit var backendApi: BackendApi

    private var isLeftEdge = false
    private var isMenuOpen = false

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        settingsStore = SettingsStore(this)
        database = AppDatabase.getInstance(this)
        backendApi = BackendApi("http://192.168.1.100:8000", "dev-token")

        initViews()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    private fun initViews() {
        val size = (56 * resources.displayMetrics.density).toInt()

        bubbleParams = WindowManager.LayoutParams(
            size,
            size,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = resources.displayMetrics.widthPixels - size
            y = resources.displayMetrics.heightPixels / 3
        }

        bubbleView = BubbleView(this).apply {
            onPositionChangedListener = { x, _, isLeft ->
                isLeftEdge = isLeft
            }
            onTapListener = {
                toggleArcMenu()
            }
            setOnTouchListener { _, event ->
                handleTouchEvent(event, bubbleParams, windowManager)
            }
        }

        menuParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        arcMenuView = ArcMenuView(this).apply {
            onDismissListener = {
                hideArcMenu()
            }
        }

        windowManager.addView(bubbleView, bubbleParams)
    }

    private fun toggleArcMenu() {
        if (isMenuOpen) {
            hideArcMenu()
        } else {
            showArcMenu()
        }
    }

    private fun showArcMenu() {
        val menu = arcMenuView ?: return
        val bubble = bubbleView ?: return

        serviceScope.launch {
            val expiry = settingsStore.expirySeconds.first()
            val selection = SelectionCache.getValid(expiry)

            val items = if (selection != null) {
                // Capture Mode
                listOf(
                    ArcMenuView.MenuItem("save_song", R.drawable.ic_save_song, "Save Song") {
                        handleSaveSong(selection.text, selection.sourcePkg)
                    },
                    ArcMenuView.MenuItem("save_artist", R.drawable.ic_save_artist, "Save Artist") {
                        handleSaveArtist(selection.text, selection.sourcePkg)
                    },
                    ArcMenuView.MenuItem("play_now", R.drawable.ic_play_now, "Play Now") {
                        handlePlayNow(selection.text)
                    }
                )
            } else {
                // Now-Playing Mode
                listOf(
                    ArcMenuView.MenuItem("like", R.drawable.ic_like, "Like") {
                        handleLike()
                    },
                    ArcMenuView.MenuItem("inbox", R.drawable.ic_inbox, "Add Current") {
                        handleAddCurrent()
                    },
                    ArcMenuView.MenuItem("skip", R.drawable.ic_skip, "Skip") {
                        handleSkip()
                    }
                )
            }

            if (menu.parent == null) {
                windowManager.addView(menu, menuParams)
            }
            menu.visibility = View.VISIBLE
            isMenuOpen = true

            val bubbleCenterX = (bubbleParams.x + bubble.width / 2f)
            val bubbleCenterY = (bubbleParams.y + bubble.height / 2f)
            menu.showButtons(items, bubbleCenterX, bubbleCenterY, isLeftEdge)
        }
    }

    private fun hideArcMenu() {
        arcMenuView?.let {
            if (it.parent != null) {
                windowManager.removeView(it)
            }
        }
        isMenuOpen = false
    }

    private fun handleSaveSong(text: String, sourcePkg: String) {
        serviceScope.launch {
            val url = settingsStore.backendUrl.first()
            val token = settingsStore.backendToken.first()
            backendApi.updateConfig(url, token)

            Toast.makeText(this@BubbleService, "Saving: $text", Toast.LENGTH_SHORT).show()
            val resolveRes = backendApi.resolve(text, "song")
            resolveRes.onSuccess { res ->
                val best = res.best_match
                if (best != null) {
                    val saveRes = backendApi.saveSong(best.videoId, best.title, best.artist, sourcePkg)
                    saveRes.onSuccess {
                        database.saveDao().insertSave(
                            SaveEntity(
                                text = text,
                                videoId = best.videoId,
                                title = best.title,
                                artist = best.artist,
                                playlistId = it.playlistId,
                                setVideoId = it.setVideoId,
                                sourcePkg = sourcePkg,
                                kind = "song",
                                syncStatus = "synced"
                            )
                        )
                        Toast.makeText(this@BubbleService, "Saved: ${best.title}", Toast.LENGTH_SHORT).show()
                    }.onFailure {
                        // Queue in Room for offline retry
                        database.saveDao().insertSave(
                            SaveEntity(text = text, videoId = best.videoId, title = best.title, artist = best.artist, sourcePkg = sourcePkg, syncStatus = "pending")
                        )
                        Toast.makeText(this@BubbleService, "Queued offline: ${best.title}", Toast.LENGTH_SHORT).show()
                    }
                }
            }.onFailure {
                Toast.makeText(this@BubbleService, "Resolve failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleSaveArtist(text: String, sourcePkg: String) {
        Toast.makeText(this, "Resolving artist: $text", Toast.LENGTH_SHORT).show()
    }

    private fun handlePlayNow(text: String) {
        Toast.makeText(this, "Playing: $text", Toast.LENGTH_SHORT).show()
    }

    private fun handleLike() {
        Toast.makeText(this, "Liked current track", Toast.LENGTH_SHORT).show()
    }

    private fun handleAddCurrent() {
        Toast.makeText(this, "Added current track to inbox", Toast.LENGTH_SHORT).show()
    }

    private fun handleSkip() {
        Toast.makeText(this, "Skipping track", Toast.LENGTH_SHORT).show()
    }

    private fun createNotification(): Notification {
        val stopIntent = Intent(this, BubbleService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, GininininiApp.OVERLAY_CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Floating overlay is active")
            .setSmallIcon(R.drawable.ic_tile)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.stop_service), stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        hideArcMenu()
        bubbleView?.let {
            if (it.parent != null) {
                windowManager.removeView(it)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
