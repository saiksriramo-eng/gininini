# Ginininini Runbook: Setup, Operation & Troubleshooting

## 1. Prerequisites

### Laptop Environment
- **Operating System:** Windows 10/11 (PowerShell)
- **Python:** 3.10+ (Verified 3.11.9)
- **Git:** Installed
- **JDK:** JDK 17 or JDK 21 (required by Android Gradle Plugin)
- **Android SDK:**
  - Android SDK Platform 36 (Android 16)
  - Android SDK Build-Tools 36.0.0
  - Platform-Tools (`adb`) at `%LOCALAPPDATA%\Android\Sdk\platform-tools`
  - Add `platform-tools` to your system `PATH` if not already present.

### Phone Environment
- **Device:** moto g86 power 5G (Android 16, API 36)
- **Developer Options:** Enabled
- **USB Debugging:** Enabled, verified via `adb devices`
- **Wi-Fi:** Connected to the same local network as the laptop (ensure your router does not isolate AP clients).

---

## 2. Backend Setup & Authentication

### 2.1 Virtual Environment & Dependencies
In the `backend/` directory:
```powershell
python -m venv venv
.\venv\Scripts\Activate.ps1
python -m pip install -r requirements.txt
```

### 2.2 Generating YouTube Music Browser Credentials
`ytmusicapi` requires browser session request headers to access your YouTube Music account.

1. Open Firefox or Chrome on your laptop and navigate to [music.youtube.com](https://music.youtube.com). Ensure you are logged into your account.
2. Open Developer Tools (`F12`), switch to the **Network** tab, and filter by `browse` or `next`.
3. Click any song or playlist in YouTube Music to trigger network requests.
4. Right-click any `POST` request to `music.youtube.com` (e.g. `browse` or `next`) -> **Copy** -> **Copy Request Headers** (Firefox provides clean headers).
5. In your own terminal (never in chat!), run:
   ```powershell
   # Store auth file outside the repository
   $env:YTM_AUTH_FILE = "$HOME\.ginininini\browser.json"
   mkdir -p "$HOME\.ginininini"
   python -c "from ytmusicapi import YTMusic; YTMusic.setup(filepath=r'$HOME\.ginininini\browser.json')"
   ```
6. Follow the prompt to paste your copied headers and press Enter twice. This generates your `browser.json` securely.

### 2.3 Backend Configuration
Copy `.env.example` to `.env` in `backend/`:
```env
GININI_TOKEN="choose-a-secure-random-secret"
YTM_AUTH_FILE="C:\Users\Admin\.ginininini\browser.json"
INBOX_PLAYLIST="Discovery Inbox"
ARTISTS_PLAYLIST="Artists to Explore"
# ANTHROPIC_API_KEY="" # Optional, for messy text cleanup
```

### 2.4 Running the Backend
From the repo root:
```powershell
.\scripts\run_backend.ps1
```
Or directly:
```powershell
cd backend
.\venv\Scripts\uvicorn.exe app.main:app --host 0.0.0.0 --port 8000 --reload
```

### 2.5 Windows Firewall
Ensure inbound traffic on port 8000 is allowed:
```powershell
New-NetFirewallRule -DisplayName "Ginininini Backend" -Direction Inbound -LocalPort 8000 -Protocol TCP -Action Allow
```

---

## 3. Android App Build & Installation

### 3.1 Building the Debug APK
In the `android/` directory:
```powershell
.\gradlew.bat assembleDebug
```

### 3.2 Sideloading onto Device
Connect your phone via USB:
```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

---

## 4. Troubleshooting

| Issue | Cause | Fix |
| :--- | :--- | :--- |
| `adb: command not found` | `platform-tools` not in system PATH | Add `%LOCALAPPDATA%\Android\Sdk\platform-tools` to Windows environment PATH or invoke with full path. |
| `java.lang.UnsupportedClassVersionError` | Android Gradle Plugin requires JDK 17+ | Install OpenJDK 17 or 21, or point `JAVA_HOME` to Android Studio's bundled `jbr`. |
| Sideloaded permission greyed out | Android 13-16 Enhanced Confirmation Mode | Go to phone **Settings** -> **Apps** -> **Ginininini** -> 3 dots top right -> **Allow restricted settings**. |
| Backend unreachable from phone | Firewall or AP isolation | Verify phone and laptop IP in same subnet; check Windows firewall rule; test `http://<laptop-ip>:8000/health` in phone browser. |
| YouTube Music returns 401 / Auth failed | Session cookie expired | Re-export fresh headers via `ytmusicapi.setup(...)` into `$HOME\.ginininini\browser.json`. |
