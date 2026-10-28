# Ginininini Phase Log

This document tracks progress across the 6 implementation phases, including verified library versions, non-obvious API decisions, official documentation links, and verification gates.

---

## Verified Versions & Toolchain Matrix (October 2026)

| Component | Target / Stable Version | Documentation Reference |
| :--- | :--- | :--- |
| **Android compileSdk / targetSdk** | API 36 (Android 16) | [Android 16 Developer Docs](https://developer.android.com/about/versions/16) |
| **Android minSdk** | API 34 (Android 14) | Fixed decision |
| **Android Gradle Plugin (AGP)** | 8.8.2 / 8.9.x | [AGP Release Notes](https://developer.android.com/build/releases/gradle-plugin) |
| **Gradle** | 8.11.1+ | [Gradle Releases](https://gradle.org/releases/) |
| **Kotlin** | 2.1.20 | [Kotlin Releases](https://kotlinlang.org/docs/releases.html) |
| **KSP** | 2.1.20-1.0.30 | [KSP GitHub Releases](https://github.com/google/ksp/releases) |
| **Jetpack Compose BOM** | 2025.02.00 / 2026.01.00 | [Compose BOM Mapping](https://developer.android.com/develop/ui/compose/bom/bom-mapping) |
| **Room** | 2.6.1 (standard Android KSP) | [Room Release Notes](https://developer.android.com/jetpack/androidx/releases/room) |
| **WorkManager** | 2.10.0 | [WorkManager Release Notes](https://developer.android.com/jetpack/androidx/releases/work) |
| **OkHttp** | 4.12.0 / 5.0.0-alpha.14 | [Square OkHttp Releases](https://square.github.io/okhttp/changelogs/changelog/) |
| **ytmusicapi** | 1.12.3 | [ytmusicapi PyPI / Docs](https://ytmusicapi.readthedocs.io/) |
| **FastAPI** | 0.115+ / 0.141.x | [FastAPI Documentation](https://fastapi.tiangolo.com/) |
| **Pydantic** | 2.10+ / 2.13.x | [Pydantic Docs](https://docs.pydantic.dev/) |

---

## Phase 0: Setup & YouTube Music Ingestion

### Status: IN PROGRESS

### Key Decisions
1. **YouTube Music Authentication:** Using browser-header authentication (`browser.json`) via `ytmusicapi.setup(filepath=..., headers_raw=...)` rather than OAuth. Cookie-based sessions stay valid for ~2 years or until desktop logout. Auth files reside strictly outside the Git repository.
2. **Cleartext Traffic for Local Debugging:** Added `android:networkSecurityConfig` active only in debug builds to permit HTTP traffic to laptop private LAN IP (e.g. `http://192.168.x.x:8000`).
3. **Target Device Specifics:** moto g86 power 5G (Android 16, API 36, software channel retin).

### Phase 0 Test Gate
- [ ] Android project builds cleanly and installs on device via ADB.
- [ ] Backend runs on laptop with `browser.json` authenticated.
- [ ] Smoke test script executes `POST /save/song` or calls YTM API directly to add a test track into `Discovery Inbox`.
- [ ] **Gate Acceptance:** The track visibly shows in the YouTube Music Android app under `Discovery Inbox`.
