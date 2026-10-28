# Ginininini: Master Prompt for Antigravity IDE

Oct 27, 2026 · @SAI

Paste the prompt below into the Antigravity IDE agent panel to start Phase 0. It assumes the report in the first tab is saved in your repo as `docs/Ginininini-report.md`.

## Before you paste

- [ ] Install Android Studio (stable). In its SDK Manager, add the Android 16 (API 36) platform, Build-Tools and Platform-Tools (adb).
- [ ] Install Python 3.10 or newer, and Git.
- [ ] On the phone, turn on Developer options and USB debugging, connect it by USB, accept the prompt, and check that `adb devices` lists it.
- [ ] Be signed in to YouTube Music on the phone and in a desktop browser. The ytmusicapi docs recommend Firefox for copying the login headers.
- [ ] Make an empty folder named `ginininini`, export the first tab of this doc as Markdown, and save it inside as `docs/Ginininini-report.md`.
- [ ] Open that folder in Antigravity IDE and sign in.

## How to run it

1. Start a new conversation in the agent panel and paste the prompt below. It asks the agent for an implementation plan before any code.
2. Read the plan. Reply `go` to start Phase 0, or say what to change.
3. After each phase the agent stops and lists tests to run on your phone. Run them, then reply `go` or describe what failed.
4. When it asks you to run the YouTube Music login command, run it yourself in your terminal. Never paste cookies or headers into the chat.

## The master prompt

Copy everything inside the block below.

