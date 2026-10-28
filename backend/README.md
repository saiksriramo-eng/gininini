# Ginininini Local Backend

A lightweight FastAPI service acting as the secure gateway between the Ginininini Android app and YouTube Music.

## Endpoints

All endpoints require the header `Authorization: Bearer <GININI_TOKEN>`.

- `GET /health` -> `{ ok: bool, ytm_auth: bool }`
- `POST /resolve` -> `{ text: str, kind: "auto" | "song" | "artist" }` -> Returns best match and up to 3 candidates
- `POST /save/song` -> `{ videoId: str, title: str, artist: str, source_pkg: str }` -> Adds track to `Discovery Inbox`
- `POST /save/artist` -> `{ browseId: str, artist: str, source_pkg: str }` -> Adds top 3 songs to `Artists to Explore`
- `POST /undo` -> `{ playlistId: str, items: list }` -> Removes items previously added
- `POST /like` -> `{ title: str, artist: str }` -> Rates track with LIKE on YouTube Music
- `POST /add-current` -> `{ title: str, artist: str }` -> Finds current track and saves to `Discovery Inbox`

## Local Development
```powershell
python -m venv venv
.\venv\Scripts\activate.ps1
pip install -r requirements.txt -r requirements-dev.txt
pytest
```
