# Ginininini

Ginininini is a personal Android utility app and local companion backend designed to turn music discovery into a one-gesture workflow.

Whenever you spot a song or artist in any Android app (Reddit, Chrome, Instagram, YouTube comments), tapping the floating overlay bubble instantly resolves and saves the track or artist to your YouTube Music library:
- **Individual songs** go to your `Discovery Inbox` playlist (skipping duplicates).
- **Artists** have their top 3 tracks saved to `Artists to Explore`.
- **Now-Playing mode** (when no text is selected) lets you play/pause, skip, like, or add the current track to your inbox.

## Architecture

- **`android/`**: Native Android app written in Kotlin targeting Android 16 (API 36) for Moto g86 power 5G.
  - Floating overlay bubble via `WindowManager` custom views.
  - `AccessibilityService` text selection capture (with transparent clipboard activity fallback).
  - `MediaSessionManager` / `NotificationListenerService` for now-playing integration.
  - Room database offline queue with `WorkManager` background sync.
  - Settings & Permissions screens built in Jetpack Compose.
- **`backend/`**: Lightweight Python FastAPI service running locally on your laptop.
  - Talks to YouTube Music via `ytmusicapi` using browser-header session auth (`browser.json`).
  - Text cleanup & fuzzy matching (with optional Anthropic Claude integration for noisy text).
  - SQLite log of historical saves.
- **`docs/`**: Architecture reports, phase logs, runbooks, and permission breakdown.
- **`scripts/`**: Convenience startup and smoke-testing scripts.

## Getting Started

See [`docs/RUNBOOK.md`](docs/RUNBOOK.md) for full setup instructions, prerequisites, and troubleshooting.
For permission requirements and security rationale, see [`docs/PERMISSIONS.md`](docs/PERMISSIONS.md).
