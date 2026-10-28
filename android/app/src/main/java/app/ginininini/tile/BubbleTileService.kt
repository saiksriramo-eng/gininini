package app.ginininini.tile

import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import app.ginininini.overlay.BubbleService

class BubbleTileService : TileService() {

    override fun onClick() {
        super.onClick()
        val tile = qsTile ?: return

        if (tile.state == Tile.STATE_ACTIVE) {
            val stopIntent = Intent(this, BubbleService::class.java).apply {
                action = BubbleService.ACTION_STOP
            }
            startService(stopIntent)
            tile.state = Tile.STATE_INACTIVE
        } else {
            val startIntent = Intent(this, BubbleService::class.java).apply {
                action = BubbleService.ACTION_START
            }
            startForegroundService(startIntent)
            tile.state = Tile.STATE_ACTIVE
        }
        tile.updateTile()
    }

    override fun onStartListening() {
        super.onStartListening()
        val tile = qsTile ?: return
        tile.updateTile()
    }
}
