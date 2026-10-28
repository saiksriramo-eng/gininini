# Ginininini: Project Report and Build Guide

Oct 27, 2026 · @SAI

## 1. Context and problem

You are exploring more music, especially DHH and international artists, but you keep looping your old playlists. Reddit is where you find new songs and artists, yet you end up revisiting the same threads because nothing carries a find into your listening queue.

The missing piece is a capture step. Today the sequence is: spot a name in a thread, search it by hand, play it once, forget it, and go back to Reddit to find it again. The tool in this report turns "I saw this name" into "it is waiting in my YouTube Music" with one gesture.

What the tool must respect:

- It lives on your phone and works over any app: Reddit, Chrome, Instagram, YouTube comments.
- You can switch it on and off at any time, and it stays out of the way during normal phone use.
- YouTube Music stays the place where you listen.

**Confirmed:** your phone runs Android 16 on a moto g86 power 5G (software channel retin). The screenshot you shared shows iPhone AssistiveTouch, used here only as the visual reference for the bubble. iOS does not let third-party apps draw a floating button over other apps, so an iPhone would need a different plan.

## 2. Goals, non-goals and success criteria

The project succeeds if saving a find takes one gesture and you actually listen to what you saved. Version 1 stays small on purpose.

**Goals**

- Save a highlighted or copied song to a "Discovery Inbox" playlist in YouTube Music.
- Save an artist by adding their top three songs to an "Artists to Explore" playlist.
- Show a floating bubble that opens a semi-circle menu with play, save, like and a few more actions.
- Switch the bubble on and off from the notification shade, so it never blocks daily phone use.
- Break the Reddit loop: each saved item remembers where you found it, so you never need to reopen the thread.

**Non-goals for version 1**

- Publishing on Google Play or supporting other people's accounts.
- A recommendation engine, or a replacement for the YouTube Music app.
- iOS support.
- Downloading or ripping audio.

**Success criteria (proposed targets)**

| Check | Target |
| --- | --- |
| Gestures from highlight to saved | 2 taps or fewer |
| Time to confirm a save | A few seconds on a normal connection |
| Wrong match | Matched track shown with Undo |
| Interference with daily use | Hidden in fullscreen apps, off in one tap |
| Behaviour change | Inbox gets played and emptied, not only filled |

## 3. Approach and options considered

A native Android app is the only route that can draw the bubble, fan out the arc menu and read what you highlighted. The table compares the four realistic options.

| Option | Bubble and arc menu | Reads your highlight | Effort | Verdict |
| --- | --- | --- | --- | --- |
| MacroDroid or Tasker | Basic overlays only; a custom arc menu is a stretch | Clipboard or share sheet | Low, about an evening | Good throwaway prototype |
| Telegram bot or share sheet only | No bubble | Share sheet | Low | Works, but nothing stays on screen |
| Flutter or React Native app | Needs native Kotlin code for the overlay anyway | Same native code | Medium to high | Adds a layer for no gain |
| Native Kotlin app plus FastAPI backend | Full control | Accessibility service, clipboard, selection menu | Medium to high | Chosen |

