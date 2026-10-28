import pytest
from unittest.mock import MagicMock
from fastapi.testclient import TestClient
from app.config import settings
from app.main import app
from app.resolve import clean_text
from app.store import SaveStore
from app.ytm import ytm_client

client = TestClient(app)
AUTH_HEADERS = {"Authorization": f"Bearer {settings.ginini_token}"}


def test_clean_text() -> None:
    raw = 'Check out "Khatta Sadak" by Seedhe Maut (Official Video 2024) u/hiphophead https://reddit.com/r/dhh 🔥'
    cleaned = clean_text(raw)
    assert "https://" not in cleaned
    assert "u/" not in cleaned
    assert '"' not in cleaned
    assert "Official Video" not in cleaned
    assert "Seedhe Maut" in cleaned
    assert "Khatta Sadak" in cleaned


def test_auth_rejection() -> None:
    res = client.get("/health")
    assert res.status_code == 403 or res.status_code == 401

    res_invalid = client.get("/health", headers={"Authorization": "Bearer bad-token"})
    assert res_invalid.status_code == 401


def test_health_endpoint() -> None:
    res = client.get("/health", headers=AUTH_HEADERS)
    assert res.status_code == 200
    data = res.json()
    assert data["ok"] is True
    assert "ytm_auth" in data


def test_resolve_endpoint(monkeypatch: pytest.MonkeyPatch) -> None:
    mock_results = [
        {
            "videoId": "vid123",
            "title": "Namastute",
            "artists": [{"name": "Seedhe Maut"}],
            "album": {"name": "Nayaab"},
            "duration": "3:24",
        },
        {
            "videoId": "vid456",
            "title": "101",
            "artists": [{"name": "Seedhe Maut"}],
            "album": {"name": "101"},
            "duration": "2:50",
        },
    ]

    monkeypatch.setattr(ytm_client, "search_songs", lambda query, limit=4: mock_results)

    res = client.post(
        "/resolve",
        headers=AUTH_HEADERS,
        json={"text": "Namastute by Seedhe Maut u/user", "kind": "song"},
    )
    assert res.status_code == 200
    data = res.json()
    assert data["best_match"]["videoId"] == "vid123"
    assert data["best_match"]["title"] == "Namastute"
    assert len(data["candidates"]) == 1
    assert data["candidates"][0]["videoId"] == "vid456"


def test_save_song_endpoint(monkeypatch: pytest.MonkeyPatch, tmp_path) -> None:
    mock_ytm = MagicMock()
    mock_ytm.get_or_create_playlist.return_value = "pl_inbox_123"
    mock_ytm.add_song_to_inbox.return_value = ("pl_inbox_123", "set_vid_1", False)

    monkeypatch.setattr(ytm_client, "client", mock_ytm)
    monkeypatch.setattr(ytm_client, "add_song_to_inbox", lambda vid: ("pl_inbox_123", "set_vid_1", False))

    res = client.post(
        "/save/song",
        headers=AUTH_HEADERS,
        json={"videoId": "test_vid_1", "title": "Test Title", "artist": "Test Artist", "source_pkg": "com.reddit.frontpage"},
    )
    assert res.status_code == 200
    data = res.json()
    assert data["playlistId"] == "pl_inbox_123"
    assert data["already_saved"] is False
    assert data["setVideoId"] == "set_vid_1"


def test_save_song_duplicate(monkeypatch: pytest.MonkeyPatch) -> None:
    mock_ytm = MagicMock()
    monkeypatch.setattr(ytm_client, "client", mock_ytm)
    monkeypatch.setattr(ytm_client, "add_song_to_inbox", lambda vid: ("pl_inbox_123", None, True))

    res = client.post(
        "/save/song",
        headers=AUTH_HEADERS,
        json={"videoId": "duplicate_vid", "title": "Dupe Song", "artist": "Artist"},
    )
    assert res.status_code == 200
    data = res.json()
    assert data["already_saved"] is True
    assert data["setVideoId"] is None


def test_undo_endpoint(monkeypatch: pytest.MonkeyPatch) -> None:
    mock_ytm = MagicMock()
    monkeypatch.setattr(ytm_client, "client", mock_ytm)
    monkeypatch.setattr(ytm_client, "undo_save", lambda pl_id, items: len(items))

    res = client.post(
        "/undo",
        headers=AUTH_HEADERS,
        json={"playlistId": "pl_123", "items": [{"setVideoId": "set_1"}]},
    )
    assert res.status_code == 200
    assert res.json()["removed_count"] == 1
