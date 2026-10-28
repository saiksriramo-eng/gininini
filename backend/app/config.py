from pathlib import Path
from typing import Optional
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    ginini_token: str = "dev-token"
    ytm_auth_file: str = ""
    inbox_playlist: str = "Discovery Inbox"
    artists_playlist: str = "Artists to Explore"
    anthropic_api_key: Optional[str] = None
    database_path: str = "saves.db"

    model_config = SettingsConfigDict(
        env_file=Path(__file__).resolve().parent.parent / ".env",
        env_file_encoding="utf-8",
        extra="ignore",
    )


settings = Settings()