```markdown
# Ginininini: master prompt

You are the lead engineer on **Ginininini**, a personal Android app plus a small Python backend. Read `docs/Ginininini-report.md` first. It is the source of truth for context, architecture, phases and risks. This prompt adds the decisions made on 27 October 2026. Where the two differ, this prompt wins, and you tell me about the conflict.

## 1. What we are building

Ginininini floats a draggable bubble over every app on my phone. When I highlight a song or artist name anywhere (Reddit, Chrome, Instagram), I tap the bubble and the match is saved to my YouTube Music. Songs go to a playlist called `Discovery Inbox`. For an artist, their top 3 songs go to a playlist called `Artists to Explore`. With nothing highlighted, the same semi-circle menu controls whatever is playing in YouTube Music: play or pause, skip, like, add the current song to the Inbox. It is for one user (me), installed by sideloading, never published.

## 2. Fixed decisions (do not re-ask)

- Phone: moto g86 power 5G, Android 16 (API 36), software channel retin. Use compileSdk 36, targetSdk 36, minSdk 34.
- Application id `app.ginininini`. App name `Ginininini`.
- Capture without copying is in version 1. An AccessibilityService reads the highlighted text. A clipboard read through a transparent activity is the fallback. A share-sheet target is a cheap extra.
- Artists: save the artist's top 3 songs to `Artists to Explore` (fewer if the artist has fewer).
- Duplicates: skip a song only if it is already in `Discovery Inbox`. Never check my other playlists.
- The backend runs on my laptop for now, on the same Wi-Fi as the phone. Saves made while the laptop is unreachable wait in a Room queue on the phone and retry with WorkManager.
- YouTube Music access: ytmusicapi with browser-header authentication (a `browser.json` file). Do not use OAuth. The official YouTube Data API is a documented fallback only; do not build it now.
- I work in Antigravity IDE. Android Studio is installed for the SDK, Gradle sync and Logcat.

## 3. Hard rules

1. Never guess APIs or versions. Look up the current stable Android Gradle Plugin, Kotlin, Compose BOM, Room, WorkManager and OkHttp, and the installed ytmusicapi version, and read their official docs before using them. Record each non-obvious API decision in `docs/PHASE-LOG.md` with the doc link.
2. Secrets: never print, log or commit the YouTube Music auth file, the backend token or any API key. The auth file lives outside the repo; its path comes from the env var `YTM_AUTH_FILE`. Never ask me to paste cookies or headers into chat. Give me a command to run in my own terminal.
3. Privacy: the accessibility service reads only selected text, skips password fields and never stores screen content. Text is sent to the backend only when I tap Save.
4. Work inside the repo. Ask before installing anything system-wide or running anything destructive. With adb, only install or uninstall `app.ginininini` on my phone.
5. One phase at a time (section 8). When a phase is done: build, run the tests, update `docs/PHASE-LOG.md`, give me a numbered on-phone test checklist, then STOP and wait for me to say go.
6. Keep it small and readable: one Gradle module `app`, Kotlin, no abstractions the phase does not need.
7. Whenever I must do something by hand (enable a permission, run a login command), list it as numbered steps.

## 4. Tech stack (verify current stable versions first)

Android:
- Kotlin, Gradle Kotlin DSL, version catalog `gradle/libs.versions.toml`.
- AndroidX Core KTX, Lifecycle (LifecycleService and runtime), DataStore Preferences, Room (with KSP), WorkManager, DynamicAnimation (spring motion for the arc menu).
- Activity Compose, Compose BOM, Material 3, for the settings and permissions screens only.
- OkHttp and kotlinx.serialization for JSON, kotlinx-coroutines.
- Tests: JUnit, kotlinx-coroutines-test, OkHttp MockWebServer.
- The bubble and arc menu are plain Views in a WindowManager overlay, not Compose, because Compose in an overlay needs a hand-built lifecycle owner.

Backend:
- Python 3.10 or newer, FastAPI, uvicorn[standard], ytmusicapi, pydantic v2 with pydantic-settings, httpx and pytest.
- Optional `anthropic` package for text cleanup, active only when `ANTHROPIC_API_KEY` is set.

Tools on my laptop (check, and tell me what is missing): Android Studio stable, Android SDK Platform 36, Build-Tools, Platform-Tools (adb), a JDK (the one bundled with Studio is fine), Git, Python 3.10+. In Antigravity IDE, use the Kotlin and Python language extensions if they are available. Android Studio stays the tool for Gradle sync and Logcat.

## 5. Files to create

Repo root `ginininini/`:
- `AGENTS.md`: standing rules for you, distilled from sections 2 and 3, under 100 lines.
- `README.md`, and `.gitignore` (Android, Python, `.env`, auth JSON files, `local.properties`).
- `docs/Ginininini-report.md` (I add it), `docs/PHASE-LOG.md`, `docs/RUNBOOK.md` (install, run, troubleshoot), `docs/PERMISSIONS.md` (every permission and why).
- `scripts/`: backend launch script for my laptop's OS. Detect the OS, or ask me once.

`android/` (Gradle project):
- `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, `gradle/libs.versions.toml`, Gradle wrapper, `app/build.gradle.kts`, `app/proguard-rules.pro`.
- `app/src/main/AndroidManifest.xml`, and under `res/`: `xml/accessibility_service_config.xml`, `values/strings.xml`, `values/themes.xml`, vector icons for the bubble, the menu buttons and the tile, a launcher icon.
- `app/src/debug/res/xml/network_security_config.xml`: cleartext HTTP to the laptop, debug builds only.
- Kotlin under `app/src/main/java/app/ginininini/`:
  - `overlay/`: BubbleService, BubbleView, ArcMenuView, FullscreenWatcher
  - `capture/`: SelectionAccessibilityService, SelectionCache, ClipboardCaptureActivity, ShareTargetActivity
  - `nowplaying/`: YtMusicListenerService (notification listener), MediaSessionMonitor
  - `tile/`: BubbleTileService
  - `data/`: AppDatabase, SaveEntity, SaveDao, SettingsStore
  - `net/`: BackendApi, Models
  - `work/`: SaveWorker
  - `ui/`: MainActivity, SettingsScreen, PermissionsScreen
- Tests in `app/src/test` and `app/src/androidTest`.

