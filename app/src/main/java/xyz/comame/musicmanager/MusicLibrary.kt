package xyz.comame.musicmanager

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// musicLibrary.cs
@Serializable
data class MusicLibrary(
    @SerialName("Tracks")
    val tracks: List<MusicTrack>,
)
