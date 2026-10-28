from typing import Any, Dict, List, Literal, Optional
from pydantic import BaseModel, Field


class HealthResponse(BaseModel):
    ok: bool = True
    ytm_auth: bool = False


class CandidateTrack(BaseModel):
    videoId: str
    title: str
    artist: str
    album: Optional[str] = None
    duration: Optional[str] = None
    thumbnailUrl: Optional[str] = None


class ResolveRequest(BaseModel):
    text: str = Field(..., min_length=1, description="Raw captured text to resolve")
    kind: Literal["auto", "song", "artist"] = "auto"


class ResolveResponse(BaseModel):
    best_match: Optional[CandidateTrack] = None
    candidates: List[CandidateTrack] = Field(default_factory=list)
    cleaned_query: str


class SaveSongRequest(BaseModel):
    videoId: str = Field(..., description="YouTube Music videoId")
    title: Optional[str] = None
    artist: Optional[str] = None
    source_pkg: str = "unknown"


class SaveSongResponse(BaseModel):
    playlistId: str
    setVideoId: Optional[str] = None
    already_saved: bool = False
    videoId: str


class SaveArtistRequest(BaseModel):
    browseId: str = Field(..., description="YouTube Music channel/browseId")
    artist: Optional[str] = None
    source_pkg: str = "unknown"


class SaveArtistResponse(BaseModel):
    playlistId: str
    artist: str
    songs_added: List[CandidateTrack] = Field(default_factory=list)


class UndoRequest(BaseModel):
    playlistId: str
    items: List[Dict[str, Any]] = Field(
        ...,
        description="List of items returned from save containing setVideoId or videoId",
    )


class UndoResponse(BaseModel):
    ok: bool
    removed_count: int


class LikeRequest(BaseModel):
    title: str
    artist: str


class LikeResponse(BaseModel):
    ok: bool
    videoId: Optional[str] = None
    title: str
    artist: str


class AddCurrentRequest(BaseModel):
    title: str
    artist: str
    source_pkg: str = "youtube_music"


class AddCurrentResponse(BaseModel):
    playlistId: str
    setVideoId: Optional[str] = None
    videoId: str
    title: str
    artist: str
