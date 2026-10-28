package app.ginininini.nowplaying

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class YtMusicListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        // Monitored in Phase 3 for active media controls
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // Notification dismissed
    }
}
