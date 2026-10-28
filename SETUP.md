# Ginininini: Complete Project Guide & Phase Roadmap

Welcome to **Ginininini**! This document is your comprehensive operational guide for the entire project. It details your current setup, exact step-by-step instructions to run and test on your phone, and a deep-dive explanation of every development phase from Phase 0 to Phase 5.

---

## 1. Current Project Status & Completed Steps

* [x] **Repository:** Created and linked at [`https://github.com/saiksriramo-eng/gininini`](https://github.com/saiksriramo-eng/gininini).
* [x] **Platform-Tools (`adb`):** Configured and added to Windows user `PATH`.
* [x] **Android Studio:** Installer downloaded on laptop.
* [x] **Python Backend:** Core modules (`main.py`, `ytm.py`, `resolve.py`, `auth.py`, `store.py`) implemented, with all 7 unit/integration tests passing.
* [x] **Android Architecture:** Complete codebase scaffolded in `android/` (`BubbleService`, `SelectionAccessibilityService`, `AppDatabase`, Compose Settings/Permissions screens, network client).
* [ ] **Next Immediate Steps (Tomorrow):**
  1. Complete Android Studio installation.
  2. Download Android SDK Platform 36 (Android 16).
  3. Connect moto g86 power 5G via USB and verify `adb devices`.
  4. Complete YouTube Music browser authentication (`browser.json`).
  5. Run Phase 0 verification test.

---

## 2. Environment & Network Cheat Sheet

* **Target Device:** moto g86 power 5G (Android 16, API 36, software channel `retin`)
* **SDK Configurations:** `compileSdk 36`, `targetSdk 36`, `minSdk 34`
* **Application ID:** `app.ginininini`
* **Laptop Local Wi-Fi IP:** `10.16.94.22`
* **Backend Base URL for Phone:** `http://10.16.94.22:8000`

### Checking your IP anytime:
In PowerShell:
```powershell
(Get-NetIPAddress -AddressFamily IPv4 -InterfaceAlias "Wi-Fi*").IPAddress
```

### Allowing Inbound Traffic Through Windows Firewall:
Run once in PowerShell as Administrator:
```powershell
New-NetFirewallRule -DisplayName "Ginininini Backend" -Direction Inbound -LocalPort 8000 -Protocol TCP -Action Allow
```

---

## 3. How to Start Tomorrow: Step-by-Step

### Step 1: Install Android Studio
1. Run the downloaded `android-studio-....exe` installer with default settings.
2. Launch Android Studio.

### Step 2: Download Android SDK Platform 36 (Android 16)
1. On the Android Studio welcome screen, open **More Actions** (or gear icon) -> **SDK Manager** (or go to **Tools -> SDK Manager**).
2. Under the **SDK Platforms** tab, check the box for:
   - **Android 16.0 ("Baklava") / API Level 36**
3. Under the **SDK Tools** tab, verify **Android SDK Build-Tools 36** and **Android SDK Platform-Tools** are installed.
4. Click **Apply** and let Android Studio finish the download.

### Step 3: Connect Phone via USB Debugging
1. On your **moto g86 power 5G**:
   - Go to **Settings -> About phone** -> Tap **Build number** 7 times.
   - Go to **Settings -> System -> Developer options** -> Enable **USB debugging**.
2. Connect your phone to your laptop with a USB cable.
3. On your phone screen, accept the prompt: *"Allow USB debugging? Check 'Always allow from this computer' -> Allow"*.
4. Open PowerShell and verify:
   ```powershell
   adb devices
   ```
   *(Your phone serial number will show up with `device` next to it).*

### Step 4: Generate YouTube Music Credentials (`browser.json`)
1. On your laptop, open Firefox or Chrome and log into [music.youtube.com](https://music.youtube.com).
2. Press **`F12`** -> click the **Network** tab -> type `browse` in the filter box.
3. Click any song or playlist in YouTube Music.
4. Right-click the `browse` entry in the Network tab -> **Copy** -> **Copy Request Headers**.
5. In PowerShell, run:
   ```powershell
   mkdir -p "$HOME\.ginininini"
   python -c "from ytmusicapi import YTMusic; YTMusic.setup(filepath=r'$HOME\.ginininini\browser.json')"
   ```
6. Press `Ctrl + V` to paste your headers, press `Enter`, then press `Ctrl + Z` and press `Enter`.
7. Credentials are saved to `C:\Users\Admin\.ginininini\browser.json` (outside the repo).

### Step 5: Run the Phase 0 Verification Gate
In PowerShell:
```powershell
python d:\gininini\backend\scripts\smoke_test.py "Seedhe Maut 101"
```
**Gate Check:** Open YouTube Music on your phone -> **Library -> Playlists -> Discovery Inbox**. Confirm that **"101" by Seedhe Maut** is added!

---

## 4. Phase-by-Phase Roadmap (Phase 0 to Phase 5)

Each phase is designed to be built incrementally, verified with on-phone tests, and gated so you stay in total control.

```
┌─────────────────────────────────────────────────────────────┐
│ Phase 0: Setup & YTM Ingestion (Gate: Track in Inbox)       │
└──────────────────────────────┬──────────────────────────────┘
                               ▼
┌─────────────────────────────────────────────────────────────┐
│ Phase 1: Draggable Bubble & Arc Menu (Gate: Smooth overlay) │
└──────────────────────────────┬──────────────────────────────┘
                               ▼
┌─────────────────────────────────────────────────────────────┐
│ Phase 2: Highlight Capture & Save (Gate: Reddit/Chrome save)│
└──────────────────────────────┬──────────────────────────────┘
                               ▼
┌─────────────────────────────────────────────────────────────┐
│ Phase 3: Now-Playing Controls (Gate: Like / Add current)    │
└──────────────────────────────┬──────────────────────────────┘
                               ▼
┌─────────────────────────────────────────────────────────────┐
│ Phase 4: Quick Settings Tile & Compose (Gate: Shade toggle) │
└──────────────────────────────┬──────────────────────────────┘
                               ▼
┌─────────────────────────────────────────────────────────────┐
│ Phase 5: Artists, Auto-Clean & Hardening (Gate: Active mix) │
└─────────────────────────────────────────────────────────────┘
```

---

### Phase 0: Setup & Foundation
* **Goal:** Verify that the laptop toolchain, phone USB connection, and YouTube Music ingestion pipeline work seamlessly.
* **What is built:**
  - Full repo structure (`android/`, `backend/`, `docs/`, `scripts/`).
  - Python FastAPI backend with `ytmusicapi` browser authentication.
  - Smoke test script creating `Discovery Inbox` and pushing test tracks.
  - Base Android project compiling with API 36 SDK and installing to phone.
* **Gate Acceptance:** Test track appears in the YouTube Music app on your phone.

---

### Phase 1: Draggable Bubble & Spring Arc Menu
* **Goal:** A responsive, non-intrusive floating overlay that lives over all your apps without interfering with phone use.
* **What is built:**
  - `BubbleService`: Android Foreground Service (`specialUse` FGS type) with ongoing notification and one-tap "Stop" action.
  - `BubbleView`: Custom view in `TYPE_APPLICATION_OVERLAY` with `FLAG_NOT_FOCUSABLE`. Draggable, snaps cleanly to the nearest screen edge, and fades to semi-transparent when idle.
  - `ArcMenuView`: Fans out 4 round buttons in a semi-circle facing away from the screen edge using spring physics (`DynamicAnimation`).
  - Tap outside closes the menu instantly.
* **Gate Acceptance:** Bubble floats over Reddit and Chrome, drags smoothly, snaps to edges, and terminates immediately when "Stop" is tapped in the notification shade.

---

### Phase 2: Highlight Capture Without Copying & Inbox Saving
* **Goal:** Turn "I saw this song name in Reddit/Chrome" into "It is waiting in YouTube Music" in one single gesture.
* **What is built:**
  - `SelectionAccessibilityService`: Scopes strictly to text selection events. Reads highlighted song/artist text directly from the screen hierarchy without requiring you to copy it. Automatically ignores password fields.
  - `ClipboardCaptureActivity`: Transparent fallback activity that reads clipboard if an app uses a non-standard custom canvas view.
  - Backend `/resolve` and `/save/song` endpoints: Regex text cleaners that strip quotes, URLs, Reddit usernames (`u/...`), year tags, and video artifacts.
  - Local Room database & `WorkManager`: Saves queue up locally if you are away from home Wi-Fi and sync automatically when you reconnect.
  - In-app confirmation toast with an instant **Undo** button.
* **Gate Acceptance:** Highlight a song name in Reddit or Chrome, tap the bubble -> Save adds the song to `Discovery Inbox` in seconds. Tapping Undo removes it.

---

### Phase 3: Now-Playing YouTube Music Controls
* **Goal:** When no text is highlighted, the bubble dynamically becomes a controller for whatever is playing in YouTube Music.
* **What is built:**
  - `MediaSessionMonitor` & `YtMusicListenerService`: Detects active media playback from `com.google.android.apps.youtube.music`.
  - Dynamic Arc Menu Mode Switching:
    - If text was selected within the last 60 seconds -> **Capture Mode** (Save Song, Save Artist, Play Now).
    - If no recent text was selected -> **Now-Playing Mode** (Play/Pause, Skip, Like, Add Current Song to Inbox).
  - Backend `/like` and `/add-current` endpoints: Resolves currently playing title/artist on backend and sends rating/playlist commands.
* **Gate Acceptance:** While listening to YouTube Music, tapping the bubble shows Now-Playing controls; tapping Like marks the song as liked in YouTube Music.

---

### Phase 4: Quick Settings Tile & Complete Settings Screen
* **Goal:** Total control from the notification shade and a clean settings interface.
* **What is built:**
  - `BubbleTileService`: A Quick Settings tile in your Android status shade to turn the floating bubble on or off in a single swipe-and-tap.
  - Jetpack Compose UI in `MainActivity`:
    - Full permissions checklist (Overlay, Accessibility, Restricted Settings bypass, Unrestricted Battery).
    - Preferences: Adjust backend URL, token, bubble size, dock side, selection expiry time (default 60s), and custom playlist names.
  - `FullscreenWatcher`: Automatically hides the bubble whenever you are watching fullscreen videos (YouTube, Netflix) or playing games.
* **Gate Acceptance:** Toggle bubble on/off directly from Quick Settings tile without opening the app; bubble hides automatically during fullscreen playback.

---

### Phase 5: Artists, Discovery Playlist & System Hardening
* **Goal:** Explore artists and prevent the Inbox from becoming an unplayed graveyard.
* **What is built:**
  - Artist Discovery: Highlighting an artist and tapping Save Artist queries YouTube Music for that artist's **Top 3 tracks** and adds them to `Artists to Explore`.
  - Weekly Discovery Mix: Auto-aggregates saves from the past 7 days into a playlist with newest tracks first.
  - Played-track auto-pruning: Removes songs from the Inbox once you have finished listening to them.
  - Hardening: Memory leak verification, background battery optimizations, and network retry edge cases.
* **Gate Acceptance:** You use the app over two weeks in daily phone life, saving finds from Reddit effortlessly while the inbox actively refreshes and shrinks.

---

## 5. Daily Operation & How to Run

1. **Start the backend on your laptop:**
   ```powershell
   .\scripts\run_backend.ps1
   ```
2. **Launch the app on your phone:**
   Open the **Ginininini** app, tap **Start Bubble** (or toggle it on from your Quick Settings tile).
3. **Browse Reddit, Chrome, or Instagram:**
   Highlight any song or artist, tap the floating bubble, and tap **Save**.
4. **Enjoy your music:**
   Open YouTube Music anytime to hear your fresh finds!
