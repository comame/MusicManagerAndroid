package xyz.comame.musicmanager

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// MusicTrack.cs
@Serializable
data class MusicTrack (
    @SerialName("Name")
    val name: String,
    @SerialName("AlbumArtist")
    val albumArtist: String,
    @SerialName("AlbumTitle")
    val albumTitle: String,
    @SerialName("Artists")
    val artists: List<String>,
    @SerialName("Genre")
    val genre: List<String>,
    @SerialName("Year")
    val year: Int,
    @SerialName("TrackNumber")
    val trackNumber: Int,
    @SerialName("TrackCount")
    val trackCount: Int,
    @SerialName("DiscNumber")
    val discNumber: Int,
    @SerialName("DiscCount")
    val discCount: Int,
    @SerialName("DurationMilliSeconds")
    val durationMilliSeconds: ULong,

    @SerialName("Format")
    val format: String,
    @SerialName("Channels")
    val channels: Int,
    @SerialName("IsVBR")
    val isVBR: Boolean,
    @SerialName("SampleRate")
    val sampleRate: UInt,
    @SerialName("Bitrate")
    val bitrate: UInt,
    @SerialName("Imported")
    val imported: String, // datetime string

    @SerialName("Path")
    val path: String,
    @SerialName("Modified")
    val modified: String, // datetime string
    @SerialName("Created")
    val created: String, // datetime string
    @SerialName("SizeBytes")
    val sizeBytes: ULong,

    @SerialName("PersistentID")
    val persistentID: String,
)

fun MusicTrack.androidFileName(): String {
    return "${this.persistentID}.${this.format}"
}