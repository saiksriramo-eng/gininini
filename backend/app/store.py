import sqlite3
from datetime import datetime, timezone
from pathlib import Path
from typing import Any, Dict, List, Optional
from app.config import settings


class SaveStore:
    def __init__(self, db_path: Optional[str] = None):
        self.db_path = db_path or str(Path(__file__).resolve().parent.parent / settings.database_path)
        self._init_db()

    def _get_connection(self) -> sqlite3.Connection:
        conn = sqlite3.connect(self.db_path)
        conn.row_factory = sqlite3.Row
        return conn

    def _init_db(self) -> None:
        with self._get_connection() as conn:
            conn.execute(
                """
                CREATE TABLE IF NOT EXISTS saves (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    created_at TEXT NOT NULL,
                    query_text TEXT,
                    video_id TEXT NOT NULL,
                    title TEXT,
                    artist TEXT,
                    playlist_id TEXT,
                    set_video_id TEXT,
                    source_pkg TEXT,
                    kind TEXT NOT NULL,
                    status TEXT NOT NULL
                )
                """
            )
            conn.commit()

    def record_save(
        self,
        video_id: str,
        title: Optional[str],
        artist: Optional[str],
        playlist_id: Optional[str],
        set_video_id: Optional[str],
        source_pkg: str = "unknown",
        kind: str = "song",
        query_text: Optional[str] = None,
        status: str = "saved",
    ) -> int:
        now_iso = datetime.now(timezone.utc).isoformat()
        with self._get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute(
                """
                INSERT INTO saves (
                    created_at, query_text, video_id, title, artist,
                    playlist_id, set_video_id, source_pkg, kind, status
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                (
                    now_iso,
                    query_text,
                    video_id,
                    title,
                    artist,
                    playlist_id,
                    set_video_id,
                    source_pkg,
                    kind,
                    status,
                ),
            )
            conn.commit()
            return cursor.lastrowid or 0

    def get_recent_saves(self, limit: int = 50) -> List[Dict[str, Any]]:
        with self._get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute(
                "SELECT * FROM saves ORDER BY id DESC LIMIT ?",
                (limit,),
            )
            return [dict(row) for row in cursor.fetchall()]


store = SaveStore()
