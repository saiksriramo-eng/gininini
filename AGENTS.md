# Ginininini Agent Rules

## Fixed Decisions
- Device: moto g86 power 5G, Android 16 (API 36), software channel retin.
- SDK: compileSdk 36, targetSdk 36, minSdk 34. Application ID: `app.ginininini`, App name: `Ginininini`.
- Capture: AccessibilityService reads highlighted text in v1. Transparent activity clipboard read is fallback. Share sheet target is secondary fallback.
- Artists: Top 3 songs saved to `Artists to Explore` playlist.
- Duplicates: Skip a song only if already in `Discovery Inbox`. Never check other playlists.
- Architecture: Android app floats draggable overlay bubble; Python FastAPI backend runs locally on laptop. Offline saves queue in Room and sync via WorkManager.
- YouTube Music Access: `ytmusicapi` using browser-header auth (`browser.json`). Never OAuth. Never YouTube Data API unless fallback is approved.
- Workspace: Single Gradle module `app`, Kotlin. Work inside repo.

## Hard Rules
1. Never guess APIs or versions: Look up stable versions before use. Record non-obvious API decisions in `docs/PHASE-LOG.md` with official documentation links.
2. Secrets & Auth: Never print, log, or commit auth files, tokens, or API keys. Auth file path comes from `YTM_AUTH_FILE` outside repo. Never ask the user to paste cookies or headers in chat; provide terminal commands.
3. Privacy: Accessibility service reads only selected text, ignores password fields, and never persists or leaks screen content. Text is transmitted to backend only when Save is tapped.
4. Safe Operations: Work inside the workspace. Ask before system-wide or destructive actions. ADB commands strictly limited to installing/uninstalling/running `app.ginininini`.
5. One Phase at a Time: Complete one phase, build, run tests, update `docs/PHASE-LOG.md`, provide a numbered on-phone test checklist, and STOP until the user replies `go`.
6. Code Style: Keep it lean and readable: single Gradle module `app`, Kotlin, no premature abstractions.
7. User Instructions: Format manual tasks (permission toggles, login commands) as clear numbered steps.
