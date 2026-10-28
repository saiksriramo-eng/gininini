# Ginininini Permissions Guide

This document catalogs every permission declared in `app/src/main/AndroidManifest.xml`, explaining the architectural necessity, security controls, and how to enable them on Android 16 (API 36).

---

## 1. Declared Permissions Breakdown

| Permission | Protection Level | Purpose in Ginininini | Privacy & Safeguards |
| :--- | :--- | :--- | :--- |
| `android.permission.SYSTEM_ALERT_WINDOW` | Signature / Dangerous (`AppOps`) | Allows `BubbleService` to anchor a `WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY` view over any application. | Configured with `FLAG_NOT_FOCUSABLE` so touches and text selections in apps underneath remain active. |
| `android.permission.FOREGROUND_SERVICE` | Normal | Allows the overlay service to run continuously and display an ongoing status notification. | Stops automatically when user taps the notification "Stop" action or toggles off the Quick Settings tile. |
| `android.permission.FOREGROUND_SERVICE_SPECIAL_USE` | Normal | Required on Android 14+ (API 34+) for overlay floating controls that do not map to standard media/location categories. | Manifest specifies `PROPERTY_SPECIAL_USE_FGS_SUBTYPE` documenting the floating utility overlay purpose. |
| `android.permission.BIND_ACCESSIBILITY_SERVICE` | Signature / Restricted | Bound to `SelectionAccessibilityService` to detect text selection changes in foreground apps (e.g., Reddit, Chrome). | Scoped strictly to text selection events; never reads password nodes; never logs screen hierarchies or persists text until user taps "Save". |
| `android.permission.BIND_NOTIFICATION_LISTENER_SERVICE` | Signature / Restricted (Phase 3) | Bound to `YtMusicListenerService` to detect active YouTube Music playback sessions and synchronize now-playing state. | Only inspects playback state and track metadata from YouTube Music (`com.google.android.apps.youtube.music`). |
| `android.permission.POST_NOTIFICATIONS` | Runtime (Android 13+) | Displays the mandatory foreground service notification containing the instant "Stop" action. | Keeps the service visible and transparent to the user. |
| `android.permission.INTERNET` | Normal | Communicates with the local laptop FastAPI backend (`http://<laptop-ip>:8000`). | Cleartext HTTP restricted strictly to debug builds via `network_security_config.xml`. |
| `android.permission.ACCESS_NETWORK_STATE` | Normal | Monitored by `WorkManager` to trigger synchronization of queued saves when Wi-Fi is reachable. | Read-only network status check. |

---

## 2. Sideloading & Restricted Settings on Android 13–16

Beginning in Android 13 and reinforced in Android 15/16 (Enhanced Confirmation Mode), Google blocks high-privilege permissions (specifically **Accessibility Services** and **Notification Listeners**) for apps installed via sideloading (browsers, file managers, ADB).

### How to Enable on Your Moto g86 power 5G:
1. Open phone **Settings** -> **Apps** -> **See all apps**.
2. Tap **Ginininini**.
3. Tap the **three-dot menu** in the top-right corner.
4. Tap **Allow restricted settings**.
5. Authenticate with your fingerprint, face unlock, or PIN.
6. Return to Ginininini's Permissions screen or phone **Accessibility** settings and toggle on **Ginininini Selection Capture**.

*(Note: Installing via ADB with `adb install -r -g` may bypass certain confirmation prompts, but if any setting remains disabled or greyed out, the above step unlocks it immediately.)*