The riskiest part is not Android but YouTube Music. There is no official public API for it. The unofficial library [ytmusicapi](https://github.com/sigma67/ytmusicapi) emulates the YouTube Music web client using your cookie data, and it can search, create playlists, add tracks, rate songs and subscribe to artists.

The official YouTube Data API is the fallback. It works with videos, so its matches can differ from YouTube Music's audio tracks. The design therefore hides YouTube Music behind one small backend module, so either library can be swapped in without touching the app.

## 4. Solution and architecture

Ginininini has two parts: an Android app that floats a bubble over every app, and a small backend that does all the YouTube Music work. The bubble opens a semi-circle menu whose buttons depend on what you just did.

&#91;embedded content: architecture · phone app, backend, YouTube Music\]

Dashed boxes are existing apps you do not build. The phone sends text to the backend, and only the backend touches YouTube Music.

**Two modes, picked automatically**

- Capture mode, when you have just copied or highlighted text: Save song, Save artist, Play now, Play next.
- Now-playing mode, when nothing is captured: Play or pause, Like, Add current song to Inbox, Skip.

**How one save travels**

1. You highlight a song or artist name in any app. No copying is needed.
2. You tap the bubble, then Save. The accessibility service reads the highlighted text. If an app hides its text, a transparent activity reads the clipboard instead.
3. The app sends the text to the backend, which cleans it, searches YouTube Music and returns the best match.
4. The app shows the match with Undo. If you do nothing, the backend adds the song to "Discovery Inbox" (or adds the artist's top three songs to "Artists to Explore") and records the source app, plus the link when known.
5. If you are offline, the save waits in a local queue and retries later.

**Three ways to capture text**

1. Accessibility service (primary): reads the highlighted text with no copying, so one highlight and one tap saves it. It needs a heavy permission and can fail in apps that hide their text.
2. Clipboard (fallback): highlight, copy, tap the bubble. Reliable, so it is built first as the baseline.
3. Share sheet and text-selection menu entry (fallbacks): small to build, but not every app offers them.

## 5. Tech stack

Everything below is free or has a free tier for a personal project, except the optional Claude API call and any cloud hosting. The only unofficial piece is ytmusicapi.

| Layer | Choice | Why or note |
| --- | --- | --- |
| Language and build | Kotlin, Gradle (Kotlin DSL), Android Studio | Standard Android toolchain |
| Floating bubble | Foreground service plus a WindowManager overlay | Only way to draw over other apps; needs the "Display over other apps" permission |
| Bubble and arc UI | Custom Views with spring animation; Jetpack Compose for the settings screen | Compose inside an overlay window needs extra lifecycle setup (section 10) |
| Capture | Accessibility service (primary); transparent activity that reads the clipboard; share-sheet target; text-selection menu entry | Accessibility first; the others are fallbacks |
| Now-playing controls | MediaSessionManager with notification-listener access | Reads and controls the active YouTube Music session |
| On and off | Quick Settings tile plus a notification action | Switch it off without opening the app |
| Local data | Room database | Save log, duplicate check, offline queue |
| Networking | OkHttp or Retrofit, WorkManager for retries | Saves still land after a dropped connection; plain HTTP to the laptop needs a debug-only network security config |
| Backend | Python 3.10 or newer, FastAPI, ytmusicapi | A stack you already use; the library needs Python 3.10+ |
| Text cleanup | Simple rules first; optional Claude API call to pull song and artist from messy text | Reddit comments are noisy |
| Secrets | YouTube Music cookies and API keys stay on the backend only | An APK can be unpacked, so keys must not live in it |
| Hosting | Your laptop for now | Decided. Saves queue on the phone while the laptop is off |
| Version control | Git, one repo with /android, /backend and /docs | Keeps both halves together |
| AI coding help | Antigravity IDE | See section 6 and the Master prompt tab |

## 6. Where to build

Yes, you can use Antigravity, but pair it with Android Studio. Antigravity is an AI coding-agent environment, while Android Studio still supplies the SDK, device tools, Gradle build and signing that an Android app needs.

The naming is confusing. As of October 2026, Antigravity covers several products: the original VS Code-based Antigravity IDE (first released November 2025), the redesigned Antigravity 2.0 desktop app (preview release 19 May 2026), a command-line tool and an SDK ([Wikipedia](https://en.wikipedia.org/wiki/Google_Antigravity)). Google's launch post, as quoted by [The Pragmatic Engineer](https://blog.pragmaticengineer.com/the-pulse-antigravity-2-0-takes-ide-out-of-its-new-ide/), calls 2.0 the future and suggests using it alongside whichever IDE you prefer. That review also reports bugs and fast model-quota use at launch, so expect rough edges.

**Recommended setup**

1. Install Android Studio and the Android SDK, and create the Kotlin project there so Gradle, signing and device tools work.
2. Open the same project folder in Antigravity (or another coding agent) to write code, and build and run from Android Studio.
3. Keep the backend as a second folder in the same repo. Edit it in Antigravity or VS Code and run it with Python 3.10 or newer.
4. Test on your real phone over USB or wireless debugging. Overlays, clipboard rules and the real YouTube Music app's media session are hard to reproduce in an emulator.

**Optional shortcut: Google AI Studio.** Its Build mode can generate a native Kotlin and Jetpack Compose project, preview it in a browser emulator, install it over USB, and export a ZIP for Android Studio ([AI Studio docs](https://ai.google.dev/gemini-api/docs/aistudio-android)). The generator supports only single-activity, single-module, Compose-only projects, so write the overlay service, tile and capture code in your own project instead.

Any agent works here, because the project is a plain Gradle repo: Android Studio's built-in assistant, Antigravity or Claude Code. Pick whichever you find least frustrating.

## 7. Build phases and milestones

There are six phases, each ending in a gate you can test on your phone before moving on. Phase 2 is the milestone that breaks the Reddit loop, so protect time for it, especially now that capture without copying is in version 1. Durations are left out because they depend on how much of the code your agent writes and how new Android is to you.

&#91;embedded content: roadmap · 6 phases, 6 gates\]

Phase 2 is the first point where a highlight becomes a saved song. Phases 3 to 5 make it comfortable and keep the Inbox from becoming a second graveyard.

## 8. Step-by-step build guide

Build one phase at a time and hand your coding agent only that phase, with its "done when" line as the acceptance test. Each phase ends with something you can run on your phone.

### Phase 0: Setup

1. Install Android Studio and create an Empty Activity project in Kotlin. Set the compile and target SDK to Android 16 (API 36), the version on your phone.
2. On the phone, turn on Developer options and USB debugging, connect it, and run the empty app.
3. Create the Git repo with /android, /backend and /docs.
4. In /backend, make a Python 3.10+ virtual environment, install FastAPI and ytmusicapi, and follow the ytmusicapi setup guide to authenticate with your account.
5. Write a test script that searches one song, creates the "Discovery Inbox" playlist and adds the track.

**Done when:** the track appears in that playlist inside the YouTube Music app.

### Phase 1: Bubble and arc menu (no YouTube Music yet)

1. Declare the overlay permission and a foreground service with its service type and matching permission.
2. Draw a draggable bubble that snaps to the screen edge and fades when idle.
3. On tap, animate four or five buttons into a semi-circle facing away from the docked edge. Tap outside to close.
4. Add a notification with a Stop button, and hide the bubble in fullscreen apps.

**Done when:** the bubble survives switching apps, drags smoothly and stops from the notification.

### Phase 2: Capture and save

1. Baseline first: add a transparent activity that reads the clipboard when you tap Save. Android restricts background clipboard reads, so reading from a visible activity avoids the block.
2. Add the accessibility service with a narrow configuration (selection events only, no storing of screen content). It reads the highlighted text from the focused text node. If an app hides its text, it asks the system to copy the selection and falls back to the clipboard activity.
3. Build an in-app permissions checklist that walks you through "Display over other apps", accessibility, and the "Allow restricted settings" step for sideloaded apps.
4. Backend: a resolve endpoint that turns text into a candidate track (search, plus an optional Claude call to clean messy text) and a save endpoint that adds it to the inbox and stores the source app.
5. App: show "Added: title, artist" with Undo. Queue saves in Room and retry with WorkManager when offline.
6. Skip duplicates inside the Inbox playlist only. There are no checks against your other playlists.
7. Add the share-sheet target as a cheap extra fallback.

**Done when:** highlight, tap the bubble, Save puts the right track in the inbox within seconds in Chrome and the Reddit app, the clipboard fallback covers any app that hides its text, and Undo removes the track.

### Phase 3: Now-playing controls

1. Request notification-listener access (a restricted setting for sideloaded apps, see section 10).
2. Read the active YouTube Music session with MediaSessionManager and show its title and artist in the menu.
3. Wire play, pause and skip to that session.
4. Like and "add current song to Inbox": search by title and artist on the backend, then rate or add the match.

**Done when:** with nothing captured, the menu switches to now-playing mode and Like shows up in YouTube Music.

### Phase 4: Control and polish

1. Add the Quick Settings tile that toggles the bubble.
2. Build the settings screen in Compose: bubble size, docked side, visible buttons, playlist names and backend address.
3. Store a genre tag (DHH, international, other) with each save.

**Done when:** you can turn the bubble on and off from the notification shade and change its behaviour without touching code.

### Phase 5: Artists, Discovery Friday and hardening

1. Artists: add the artist's top three songs to "Artists to Explore" (fewer if the artist has fewer).
2. Build a weekly "Discovery Friday" playlist from your freshest saves, and remove items once played.
3. Harden the accessibility service: narrow it to the events it needs and test it across the apps you use.
4. Register with Android developer verification if you plan to keep the app past 2026 (section 11).
5. Keep the backend on your laptop, and add a private tunnel only if you later want saves to work away from home Wi-Fi.

**Done when:** you have used the Inbox for two weeks and it is shrinking, not only growing.

## 9. How to run and use it

The backend runs on your laptop and the app on your phone, both on the same Wi-Fi. Saves made while the laptop is off or on another network wait in the phone's queue and sync later.

**Run the backend**

1. Activate the virtual environment and start the FastAPI app with uvicorn.
2. Enter the backend address and a shared secret token in the app's settings screen.
3. Keep cookie files and tokens out of Git with a .gitignore entry.

**Install the app**

1. Build a debug APK in Android Studio and run it on the connected phone, or use wireless debugging.
2. When prompted, allow "Display over other apps" for the app.
3. The accessibility service and notification-listener access are both needed, and Android blocks both for sideloaded apps at first. Open the app's App info page, open the three-dot menu and choose Allow restricted settings. The exact path varies by phone brand.
4. If the bubble disappears after a while, set the app's battery usage to unrestricted. Some phone makers stop background services aggressively.

**Daily use**

1. Switch the bubble on from the Quick Settings tile.
2. Highlight a song or artist in Reddit or any app, copy it, tap the bubble and tap Save. Check the confirmation and tap Undo if the match is wrong.
3. With nothing captured, the same menu controls whatever is playing: play, pause, skip, like.
4. In free time, open the "Discovery Inbox" playlist in YouTube Music. The weekly Friday mix puts the newest saves first.
5. Switch the bubble off from the tile or the notification's Stop button whenever you want a clean screen.

## 10. Bottlenecks

The hard parts are Android's permission rules and matching messy text to the right track, not the bubble itself.

| Bottleneck | Why it is hard | Plan |
| --- | --- | --- |
| Reading highlighted text from other apps | An overlay cannot see other apps' text, and Android restricts background clipboard reads | Accessibility service first, with the clipboard activity and share sheet as fallbacks |
| Restricted settings on sideloaded apps | Android 13 and later block accessibility and notification-listener access for sideloaded apps until you allow it in App info. Android 15 was reported to add a stricter Enhanced Confirmation Mode ([Android Authority](https://androidauthority.com/android-15-enhanced-confirmation-mode-3436697)), and I could not confirm how it treats a personal app today | Add an in-app permissions checklist and test the accessibility permission first thing in Phase 2 |
| Foreground service rules | Android 14 and later need declared service types. For apps targeting API 35 or higher, an app with the overlay permission can start a foreground service from the background only while an overlay window is visible ([Android docs](https://developer.android.com/develop/background-work/services/fgs/changes)) | Start the service while the app is visible and keep the overlay alive |
| Compose inside an overlay | An overlay window has no lifecycle owner for Compose to attach to | Use plain Views for the bubble and arc, Compose for the settings screen |
| Matching the right track | Reddit text is messy: transliterated titles, covers, remixes, similar artist names | Prefer the songs filter in search, show the match with Undo, optional Claude cleanup |
| Like on the current song | The media session may not expose a YouTube Music track ID | Match by title and artist on the backend, then rate the result |
| Unofficial YouTube Music access | Cookies expire and the library can break when YouTube changes | Keep it behind one backend module, add a health check, keep the official API as fallback |
| Phone makers killing background services | Some brands stop services to save battery | Set the app to unrestricted battery use |

## 11. Drawbacks and risks

The two risks most likely to bite are the unofficial YouTube Music access and Android's sideloading rules, which are changing in 2026 and 2027.

| Risk | What could happen | Mitigation |
| --- | --- | --- |
| Unofficial YouTube Music access | The library breaks when YouTube changes, and automated use of the web client may conflict with YouTube's terms (read them before relying on it) | Use your own account only, keep request volume low, keep the official API as a fallback |
| Android developer verification | Google says protections began on 30 September 2026 for installs from participating stores in Brazil, Indonesia, Singapore and Thailand, with global expansion in 2027 ([Android Developers](https://developer.android.com/developer-verification)). India is not in the first list, and what applies to an app you build and install yourself is not settled in what I read | Plan to register a free limited distribution account (up to 20 devices, no government ID or fee) and recheck in early 2027 |
| Accessibility service is powerful | It can read screen content, so Android treats it as high-risk ([Android Authority](https://androidauthority.com/android-15-enhanced-confirmation-mode-3436697)) | It is required in version 1, so limit it to selection events, never store or send screen content, and use it only on your own phone |
| Clipboard privacy | The clipboard can hold passwords or one-time codes | Read it only when you tap Save, never log it, send only the text you saved |
| Cookie leakage | Anyone with your cookie file can act as you in YouTube Music | Keep it on the backend, use HTTPS and a token if hosted, never commit it |
| The Inbox becomes a second graveyard | You save a lot and listen to little | Friday mix, auto-remove after play, a weekly review |
| Antigravity rough edges | Reported bugs and fast quota use around the 2.0 launch | Keep Android Studio as the main tool, and swap coding agents freely |
| Android updates | A new version can change overlay or clipboard behaviour | Retest after every major update |

## 12. Updates and maintenance

Expect small, regular upkeep, mostly around the YouTube Music login and Android updates. The facts in this report were checked on 27 October 2026; Antigravity, Android's verification rules and ytmusicapi all change quickly, so recheck them before you start.

| What | When | Action |
| --- | --- | --- |
| ytmusicapi | Monthly, and whenever searches start failing | Pin the version, upgrade in a test branch, rerun the Phase 0 script |
| YouTube Music login (cookies) | When the backend reports auth errors | Export fresh cookies and replace them on the backend |
| Android updates | After each major update | Retest the bubble, clipboard capture, tile and notification |
| Developer verification | Early 2027 | Check the global rollout and register the app if needed |
| Android Studio and libraries | Every few weeks | Update through the IDE, rebuild and rerun the phase checks |
| Antigravity | When it prompts | Names and versions are shifting, so keep the project plain Gradle and tool changes will not matter |
| This report | After each phase | Add a changelog line and update the open questions |

## 13. Open questions, future ideas and sources

**Decisions made on 27 October 2026**

- Phone: Android 16 on a moto g86 power 5G (software channel retin).
- Artists: add their top three songs to "Artists to Explore".
- Backend hosting: your laptop for now.
- Capture without copying: in version 1, through the accessibility service.
- Duplicate checks: only inside the Inbox, not against your other playlists.
- Coding agent: Antigravity IDE.

**Still open**

- [ ] Whether the accessibility service can read selected text in the Reddit app. This is tested in Phase 2.

**Future ideas**

- Automatic genre tags (DHH, international) from track metadata.
- A home-screen widget showing how many songs wait in the Inbox.
- Weekly stats of saved versus played, to check whether the Reddit loop really broke.
- Share a saved list with friends.

**Sources** (opened 27 October 2026)

- [Google Antigravity, Wikipedia](https://en.wikipedia.org/wiki/Google_Antigravity)
- [The Pulse: Antigravity 2.0 takes IDE out of its new IDE, The Pragmatic Engineer](https://blog.pragmaticengineer.com/the-pulse-antigravity-2-0-takes-ide-out-of-its-new-ide/)
- [Build Android apps in Google AI Studio](https://ai.google.dev/gemini-api/docs/aistudio-android)
- [Changes to foreground services, Android Developers](https://developer.android.com/develop/background-work/services/fgs/changes)
- [Android developer verification, Android Developers](https://developer.android.com/developer-verification)
- [ytmusicapi, GitHub](https://github.com/sigma67/ytmusicapi)
- [Android 15 may make it even harder for sideloaded apps to get sensitive permissions, Android Authority (April 2024)](https://androidauthority.com/android-15-enhanced-confirmation-mode-3436697)
