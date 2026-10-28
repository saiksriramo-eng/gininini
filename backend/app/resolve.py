import re
from typing import Any, Dict, List, Optional
from app.config import settings
from app.schemas import CandidateTrack


def clean_text(raw_text: str) -> str:
    """Cleans noisy text by removing URLs, Reddit tags, quotes, emojis, and filler metadata."""
    if not raw_text:
        return ""

    text = raw_text

    # Strip URLs
    text = re.sub(r"https?://\S+", " ", text)

    # Strip Reddit usernames and subreddits (u/name, /u/name, r/sub)
    text = re.sub(r"/?(?:u|r)/[A-Za-z0-9_-]+", " ", text)

    # Strip common music video tags and years in parentheses or brackets
    text = re.sub(
        r"[\(\[\{][^\)\]\}]*(?:official|video|audio|visualizer|lyric|prod\.?|remix|remaster|hd|4k|\b(?:19|20)\d{2}\b)[^\)\]\}]*[\)\]\}]",
        " ",
        text,
        flags=re.IGNORECASE,
    )

    # Strip quotes
    text = re.sub(r'["\'`“‘’”«»]', " ", text)

    # Strip emojis (covers surrogate pairs and broad unicode symbols)
    emoji_pattern = re.compile(
        "["
        "\U0001f600-\U0001f64f"  # emoticons
        "\U0001f300-\U0001f5ff"  # symbols & pictographs
        "\U0001f680-\U0001f6ff"  # transport & map
        "\U0001f1e0-\U0001f1ff"  # flags (iOS)
        "\U00002702-\U000027b0"
        "\U000024c2-\U0001f251"
        "]+",
        flags=re.UNICODE,
    )
    text = emoji_pattern.sub(" ", text)

    # Normalize whitespace
    text = re.sub(r"\s+", " ", text).strip()
    return text


async def extract_with_claude(raw_text: str) -> Optional[str]:
    """Uses Anthropic API if key is present to extract clean song/artist query from noisy text."""
    if not settings.anthropic_api_key:
        return None

    try:
        from anthropic import AsyncAnthropic

        client = AsyncAnthropic(api_key=settings.anthropic_api_key)
        response = await client.messages.create(
            model="claude-3-5-haiku-latest",
            max_tokens=100,
            temperature=0.0,
            system="Extract strictly the song title and artist from the user text in the format 'Song Title - Artist Name'. Output nothing else.",
            messages=[{"role": "user", "content": raw_text}],
        )
        first_block = response.content[0]
        if hasattr(first_block, "text") and first_block.text:
            return clean_text(first_block.text)
    except Exception:
        # Fall back gracefully to standard regex cleaning
        pass
    return None


def format_search_results(results: List[Dict[str, Any]], kind: str = "song") -> List[CandidateTrack]:
    candidates: List[CandidateTrack] = []

    for item in results:
        video_id = item.get("videoId")
        if not video_id:
            continue

        title = item.get("title", "Unknown Title")
        artists = item.get("artists", [])
        artist_name = "Unknown Artist"
        if artists and isinstance(artists, list):
            artist_name = ", ".join(a.get("name", "") for a in artists if a.get("name"))

        album_info = item.get("album")
        album_name = album_info.get("name") if isinstance(album_info, dict) else None
        duration = item.get("duration")

        thumbnails = item.get("thumbnails", [])
        thumb_url = thumbnails[-1].get("url") if thumbnails else None

        candidates.append(
            CandidateTrack(
                videoId=video_id,
                title=title,
                artist=artist_name,
                album=album_name,
                duration=duration,
                thumbnailUrl=thumb_url,
            )
        )
    return candidates