`backend/`:
- `pyproject.toml` (or `requirements.txt` plus `requirements-dev.txt`), `.env.example`, `README.md`.
- `app/main.py`, `app/config.py`, `app/auth.py`, `app/ytm.py`, `app/resolve.py`, `app/schemas.py`, `app/store.py` (small SQLite log of saves).
- `scripts/smoke_test.py`, `tests/`.

## 6. Behaviour spec (the parts that are easy to get wrong)

Bubble and menu:
- Foreground service with the correct service type and matching permission declared. Check developer.android.com for the right type for an overlay bubble (likely `specialUse` with its subtype property).
- Overlay window type TYPE_APPLICATION_OVERLAY with FLAG_NOT_FOCUSABLE, so the text selection in the app underneath survives my tap.
- Draggable, snaps to the nearest screen edge, fades when idle, hides in fullscreen apps (a setting). Stop action in the notification. A Quick Settings tile toggles it.
- Tap opens 4 or 5 round buttons on a semi-circle facing away from the docked edge, with spring motion. Tap outside closes it.
- Two modes, picked automatically. Capture mode (a selection newer than the expiry setting exists): Save song, Save artist, Play now. Now-playing mode: play or pause, skip, like, add current song to Inbox. Play now opens the song in YouTube Music through its web link. Add Play next only if there is a reliable way; otherwise leave it out and tell me.

Capture:
- Accessibility service config: only the event types it needs (text-selection changes, plus window changes to know the foreground app). Cache the latest selected text, its source package and a timestamp in SelectionCache. Skip password fields.
- If an app does not expose its selected text, perform the copy action on the node that holds the selection, then read the clipboard through the transparent ClipboardCaptureActivity. Android only lets a focused activity read the clipboard.
- Selection expiry is a setting (default about 60 seconds).

Saving:
- Resolve: strip quotes, links, emoji, `u/` names, `feat.`, year tags and filler words like by. Search with the songs filter first, and the artists filter for artist saves. Return the best match plus up to 3 candidates. The app shows the best match with Undo and a way to pick another candidate. Optional Claude cleanup for very messy text, only when the env var is set.
- Save song: add to `Discovery Inbox` unless already there. Return the playlist id and set id so Undo can remove it. Log source package and time.
- Save artist: resolve the artist, take the top 3 songs, add them to `Artists to Explore`.
- Offline: Room queue with WorkManager and exponential backoff. Show a queued state. Never queue the same item twice.
- Like and add-current: match the playing title and artist through search on the backend, then rate or add the match.

Screens:
- Settings: backend URL, token, bubble size and side, which buttons show, selection expiry, playlist names.
- Permissions checklist: Display over other apps, Notification access (now playing), Accessibility, the Allow restricted settings step for sideloaded apps, unrestricted battery use for the app, notifications. Each row opens the right system screen. Say that menu paths can differ on my phone.

## 7. Backend API

Every endpoint needs the header `Authorization: Bearer <GININI_TOKEN>`.
- `GET /health`: `{ok, ytm_auth}`.
- `POST /resolve`: `{text, kind: auto|song|artist}` returns the best match and up to 3 candidates.
- `POST /save/song`: `{videoId, source_pkg}` returns `{playlistId, setVideoId, already_saved}`.
- `POST /save/artist`: `{browseId, source_pkg}` returns the songs added.
- `POST /undo`: `{playlistId, items}`.
- `POST /like`: `{title, artist}`. `POST /add-current`: `{title, artist}`.
- Config from env or `.env`: `GININI_TOKEN`, `YTM_AUTH_FILE`, `INBOX_PLAYLIST`, `ARTISTS_PLAYLIST`, optional `ANTHROPIC_API_KEY`. Create the two playlists by name if they do not exist.
- Start with `uvicorn app.main:app --host 0.0.0.0 --port 8000`. Write pytest tests with a fake YouTube Music client.

## 8. Phases and gates

