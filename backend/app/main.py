import logging
from typing import Optional
from fastapi import Depends, FastAPI, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware

from app.auth import verify_token
from app.config import settings
from app.resolve import clean_text, extract_with_claude, format_search_results
from app.schemas import (
    AddCurrentRequest,
    AddCurrentResponse,
    HealthResponse,
    LikeRequest,
    LikeResponse,
    ResolveRequest,
    ResolveResponse,
    SaveArtistRequest,
    SaveArtistResponse,
    SaveSongRequest,
    SaveSongResponse,
    UndoRequest,
    UndoResponse,
)
from app.store import store
from app.ytm import ytm_client

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("ginininini-backend")

app = FastAPI(
    title="Ginininini Backend",
    description="Local YouTube Music bridge service for Ginininini Android Overlay",
    version="0.1.0",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/health", response_model=HealthResponse)
async def health(token: str = Depends(verify_token)) -> HealthResponse:
    return HealthResponse(ok=True, ytm_auth=ytm_client.is_authenticated)


@app.post("/resolve", response_model=ResolveResponse)
async def resolve(payload: ResolveRequest, token: str = Depends(verify_token)) -> ResolveResponse:
    cleaned = clean_text(payload.text)

    # Optional Claude extraction for complex text
    claude_cleaned = await extract_with_claude(payload.text)
    effective_query = claude_cleaned or cleaned

    if not effective_query:
        return ResolveResponse(best_match=None, candidates=[], cleaned_query="")

    try:
        raw_results = ytm_client.search_songs(effective_query, limit=4)
        candidates = format_search_results(raw_results, kind=payload.kind)
        best_match = candidates[0] if candidates else None

        return ResolveResponse(
            best_match=best_match,
            candidates=candidates[1:4] if len(candidates) > 1 else [],
            cleaned_query=effective_query,
        )
    except Exception as e:
        logger.error("Failed to resolve text: %s", e)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Error resolving text: {str(e)}",
        )


@app.post("/save/song", response_model=SaveSongResponse)
async def save_song(payload: SaveSongRequest, token: str = Depends(verify_token)) -> SaveSongResponse:
    if not ytm_client.is_authenticated:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="YouTube Music is not authenticated on backend. See RUNBOOK.md",
        )

    try:
        playlist_id, set_video_id, already_saved = ytm_client.add_song_to_inbox(payload.videoId)

        # Log save to SQLite store
        store.record_save(
            video_id=payload.videoId,
            title=payload.title,
            artist=payload.artist,
            playlist_id=playlist_id,
            set_video_id=set_video_id,
            source_pkg=payload.source_pkg,
            kind="song",
            status="already_saved" if already_saved else "saved",
        )

        return SaveSongResponse(
            playlistId=playlist_id,
            setVideoId=set_video_id,
            already_saved=already_saved,
            videoId=payload.videoId,
        )
    except Exception as e:
        logger.error("Error saving song: %s", e)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to save song to playlist: {str(e)}",
        )


@app.post("/save/artist", response_model=SaveArtistResponse)
async def save_artist(payload: SaveArtistRequest, token: str = Depends(verify_token)) -> SaveArtistResponse:
    if not ytm_client.is_authenticated:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="YouTube Music is not authenticated on backend",
        )

    try:
        playlist_id, artist_name, songs = ytm_client.add_artist_top_tracks(payload.browseId)

        for song in songs:
            store.record_save(
                video_id=song.videoId,
                title=song.title,
                artist=artist_name,
                playlist_id=playlist_id,
                set_video_id=None,
                source_pkg=payload.source_pkg,
                kind="artist_song",
                status="saved",
            )

        return SaveArtistResponse(
            playlistId=playlist_id,
            artist=artist_name,
            songs_added=songs,
        )
    except Exception as e:
        logger.error("Error saving artist: %s", e)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to save artist: {str(e)}",
        )


@app.post("/undo", response_model=UndoResponse)
async def undo(payload: UndoRequest, token: str = Depends(verify_token)) -> UndoResponse:
    if not ytm_client.is_authenticated:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="YouTube Music is not authenticated on backend",
        )

    try:
        count = ytm_client.undo_save(payload.playlistId, payload.items)
        return UndoResponse(ok=True, removed_count=count)
    except Exception as e:
        logger.error("Error undoing save: %s", e)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to undo save: {str(e)}",
        )


@app.post("/like", response_model=LikeResponse)
async def like_current(payload: LikeRequest, token: str = Depends(verify_token)) -> LikeResponse:
    if not ytm_client.is_authenticated:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="YouTube Music is not authenticated on backend",
        )

    query = f"{payload.title} {payload.artist}".strip()
    try:
        results = ytm_client.search_songs(query, limit=1)
        video_id = results[0].get("videoId") if results else None
        if not video_id:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Could not find matching track for '{query}'",
            )

        ytm_client.rate_song(video_id, "LIKE")
        return LikeResponse(ok=True, videoId=video_id, title=payload.title, artist=payload.artist)
    except Exception as e:
        logger.error("Error liking song: %s", e)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to like track: {str(e)}",
        )


@app.post("/add-current", response_model=AddCurrentResponse)
async def add_current(payload: AddCurrentRequest, token: str = Depends(verify_token)) -> AddCurrentResponse:
    if not ytm_client.is_authenticated:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="YouTube Music is not authenticated on backend",
        )

    query = f"{payload.title} {payload.artist}".strip()
    try:
        results = ytm_client.search_songs(query, limit=1)
        video_id = results[0].get("videoId") if results else None
        if not video_id:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Could not find matching track for '{query}'",
            )

        playlist_id, set_video_id, _ = ytm_client.add_song_to_inbox(video_id)
        store.record_save(
            video_id=video_id,
            title=payload.title,
            artist=payload.artist,
            playlist_id=playlist_id,
            set_video_id=set_video_id,
            source_pkg=payload.source_pkg,
            kind="now_playing",
            status="saved",
        )

        return AddCurrentResponse(
            playlistId=playlist_id,
            setVideoId=set_video_id,
            videoId=video_id,
            title=payload.title,
            artist=payload.artist,
        )
    except Exception as e:
        logger.error("Error adding current track: %s", e)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to add track: {str(e)}",
        )