0. Setup: the Android project builds and runs on my phone; backend login works; a script adds one track to `Discovery Inbox`. Gate: the track shows in the YouTube Music app.
1. Bubble and arc menu with fake buttons. Gate: survives app switching, drags smoothly, stops from the notification.
2. Capture and save (accessibility first, clipboard fallback), plus a basic permissions checklist screen for Display over other apps, Accessibility and restricted settings. Gate: highlight, tap the bubble, Save puts the right track in the Inbox within seconds in Chrome and in the Reddit app; Undo removes it.
3. Now-playing controls. Gate: with nothing highlighted, the menu shows the current song and Like appears in YouTube Music.
4. Quick Settings tile, full settings screen, notification-access row in the permissions checklist, genre tag per save. Gate: on and off from the notification shade.
5. Artists (top 3), weekly Discovery Friday playlist from the freshest saves, removal after play, hardening. Gate: the Inbox shrinks over two weeks of real use.

## 9. Open design question for your plan

Because the accessibility service is now required, it could also draw the bubble itself (an accessibility overlay window), which avoids the foreground-service and overlay-permission rules. Compare both in your plan in 5 lines or fewer. Keep the service-based design unless I approve the switch.

## 10. Your first reply

1. Read `docs/Ginininini-report.md` and this prompt.
2. Produce an implementation plan: versions found, file tree, the manifest permission list, risks, and at most 5 open questions.
3. Create `AGENTS.md`, `README.md`, `.gitignore` and the `docs/` files.
4. Do Phase 0 only. Then stop and show me the test steps.
```

## Review: what I changed and what to watch

The report holds up. These are the points I would want to know before the agent starts.

- **Login by browser headers, not OAuth.** The ytmusicapi docs say browser credentials stay valid as long as your YouTube Music browser session does, about 2 years unless you log out ([browser authentication](https://ytmusicapi.readthedocs.io/en/stable/setup/browser.html)). Its OAuth route is a separate setup that needs your own Google client ID and secret. The repo's README example still shows an `oauth.json` file, so the prompt tells the agent to ignore it.
- **Secrets stay outside the repo.** The agent can read anything in the workspace. The login file lives in a folder outside it, and the prompt forbids pasting cookies into chat.
- **Accessibility capture is the riskiest bet.** Some apps, possibly the Reddit app, may not expose selected text. The Phase 2 gate tests Chrome and Reddit, and the clipboard fallback keeps the app useful if it fails.
- **Sideloaded apps hit extra permission prompts.** Since Android 13, accessibility and notification-listener access stay blocked for sideloaded apps until you allow restricted settings in App info ([Android Authority](https://androidauthority.com/android-15-enhanced-confirmation-mode-3436697)). I have seen reports that newer versions also restrict display over other apps, but I did not verify that on Android 16. Test the permissions early, and note that installing over adb may skip part of the prompt.
- **Laptop hosting needs three things.** Allow the backend port through the laptop's firewall, keep the laptop on the same local address (reserve one on your router), and stay on the same Wi-Fi. Saves made elsewhere wait in the phone's queue. Plain HTTP is allowed in debug builds only.
- **Bubble design.** The prompt keeps the service-based bubble from the report, and asks the agent to compare drawing it from the accessibility service instead.
- **Play next is not guaranteed.** The report listed it, but the prompt builds it only if there is a reliable way.
- **Rules file size.** Antigravity reads `AGENTS.md` as always-on rules and truncates a rule file larger than 24,000 bytes ([Antigravity docs](https://www.antigravity.google/docs/rules)). The prompt asks for one under 100 lines.
- **Antigravity IDE is the older product.** Google calls Antigravity 2.0 the future ([The Pragmatic Engineer](https://blog.pragmaticengineer.com/the-pulse-antigravity-2-0-takes-ide-out-of-its-new-ide/)). The repo and prompt work the same in 2.0, Android Studio's assistant or another agent if the IDE agent misbehaves.
